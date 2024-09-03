package com.example.geofenceapp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "Locations")
public class Locations {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name="longitude")
    public double longitude;

    @ColumnInfo(name="latitude")
    public double latitude;

    @ColumnInfo(name="session_id")
    public int sessionid;

    @ColumnInfo(name = "flag")
    public int flag;

}
