package com.example.diemen.mapapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anujkumars on 10/20/2015.
 */
public class DbHandler extends SQLiteOpenHelper {


    public DbHandler(Context context) {
        super(context, DbTableStrings.DATABASE_NAME, null, DbTableStrings.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Schema.CREATE_LOC_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DbTableStrings.TABLE_NAME_LOC);
        // Create tables again
        onCreate(db);
    }


    public List<LocationModel> readLocData(Context context) {
        List<LocationModel> locList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + DbTableStrings.TABLE_NAME_LOC;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do {
                LocationModel locationModel = new LocationModel();
                locationModel.name = cursor.getString(cursor.getColumnIndex(DbTableStrings.NAME));
                locationModel.address = cursor.getString(cursor.getColumnIndex(DbTableStrings.ADDRESS));
                locationModel.lat = String.valueOf(cursor.getDouble(cursor.getColumnIndex(DbTableStrings.LAT)));
                locationModel.lng = String.valueOf(cursor.getDouble(cursor.getColumnIndex(DbTableStrings.LNG)));
                Double val=cursor.getDouble(cursor.getColumnIndex(DbTableStrings.LAT));
                locList.add(locationModel);
            }while(cursor.moveToNext());

        }
        cursor.close();
        return locList;
    }


    public void insertLoc(LocationModel model){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbTableStrings.NAME, model.name);
        contentValues.put(DbTableStrings.ADDRESS, model.address);
        contentValues.put(DbTableStrings.LAT, model.lat);
        contentValues.put(DbTableStrings.LNG, model.lng);
        db.insert(DbTableStrings.TABLE_NAME_LOC,null, contentValues);
    }


}
