package com.customkeyboard;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Voice input using Groq's Whisper API for fast, accurate transcription.
 * Records audio in memory, sends to Groq API, returns transcription.
 */
public class GroqWhisperHelper {

    public interface VoiceCallback {
        void onTranscription(String text);
        void onError(String message);
        void onRecordingStateChanged(boolean isRecording);
    }

    private static final String TAG = "GroqWhisperHelper";
    private static final String API_URL = "https://api.groq.com/openai/v1/audio/transcriptions";
    private static final String MODEL = "whisper-large-v3-turbo";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private volatile boolean isRecording = false;
    private volatile boolean isSending = false;
    private Thread recordThread;
    private Thread apiThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private VoiceCallback callback;
    private final Object recordLock = new Object();

    public void setCallback(VoiceCallback callback) {
        this.callback = callback;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void startRecording() {
        if (isRecording) return;

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            notifyError("Audio recording not supported on this device");
            return;
        }

        try {
            synchronized (recordLock) {
                audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE, CHANNEL, ENCODING, bufferSize);

                if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    audioRecord.release();
                    audioRecord = null;
                    notifyError("Failed to initialize audio recorder");
                    return;
                }
            }

            isRecording = true;
            notifyRecordingState(true);

            synchronized (recordLock) {
                if (audioRecord != null) audioRecord.startRecording();
            }

            recordThread = new Thread(() -> {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[bufferSize];

                while (isRecording) {
                    int read;
                    synchronized (recordLock) {
                        if (audioRecord == null) break;
                        read = audioRecord.read(buffer, 0, buffer.length);
                    }
                    if (read > 0) {
                        baos.write(buffer, 0, read);
                    } else if (read < 0) {
                        break;
                    }
                }

                byte[] audioData = baos.toByteArray();
                if (audioData.length > 0) {
                    sendToGroqApi(audioData);
                } else {
                    notifyError("No audio recorded");
                }
            }, "GroqWhisperRecorder");
            recordThread.start();

        } catch (SecurityException e) {
            notifyError("Microphone permission not granted");
        } catch (Exception e) {
            notifyError("Failed to start recording: " + e.getMessage());
        }
    }

    public void stopRecording() {
        if (!isRecording) return;
        isRecording = false;
        notifyRecordingState(false);

        synchronized (recordLock) {
            if (audioRecord != null) {
                try { audioRecord.stop(); } catch (Exception ignored) {}
                audioRecord.release();
                audioRecord = null;
            }
        }

        if (recordThread != null) {
            try { recordThread.join(2000); } catch (InterruptedException ignored) {}
            recordThread = null;
        }
    }

    public void release() {
        stopRecording();
        if (apiThread != null) {
            apiThread.interrupt();
            apiThread = null;
        }
        isSending = false;
        callback = null;
    }

    private void sendToGroqApi(byte[] audioData) {
        String apiKey = CustomKeyboardService.getGroqApiKeyStatic();
        if (apiKey == null || apiKey.isEmpty()) {
            notifyError("Groq API key not set. Go to keyboard settings.");
            return;
        }

        if (isSending) {
            notifyError("Already processing previous recording");
            return;
        }
        isSending = true;

        apiThread = new Thread(() -> {
            try {
                // Convert PCM to WAV
                byte[] wavData = pcmToWav(audioData, SAMPLE_RATE, 1, 16);

                // Build multipart/form-data request
                String boundary = "----GroqWhisperBoundary" + System.currentTimeMillis();
                String lineEnd = "\r\n";

                HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                OutputStream os = conn.getOutputStream();

                // model field
                os.write(("--" + boundary + lineEnd).getBytes());
                os.write(("Content-Disposition: form-data; name=\"model\"" + lineEnd + lineEnd).getBytes());
                os.write((MODEL + lineEnd).getBytes());

                // language field (auto-detect by omitting, or set to "en")
                // Omitting language lets Whisper auto-detect

                // file field
                os.write(("--" + boundary + lineEnd).getBytes());
                os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"audio.wav\"" + lineEnd).getBytes());
                os.write(("Content-Type: audio/wav" + lineEnd + lineEnd).getBytes());
                os.write(wavData);
                os.write(lineEnd.getBytes());

                // closing boundary
                os.write(("--" + boundary + "--" + lineEnd).getBytes());
                os.close();

                int code = conn.getResponseCode();
                InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();

                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) != -1) {
                    if (Thread.interrupted()) break;
                    responseStream.write(buf, 0, n);
                }
                is.close();
                conn.disconnect();

                String responseStr = responseStream.toString("UTF-8");

                if (code != 200) {
                    String errorMsg = "Groq API error (" + code + ")";
                    try {
                        JSONObject err = new JSONObject(responseStr);
                        if (err.has("error")) {
                            JSONObject errorObj = err.getJSONObject("error");
                            errorMsg = errorObj.optString("message", errorMsg);
                        }
                    } catch (Exception ignored) {}
                    notifyError(errorMsg);
                    return;
                }

                // Groq Whisper returns: {"text": "..."}
                JSONObject response = new JSONObject(responseStr);
                String text = response.optString("text", "").trim();
                if (!text.isEmpty()) {
                    notifyTranscription(text);
                } else {
                    notifyError("No transcription returned");
                }

            } catch (InterruptedException e) {
                // Cancelled
            } catch (Exception e) {
                if (!Thread.interrupted()) {
                    Log.e(TAG, "Groq API call failed", e);
                    notifyError("Transcription failed: " + e.getMessage());
                }
            } finally {
                isSending = false;
            }
        }, "GroqWhisperAPI");
        apiThread.start();
    }

    private byte[] pcmToWav(byte[] pcmData, int sampleRate, int channels, int bitsPerSample) {
        int dataLength = pcmData.length;
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(44 + dataLength);
        try {
            baos.write("RIFF".getBytes());
            baos.write(intToLittleEndian(36 + dataLength));
            baos.write("WAVE".getBytes());
            baos.write("fmt ".getBytes());
            baos.write(intToLittleEndian(16));
            baos.write(shortToLittleEndian(1));
            baos.write(shortToLittleEndian(channels));
            baos.write(intToLittleEndian(sampleRate));
            baos.write(intToLittleEndian(byteRate));
            baos.write(shortToLittleEndian(blockAlign));
            baos.write(shortToLittleEndian(bitsPerSample));
            baos.write("data".getBytes());
            baos.write(intToLittleEndian(dataLength));
            baos.write(pcmData);
        } catch (Exception ignored) {}
        return baos.toByteArray();
    }

    private byte[] intToLittleEndian(int value) {
        return new byte[]{
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 24) & 0xFF)
        };
    }

    private byte[] shortToLittleEndian(int value) {
        return new byte[]{
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF)
        };
    }

    private void notifyTranscription(String text) {
        mainHandler.post(() -> { if (callback != null) callback.onTranscription(text); });
    }

    private void notifyError(String msg) {
        mainHandler.post(() -> { if (callback != null) callback.onError(msg); });
    }

    private void notifyRecordingState(boolean recording) {
        mainHandler.post(() -> { if (callback != null) callback.onRecordingStateChanged(recording); });
    }
}
