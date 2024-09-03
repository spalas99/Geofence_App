package com.example.geofenceapp;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.geofenceapp.databinding.ActivityResultsMapsBinding;

import java.util.ArrayList;
import java.util.List;

public class ResultsMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityResultsMapsBinding binding;

    private static String AUTHORITY = "com.example.geofenceapp";
    private static String PATH = "Locations";



    private Button pausebutton;

    private Button cancelbutton;

    ContentResolver ResultsMapsResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ResultsMapsResolver = this.getContentResolver();

        binding = ActivityResultsMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pausebutton = findViewById(R.id.pausebutton);
        pausebutton.setOnClickListener(v -> {
              if(ServiceRunning()){
                  stopService(new Intent(ResultsMapsActivity.this,LocationService.class));
              }else {
                  startService(new Intent(ResultsMapsActivity.this,LocationService.class));
              }

        });

        cancelbutton = findViewById(R.id.cancelbutton);
        cancelbutton.setOnClickListener(v -> {
            onBackPressed();
        });




    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showPosition(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},4);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        showPosition();

        CircleOptions cirlceOptions = new CircleOptions();
        cirlceOptions.strokeColor(Color.BLACK);
        cirlceOptions.radius(100);

        CircleOptions circleOptions2 =new CircleOptions();
        circleOptions2.strokeColor(Color.RED);
        circleOptions2.fillColor(Color.RED);
        circleOptions2.radius(3);


        new Thread(() -> {
            Uri uri = Uri.parse("content://"+AUTHORITY+"/"+PATH);
            Cursor cursor = ResultsMapsResolver.query(uri,null,null,null,null);

            if(cursor.moveToFirst()){
                do{
                    LatLng latLng =new LatLng(cursor.getDouble(2),cursor.getDouble(1));

                    if(cursor.getInt(4) == 1) {
                        runOnUiThread(() -> {
                            cirlceOptions.center(latLng);
                            mMap.addCircle(cirlceOptions);

                        });
                    } else if (cursor.getInt(4) == 2) {
                        runOnUiThread(() -> {
                            circleOptions2.center(latLng);
                            mMap.addCircle(circleOptions2);
                        });
                    }

                }while(cursor.moveToNext());

            }

        }).start();

    }

    //Check if the Service is running.
    private boolean ServiceRunning(){

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(LocationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}