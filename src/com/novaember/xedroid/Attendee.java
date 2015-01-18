package com.novaember.xedroid;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Attendee implements Comparable<Attendee>
{
    private int id;
    private String name;
    private int location;
    private Type type;

    public enum Type
    {
        CLASS    (1, R.string.attendee_type_class),
        STAFF    (2, R.string.attendee_type_staff),
        FACILITY (3, R.string.attendee_type_facility);

        public final int id;
        public final int label;

        private Type(int id, int label)
        {
            this.id = id;
            this.label = label;
        }

        public static Type getById(int id)
        {
            switch (id)
            {
                case 1:  return Type.CLASS;
                case 2:  return Type.STAFF;
                case 3:  return Type.FACILITY;
                default: return Type.CLASS;
            }
        }
    }

    public Attendee(int id)
    {
        this.id = id;
    }

    public Attendee(String name, SQLiteDatabase db)
    {
        this.name = name;

        Cursor cursor = db.query("attendees", new String[]{ "id", "name", "location", "type" }, "name = ?", new String[]{ this.name }, null, null, "id", null);

        cursor.moveToFirst();
        this.id = cursor.getInt(0);
        this.location = cursor.getInt(2);
        this.type = Type.getById(cursor.getInt(3));
    }

    public Attendee(int id, String name, int location, int type)
    {
        this.id = id;
        this.name = name;
        this.location = location;

        try
        {
            this.type = Type.getById(type);
        }
        catch (Exception e)
        {
            Log.e("Xedule", "Error: " + e.getMessage());
        }
    }

    public Attendee(Cursor cursor)
    {
        this.id = cursor.getInt(0);
        this.name = cursor.getString(1);
        this.location = cursor.getInt(2);

        try
        {
            this.type = Type.getById(cursor.getInt(3));
        }
        catch (Exception e)
        {
            Log.e("Xedule", "Error: " + e.getMessage());
        }
    }

    public void populate()
    {
        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getReadableDatabase();
        Cursor cursor = db.query("attendees", new String[]{ "id", "name", "location", "type" }, "id = " + this.id, null, null, null, "id", null);

        cursor.moveToFirst();
        this.name = cursor.getString(1);
        this.location = cursor.getInt(2);
        this.type = Type.getById(cursor.getInt(3));
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

    public Location getLocation()
    {
        if (location == 0) populate();

        return new Location(location);
    }

    public Type getType()
    {
        if (type == null) populate();

        return type;
    }

    @Override
    public int compareTo(Attendee att)
    {
        return this.name.compareTo(att.name);
    }

    public void save(SQLiteDatabase db)
    {
        ContentValues values = new ContentValues();
        values.put("id", this.id);
        values.put("name", this.name);
        values.put("location", this.location);
        values.put("type", this.type.id);

        db.insertWithOnConflict("attendees", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void save()
    {
        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getWritableDatabase();
        save(db);
        db.close();
    }

    public ArrayList<Event> getEvents()
    {
        ArrayList<Event> output = new ArrayList<Event>();

        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getReadableDatabase();
        Cursor cursor = db.query("attendee_events_view",
                new String[]{ "event", "year", "week", "day", "start", "end", "description" }, "attendee = " + this.id, null, null, null, "event", null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            output.add(new Event(cursor));
            cursor.moveToNext();
        }

        db.close();

        return output;
    }
}
