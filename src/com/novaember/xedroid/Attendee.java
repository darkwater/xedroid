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

        public static Type getById(int id) throws Exception
        {
            switch (id)
            {
                case 1:  return Type.CLASS;
                case 2:  return Type.STAFF;
                case 3:  return Type.FACILITY;
                default: throw new Exception("Invalid attendee type: " + String.valueOf(id));
            }
        }
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

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Location getLocation()
    {
        return new Location(location);
    }

    public Type getType()
    {
        return type;
    }

    @Override
    public int compareTo(Attendee att)
    {
        return this.name.compareTo(att.name);
    }

    public void save()
    {
        ContentValues values = new ContentValues();
        values.put("id", this.id);
        values.put("name", this.name);
        values.put("location", this.location);
        values.put("type", this.type.id);

        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getWritableDatabase();
        db.insertWithOnConflict(DatabaseOpenHelper.ATTENDEES_TABLE_NAME,
                null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
}
