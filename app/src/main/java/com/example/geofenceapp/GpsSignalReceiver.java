package com.example.geofenceapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

public class GpsSignalReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("LocationManager.MODE_CHANGED_ACTION")){
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(!gpsEnabled){
                Intent stopIntent = new Intent(context,LocationService.class);
                context.stopService(stopIntent);
                Log.d("noSignal", "LocationService stopped");

            }else {
                Intent startIntent = new Intent(context,LocationService.class);
                context.startService(startIntent);
                Log.d("GpsSignalReceiver", "LocationService started");
            }
        }
    }
}
