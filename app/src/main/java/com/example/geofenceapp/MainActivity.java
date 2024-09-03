package com.example.geofenceapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button button1;
    private Button button3;
    private Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this,LocationService.class);

        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapActivity();
            }
        });

        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(v -> {
            stopService(intent);

        });



        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(v -> {
            openResultsMapsActivity();

        });


    }

    public void openMapActivity(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void openResultsMapsActivity(){
         Intent intent = new Intent(this,ResultsMapsActivity.class);
         startActivity(intent);
    }
}