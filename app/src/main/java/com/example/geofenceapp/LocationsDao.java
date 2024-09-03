package com.example.geofenceapp;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocationsDao {

    @Query("SELECT * FROM Locations")
    public List<Locations> getAll();

    @Query("SELECT * FROM Locations")
    public Cursor getCursonAll();

    @Query("SELECT * FROM Locations WHERE session_id = :sessionid")
    public Locations getLocationsbyId(int sessionid);

    @Insert
    public void insertLocation(Locations... Locations);

    @Query("DELETE FROM locations")
    void deleteAll();

}
