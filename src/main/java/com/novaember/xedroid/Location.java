package com.novaember.xedroid;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class Location implements Comparable<Location>
{
    private int id;
    private String name;
    private Organisation organisation;

    public Location(int id)
    {
        this.id = id;
    }

    public Location(int id, String name, Organisation organisation)
    {
        this.id = id;
        this.name = name;
        this.organisation = organisation;
    }

    public Location(Cursor cursor)
    {
        this.id = cursor.getInt(0);
        this.name = cursor.getString(1);
        this.organisation = new Organisation(cursor.getInt(2));
    }

    public boolean populate()
    {
        SQLiteDatabase db = Xedroid.getWritableDatabase();
        Cursor cursor = db.query("locations", new String[]{ "id", "name", "organisation" }, "id = " + this.id, null, null, null, "id", null);

        if (cursor == null || cursor.getCount() == 0) return false;

        cursor.moveToFirst();
        name = cursor.getString(1);
        organisation = new Organisation(cursor.getInt(2));

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

    public Organisation getOrganisation()
    {
        if (organisation == null) populate();

        return organisation;
    }

    public String toString()
    {
        return getName();
    }

    @Override
    public int compareTo(Location loc)
    {
        return this.name.compareTo(loc.name);
    }

    public void save(SQLiteDatabase db)
    {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("organisation", organisation.getId());

        db.insertWithOnConflict("locations", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void save()
    {
        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getWritableDatabase();
        save(db);
        db.close();
    }

    public ArrayList<Attendee> getAttendees()
    {
        ArrayList<Attendee> output = new ArrayList<Attendee>();

        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getReadableDatabase();
        Cursor cursor = db.query("attendees", new String[]{ "id", "name", "location", "type" }, "location = " + this.id, null, null, null, "name", null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            output.add(new Attendee(cursor));
            cursor.moveToNext();
        }

        db.close();

        return output;
    }
}
