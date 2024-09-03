package com.example.geofenceapp;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class LocationService extends Service  {

    private LocationManager locationManager;

    private MyLocationListener locationListener;

    private GpsSignalReceiver GpsReceiver;
    private static String AUTHORITY = "com.example.geofenceapp";
    private static String PATH = "Locations";

    long minTime = 5000;
    float minDistance = 50;

    public int sessionid = 1;

    //This flag using to stand out the locations types on Database (dbflag = 2 means this is a users location on geofence)
    private int dbflag=2;


    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GpsReceiver = new GpsSignalReceiver();

        ContentResolver ServiceResolver = this.getContentResolver();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(ServiceResolver);

        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver( GpsReceiver,filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_NOT_STICKY;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);

        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(GpsReceiver);
        locationManager.removeUpdates( locationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class MyLocationListener implements android.location.LocationListener {
        private ContentResolver resolver;


        public MyLocationListener(ContentResolver resolver){
            this.resolver=resolver;

        }

        @Override
        public void onLocationChanged(@NonNull Location location) {


            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            new Thread(() -> {
                int result = 0;
                Uri uri = Uri.parse("content://"+AUTHORITY+"/"+PATH);
                Cursor cursor = resolver.query(uri,null,null,null,null);

                if(cursor.moveToFirst()) {
                    do {
                        if (cursor.getInt(4) == 1) {
                            result = Distance(cursor.getDouble(2), cursor.getDouble(1), latitude, longitude);
                            if (result <= 100) {
                                Logger.getAnonymousLogger().severe("inside");

                                //Insert location when the user is inside with content provider in database.
                                ContentValues values = new ContentValues();
                                values.put("longitude",location.getLongitude());
                                values.put("latitude",location.getLatitude());
                                values.put("session_id",sessionid);
                                values.put("flag",dbflag);

                                getContentResolver().insert(uri,values);

                            } else {
                                Logger.getAnonymousLogger().severe("outside");
                            }
                        }
                        }while (cursor.moveToNext()) ;

                }
            }).start();
            Log.d("latitude", "longitude" +latitude +longitude);


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //LocationListener.super.onStatusChanged(provider, status, extras);
        }


        //Method that calculate the distance between two locations with lats and longs.
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
    }



}