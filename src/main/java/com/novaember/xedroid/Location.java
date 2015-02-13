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
    private int organisation;
    private String[] weeks;

    public Location(int id)
    {
        this.id = id;

        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getReadableDatabase();
        Cursor cursor = db.query("locations", new String[]{ "id", "name", "organisation", "weeks" }, "id = " + this.id, null, null, null, "id", null);

        cursor.moveToFirst();
        this.name = cursor.getString(1);
        this.organisation = cursor.getInt(2);
        this.weeks = cursor.getString(3).split(",");
    }

    public Location(int id, String name, int organisation, String[] weeks)
    {
        this.id = id;
        this.name = name;
        this.organisation = organisation;
        this.weeks = weeks;
    }

    public Location(Cursor cursor)
    {
        this.id = cursor.getInt(0);
        this.name = cursor.getString(1);
        this.organisation = cursor.getInt(2);
        this.weeks = cursor.getString(3).split(",");
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Organisation getOrganisation()
    {
        return new Organisation(this.organisation);
    }

    public String[] getWeeks()
    {
        return weeks;
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
        values.put("organisation", organisation);
        values.put("weeks", TextUtils.join(",", weeks));

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
