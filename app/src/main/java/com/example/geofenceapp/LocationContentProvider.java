package com.example.geofenceapp;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import java.util.List;

public class LocationContentProvider extends ContentProvider {
    private UriMatcher uriMatcher;
    private static String AUTHORITY =  "com.example.geofenceapp";
    private static String PATH = "Locations";

    private static final int INSERT_OPERATION = 2;

    LocationsDao locationsDao;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,PATH,1);
        uriMatcher.addURI(AUTHORITY,PATH + "/insert",INSERT_OPERATION);

        AppDataBase db = Room.databaseBuilder(getContext(),AppDataBase.class,"Locations").build();
        locationsDao = db.LocationsDao();


        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        switch (uriMatcher.match(uri)){
            case 1:
               cursor = locationsDao.getCursonAll();
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case INSERT_OPERATION:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.example.geofenceapp.Locations";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = uriMatcher.match(uri);
        Uri resultUri = null;

        switch (match){
            case 1:
                Locations locations = new Locations();
                locations.longitude = values.getAsDouble("longitude");
                locations.latitude  = values.getAsDouble("latitude");
                locations.sessionid = values.getAsInteger("session_id");
                locations.flag      = values.getAsInteger("flag");

                new Thread(() -> {
                    locationsDao.insertLocation(locations);
                }).start();

                resultUri = ContentUris.withAppendedId(uri,locations.id);
                getContext().getContentResolver().notifyChange(uri,null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        int rowsDeleted = 0;
        switch (match) {
            case 1:
                new Thread(() -> {
                    locationsDao.deleteAll();
                }).start();
                rowsDeleted = 1;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
