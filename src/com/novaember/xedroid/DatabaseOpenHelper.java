package com.novaember.xedroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "xedroid";
    public static final int DATABASE_VERSION = 1;

    public static final String ORGANISATIONS_TABLE_NAME = "organisations";
    public static final String ORGANISATIONS_TABLE_CREATE =
                "CREATE TABLE " + ORGANISATIONS_TABLE_NAME + " (" +
                "id INT, " +
                "name TEXT, " +
                "PRIMARY KEY (id));";

    public static final String LOCATIONS_TABLE_NAME = "locations";
    public static final String LOCATIONS_TABLE_CREATE =
                "CREATE TABLE " + LOCATIONS_TABLE_NAME + " (" +
                "id INT, " +
                "name TEXT, " +
                "organisation INT, " +
                "PRIMARY KEY (id));";

    public static final String ATTENDEES_TABLE_NAME = "attendees";
    public static final String ATTENDEES_TABLE_CREATE =
                "CREATE TABLE " + ATTENDEES_TABLE_NAME + " (" +
                "id INT, " +
                "name TEXT, " +
                "location INT, " +
                "type INT, " +
                "PRIMARY KEY (id));";

    DatabaseOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(ORGANISATIONS_TABLE_CREATE);
        db.execSQL(LOCATIONS_TABLE_CREATE);
        db.execSQL(ATTENDEES_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }
}
