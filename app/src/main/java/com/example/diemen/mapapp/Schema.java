package com.example.diemen.mapapp;

/**
 * Created by anujkumars on 10/20/2015.
 */
public class Schema {

    public static final String CREATE_LOC_TABLE = "create table if not exists " + DbTableStrings.TABLE_NAME_LOC +
            "( _id integer primary key autoincrement, "
            + DbTableStrings.NAME + " string, "
            + DbTableStrings.ADDRESS + " string, "
            + DbTableStrings.LAT + " double, "
            + DbTableStrings.LNG + " double) ";
}
