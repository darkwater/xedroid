package com.novaember.xedroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "xedroid";
    public static final int DATABASE_VERSION = 1;

    public static final String ORGANISATIONS_TABLE_CREATE
    =
        "CREATE TABLE organisations (" +
        "id             INT,  " +
        "name           TEXT, " +

        "PRIMARY KEY (id) " +
    ");";

    public static final String LOCATIONS_TABLE_CREATE
    =
        "CREATE TABLE locations (" +
        "id             INT,  " +
        "name           TEXT, " +
        "organisation   INT,  " +

        "PRIMARY KEY (id), " +
        "FOREIGN KEY (organisation) REFERENCES organisations " +
    ");";

    public static final String ATTENDEES_TABLE_CREATE
    =
        "CREATE TABLE attendees (" +
        "id             INT,  " +
        "name           TEXT, " +
        "location       INT,  " +
        "type           INT,  " +

        "PRIMARY KEY (id), " +
        "FOREIGN KEY (location) REFERENCES locations " +
    ");";

    public static final String EVENTS_TABLE_CREATE
    =
        "CREATE TABLE events (" +
        "id             INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "year           INT,  " +
        "week           INT,  " +
        "day            INT,  " +
        "description    TEXT, " +
        "start          TEXT, " +
        "end            TEXT  " +
    ");";

    public static final String ATTENDEE_EVENTS_TABLE_CREATE
    =
        "CREATE TABLE attendee_events (" +
        "attendee       INT, " +
        "event          INT, " +

        "FOREIGN KEY (attendee) REFERENCES attendees, " +
        "FOREIGN KEY (event) REFERENCES events " +
    ");";

    public static final String ATTENDEE_EVENTS_VIEW_CREATE
    =
        "CREATE VIEW attendee_events_view AS " +
        "SELECT attendee, event, year, week, day, description, start, end, name, location, type " +
        "FROM attendee_events " +
            "INNER JOIN events ON attendee_events.event = events.id " +
            "INNER JOIN attendees ON attendee_events.attendee = attendees.id" +
    ";";

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
        db.execSQL(EVENTS_TABLE_CREATE);
        db.execSQL(ATTENDEE_EVENTS_TABLE_CREATE);
        db.execSQL(ATTENDEE_EVENTS_VIEW_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }
}
