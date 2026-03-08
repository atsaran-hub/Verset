package com.example.verset.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.verset.R;
import com.google.android.material.button.MaterialButton;

public class GodRelationshipIntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.godrelationship_intro);

        // Back
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Continue -> Recueillement Frequency page
        MaterialButton btnContinue = findViewById(R.id.buttonContinue);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                Intent intent = new Intent(GodRelationshipIntroActivity.this, RecueillementFrequencyActivity.class);
                startActivity(intent);

                // Transition SAFE (si tes anims existent)
                int enter = getResources().getIdentifier("slide_in_right", "anim", getPackageName());
                int exit = getResources().getIdentifier("slide_out_left", "anim", getPackageName());
                if (enter != 0 && exit != 0) {
                    overridePendingTransition(enter, exit);
                }
            });
        }
    }
}

