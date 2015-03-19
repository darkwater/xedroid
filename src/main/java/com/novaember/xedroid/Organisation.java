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
    }

    public Organisation(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public Organisation(Cursor cursor)
    {
        id = cursor.getInt(0);
        name = cursor.getString(1);
    }

    public boolean populate()
    {
        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getReadableDatabase();
        Cursor cursor = db.query("organisations", new String[]{ "id", "name" }, "id = " + id, null, null, null, "id", null);

        if (cursor == null || cursor.getCount() == 0) return false;

        cursor.moveToFirst();
        name = cursor.getString(1);

        return true;
    }
    public int getId()
    {
        return id;
    }

    public String getName()
    {
        if (name == null) populate();

        return name;
    }

    public String toString()
    {
        return getName();
    }

    @Override
    public int compareTo(Organisation org)
    {
        return getName().compareTo(org.getName());
    }

    public void save(SQLiteDatabase db)
    {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);

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
        Cursor cursor = db.query("locations", new String[]{ "id", "name", "organisation", "weeks" }, "organisation = " + id, null, null, null, "name", null);

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
