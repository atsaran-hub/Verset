package com.example.verset; // <-- change ce package avec le tien

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.verset.Onboarding.PremiumOfferActivity;

public class FreeTryOutActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnTryNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.freetryout); // <-- mets ici le bon nom de layout

        // Bind views
        btnBack = findViewById(R.id.btnBack);
        btnTryNow = findViewById(R.id.btnTryNow);

        // Back: revenir à l'écran précédent
        btnBack.setOnClickListener(v -> finish());

        // Next (Try now): aller vers PremiumOfferActivity
        btnTryNow.setOnClickListener(v -> {
            Intent intent = new Intent(FreeTryOutActivity.this, PremiumOfferActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        // même comportement que le bouton back en haut
        finish();
    }
}
