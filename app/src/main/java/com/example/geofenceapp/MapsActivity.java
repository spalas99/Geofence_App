package com.example.geofenceapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ThemedSpinnerAdapter;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.geofenceapp.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final static int FINE_LOCATION_PERMISSION_REQUEST = 4;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    Circle mapCircle;
    Circle circlefound;
    static List<Circle> mCircleList;
    static List<Locations> dbResults;
    public int sessionid = 1;

    private int dbflag = 1;



    private static String AUTHORITY = "com.example.geofenceapp";
    private static String PATH = "Locations";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ContentResolver resolver = this.getContentResolver();

        //Service Intent
        Intent intent = new Intent(this,LocationService.class);

        //Cancel Button.Clear the map and go to MainActivity.
        Button mapbutton1 = (Button) findViewById(R.id.mapbutton_1);
        mapbutton1.setOnClickListener(v -> {
            if(mMap!=null){
                mMap.clear();
            }
            if(mCircleList!=null){
                mCircleList.clear();
            }
            onBackPressed();
        });

        //New Session Button.Clear the database for new session.
        Button mapbutton3 = (Button) findViewById(R.id.mapbutton_3);
        mapbutton3.setOnClickListener(v -> {
            Uri uri = Uri.parse("content://"+AUTHORITY+"/"+PATH);
            getContentResolver().delete(uri,null,null);
        });


        //Start Button.Take the Geofences from map and starts the session.
        Button mapbutton2 = (Button) findViewById(R.id.mapbutton_2);
        mapbutton2.setOnClickListener(v -> {
            Uri uri = Uri.parse("content://"+AUTHORITY+"/"+PATH);

            sessionid=getSessionId();

             for(Circle c: mCircleList) {
                 ContentValues values = new ContentValues();
                 values.put("longitude", c.getCenter().longitude);
                 values.put("latitude", c.getCenter().latitude);
                 values.put("session_id", sessionid);
                 values.put("flag", dbflag);

                 getContentResolver().insert(uri, values);
             }


            startService(intent);


        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode ){
            case FINE_LOCATION_PERMISSION_REQUEST:
                for(int i=0 ; i<permissions.length; i++){
                    String permission = permissions[i];
                    if(Objects.equals(permission, Manifest.permission.ACCESS_FINE_LOCATION)){
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED){

                        }
                    }
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showPosition(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},4);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        showPosition();
        mMap.setOnMapLongClickListener(latLng -> {

            if(mCircleList==null){
                mCircleList = new ArrayList<Circle>();
            }else{
                circlefound = findCircle(latLng,mCircleList);
                if(circlefound!=null){
                    circlefound.remove();
                    mCircleList.remove(circlefound);
                }else{
                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(latLng);
                    circleOptions.radius(100);
                    circleOptions.strokeColor(Color.BLACK);

                    mCircleList.add(mMap.addCircle(circleOptions));

                }
            }
        });
    }


    //Use this to find long clicks when happen on allready exist circles.
    public Circle findCircle(LatLng ltlng, List<Circle> circles){
        int result = 0;
        if(circles!=null) {
            for(Circle c : circles) {
                result = Distance(ltlng.latitude,ltlng.longitude,c.getCenter().latitude,c.getCenter().longitude);
                if(result <= 100){
                    return c;
                }
            }
        }
        return null;
    }


    public  int Distance(double lat1,double lon1,double lat2,double lon2){
        double R = 6371; //radius of the earth in km

        double ph1 = Math.toRadians(lat1);
        double ph2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLamda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2 ) * Math.sin(deltaPhi / 2 ) + Math.cos(ph1) * Math.cos(ph2) * Math.sin(deltaLamda / 2) * Math.sin(deltaLamda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        //distance in meters
        return (int ) Math.round(R * c * 1000);
    }


    public void  PrintCircles(List<Circle > circles){
        if(circles!=null){
            for (Circle c : circles) {
                Logger.getAnonymousLogger().severe(c.getCenter().toString());
            }
        }

    }

    private int getSessionId(){
        return sessionid++;
    }
}




