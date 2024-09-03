package com.example.geofenceapp;


import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {Locations.class}, version = 1 )
public abstract class AppDataBase extends RoomDatabase{
    public abstract LocationsDao LocationsDao();

}
