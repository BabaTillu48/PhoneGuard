package com.phoneguard.blocker;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private TextView streakDisplay;
    private TextView statusText;
    private EditText editKeywords;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        streakDisplay = findViewById(R.id.streakDisplay);
        statusText = findViewById(R.id.statusText);
        editKeywords = findViewById(R.id.editKeywords);

        Set<String> keywords = GuardPrefs.getKeywords(this);
        StringBuilder sb = new StringBuilder();
        for (String kw : keywords) {
            sb.append(kw).append("\n");
        }
        editKeywords.setText(sb.toString());

        final MainActivity self = this;

        findViewById(R.id.btnAccessibility).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });

        findViewById(R.id.btnOverlay).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())
                    );
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.btnSaveKeywords).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GuardPrefs.saveKeywords(self, editKeywords.getText().toString());
                statusText.setText("Keywords saved.");
            }
        });

        findViewById(R.id.btnTestOverlay).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                OverlayManager.showBlockOverlay(self);
            }
        });

        updateStreak();
    }

    protected void onResume() {
        super.onResume();
        updateStreak();
    }

    private void updateStreak() {
        long days = GuardPrefs.getStreakDays(this);
        int goal = GuardPrefs.getGoalDays();
        streakDisplay.setText(days + " / " + goal + " days");
    }
}
