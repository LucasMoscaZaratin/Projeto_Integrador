package com.example.magnetometro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private Button buttonMagnetometro;

    @Override
    protected void onCreate (Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);
        setContentView(R.layout.activity_main);

        buttonMagnetometro = findViewById(R.id.magnetometroButton);

        buttonMagnetometro.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Magnetometro.class);
                startActivity(intent);

            }
        });
    }

}

