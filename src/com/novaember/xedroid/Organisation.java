package com.novaember.xedroid;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Organisation implements Comparable<Organisation>
{
    private int id;
    private String name;

    public Organisation(int id)
    {
        this.id = id;

        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getReadableDatabase();
        Cursor cursor = db.query(DatabaseOpenHelper.ORGANISATIONS_TABLE_NAME,
                new String[]{ "id", "name" }, "id = " + this.id, null, null, null, "id", null);

        cursor.moveToFirst();
        this.name = cursor.getString(1);
    }

    public Organisation(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public Organisation(Cursor cursor)
    {
        this.id = cursor.getInt(0);
        this.name = cursor.getString(1);
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        if (name != null) return name;

        return "???";
    }

    @Override
    public int compareTo(Organisation org)
    {
        return name.compareTo(org.name);
    }

    public void save()
    {
        ContentValues values = new ContentValues();
        values.put("id", this.id);
        values.put("name", this.name);

        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getWritableDatabase();
        db.insertWithOnConflict(DatabaseOpenHelper.ORGANISATIONS_TABLE_NAME,
                null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public static ArrayList<Organisation> getAll()
    {
        ArrayList<Organisation> output = new ArrayList<Organisation>();

        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getReadableDatabase();
        Cursor cursor = db.query(DatabaseOpenHelper.ORGANISATIONS_TABLE_NAME,
                new String[]{ "id", "name" }, null, null, null, null, "name", null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            output.add(new Organisation(cursor));
            cursor.moveToNext();
        }

        db.close();

        return output;
    }

    public ArrayList<Location> getLocations()
    {
        ArrayList<Location> output = new ArrayList<Location>();

        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getReadableDatabase();
        Cursor cursor = db.query(DatabaseOpenHelper.LOCATIONS_TABLE_NAME,
                new String[]{ "id", "name", "organisation" }, "organisation = " + this.id, null, null, null, "name", null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            output.add(new Location(cursor));
            cursor.moveToNext();
        }

        db.close();

        return output;
    }
}
