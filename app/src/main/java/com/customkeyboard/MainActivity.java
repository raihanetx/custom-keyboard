package com.customkeyboard;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button enableButton = findViewById(R.id.btn_enable);
        Button switchButton = findViewById(R.id.btn_switch);
        TextView statusText = findViewById(R.id.tv_status);

        enableButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            startActivity(intent);
        });

        switchButton.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showInputMethodPicker();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        TextView statusText = findViewById(R.id.tv_status);
        String enabledIMEs = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS);
        if (enabledIMEs != null && enabledIMEs.contains(getPackageName())) {
            statusText.setText("✅ Keyboard is enabled!");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            statusText.setText("⚠️ Keyboard not enabled yet");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
}
