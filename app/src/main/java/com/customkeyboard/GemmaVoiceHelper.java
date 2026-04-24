package com.customkeyboard;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Voice input using Google AI Studio (Gemini/Gemma) API.
 * Records audio in memory, sends to API, returns transcription.
 * No files saved — audio stays in RAM the whole time.
 */
public class GemmaVoiceHelper {

    public interface VoiceCallback {
        void onTranscription(String text);
        void onError(String message);
        void onRecordingStateChanged(boolean isRecording);
    }

    private static final String TAG = "GemmaVoiceHelper";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private volatile boolean isRecording = false;
    private Thread recordThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private VoiceCallback callback;

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
            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, CHANNEL, ENCODING, bufferSize);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                notifyError("Failed to initialize audio recorder");
                return;
            }

            isRecording = true;
            notifyRecordingState(true);

            audioRecord.startRecording();

            recordThread = new Thread(() -> {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[bufferSize];

                while (isRecording) {
                    int read = audioRecord.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        baos.write(buffer, 0, read);
                    }
                }

                // Recording stopped — send to API
                byte[] audioData = baos.toByteArray();
                if (audioData.length > 0) {
                    sendToApi(audioData);
                } else {
                    notifyError("No audio recorded");
                }
            }, "GemmaVoiceRecorder");
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

        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (Exception ignored) {}
            audioRecord.release();
            audioRecord = null;
        }
    }

    private void sendToApi(byte[] audioData) {
        String apiKey = CustomKeyboardService.getGemmaApiKeyStatic();
        if (apiKey == null || apiKey.isEmpty()) {
            notifyError("API key not set. Go to keyboard settings.");
            return;
        }

        new Thread(() -> {
            try {
                // Convert PCM to WAV in memory
                byte[] wavData = pcmToWav(audioData, SAMPLE_RATE, 1, 16);
                String base64Audio = Base64.encodeToString(wavData, Base64.NO_WRAP);

                // Build request
                JSONObject part1 = new JSONObject();
                part1.put("text", "Transcribe this audio accurately. Return ONLY the transcribed text, nothing else. If the audio is in Bangla, transcribe in Bangla. If in English, transcribe in English.");

                JSONObject audioPart = new JSONObject();
                audioPart.put("mime_type", "audio/wav");
                audioPart.put("data", base64Audio);

                JSONObject part2 = new JSONObject();
                part2.put("inline_data", audioPart);

                JSONArray parts = new JSONArray();
                parts.put(part1);
                parts.put(part2);

                JSONObject content = new JSONObject();
                content.put("parts", parts);

                JSONArray contents = new JSONArray();
                contents.put(content);

                JSONObject body = new JSONObject();
                body.put("contents", contents);

                // Make API call
                String model = "gemini-2.0-flash";
                String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int code = conn.getResponseCode();
                InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();

                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) != -1) {
                    responseStream.write(buf, 0, n);
                }
                is.close();
                conn.disconnect();

                String responseStr = responseStream.toString("UTF-8");

                if (code != 200) {
                    String errorMsg = "API error (" + code + ")";
                    try {
                        JSONObject err = new JSONObject(responseStr);
                        if (err.has("error")) {
                            errorMsg = err.getJSONObject("error").optString("message", errorMsg);
                        }
                    } catch (Exception ignored) {}
                    notifyError(errorMsg);
                    return;
                }

                // Parse response
                JSONObject response = new JSONObject(responseStr);
                JSONArray candidates = response.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject candidate = candidates.getJSONObject(0);
                    JSONObject contentObj = candidate.getJSONObject("content");
                    JSONArray responseParts = contentObj.getJSONArray("parts");
                    if (responseParts.length() > 0) {
                        String text = responseParts.getJSONObject(0).getString("text").trim();
                        if (!text.isEmpty()) {
                            notifyTranscription(text);
                            return;
                        }
                    }
                }

                notifyError("No transcription returned");

            } catch (Exception e) {
                Log.e(TAG, "API call failed", e);
                notifyError("API call failed: " + e.getMessage());
            }
        }, "GemmaAPI").start();
    }

    /**
     * Convert raw PCM data to WAV format in memory.
     */
    private byte[] pcmToWav(byte[] pcmData, int sampleRate, int channels, int bitsPerSample) {
        int dataLength = pcmData.length;
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(44 + dataLength);
        try {
            // RIFF header
            baos.write("RIFF".getBytes());
            baos.write(intToLittleEndian(36 + dataLength));
            baos.write("WAVE".getBytes());

            // fmt chunk
            baos.write("fmt ".getBytes());
            baos.write(intToLittleEndian(16)); // chunk size
            baos.write(shortToLittleEndian(1)); // PCM format
            baos.write(shortToLittleEndian(channels));
            baos.write(intToLittleEndian(sampleRate));
            baos.write(intToLittleEndian(byteRate));
            baos.write(shortToLittleEndian(blockAlign));
            baos.write(shortToLittleEndian(bitsPerSample));

            // data chunk
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
        mainHandler.post(() -> {
            if (callback != null) callback.onTranscription(text);
        });
    }

    private void notifyError(String msg) {
        mainHandler.post(() -> {
            if (callback != null) callback.onError(msg);
        });
    }

    private void notifyRecordingState(boolean recording) {
        mainHandler.post(() -> {
            if (callback != null) callback.onRecordingStateChanged(recording);
        });
    }
}
