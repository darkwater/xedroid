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
        Cursor cursor = db.query("organisations", new String[]{ "id", "name" }, "id = " + this.id, null, null, null, "id", null);

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

    public void save(SQLiteDatabase db)
    {
        ContentValues values = new ContentValues();
        values.put("id", this.id);
        values.put("name", this.name);

        db.insertWithOnConflict("organisations", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void save()
    {
        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getWritableDatabase();
        save(db);
        db.close();
    }

    public static ArrayList<Organisation> getAll()
    {
        ArrayList<Organisation> output = new ArrayList<Organisation>();

        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getReadableDatabase();
        Cursor cursor = db.query("organisations", new String[]{ "id", "name" }, null, null, null, null, "name", null);

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
        Cursor cursor = db.query("locations", new String[]{ "id", "name", "organisation", "weeks" }, "organisation = " + this.id, null, null, null, "name", null);

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
