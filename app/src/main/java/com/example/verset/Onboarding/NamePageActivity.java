package com.example.verset.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.verset.Prefs;
import com.example.verset.R;

public class NamePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.namepage); // <-- mets ton vrai layout ici

        ImageButton btnBack = findViewById(R.id.btnBack);
        EditText editName = findViewById(R.id.editTextInput);
        Button btnNext = findViewById(R.id.buttonGetStarted);

        // Back
        btnBack.setOnClickListener(v -> finish());

        // Next
        btnNext.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                editName.setError("Please enter your name");
                return;
            }

            // ✅ Sauvegarde dans Prefs
            Prefs.saveName(this, name);

            // ✅ Aller vers la page "WidgetReadyActivity"
            startActivity(new Intent(this, AgePageActivity.class));
            finish();
        });
    }
}
