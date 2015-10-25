package com.novaember.xedroid;

import java.lang.Comparable;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

public class Event implements Comparable<Event>
{
    private int id;
    private int year;
    private int week;
    private int day;
    private Time start;
    private Time end;
    private String description;
    private ArrayList<Attendee> attendees;
    private int color;

    public Event(int id)
    {
        this.id = id;

        SQLiteDatabase db = Xedroid.getWritableDatabase();
        Cursor cursor = db.query("events", new String[]{ "id", "year", "week", "day", "start", "end", "description" }, "id = " + this.id, null, null, null, "id", null);

        cursor.moveToFirst();
        this.year = cursor.getInt(1);
        this.week = cursor.getInt(2);
        this.day = cursor.getInt(3);
        this.start = new Time(cursor.getString(4));
        this.end = new Time(cursor.getString(5));
        this.description = cursor.getString(6);
    }

    public Event(int year, int week, int day, Time start, Time end, String description)
    {
        this.year = year;
        this.week = week;
        this.day = day;
        this.start = start;
        this.end = end;
        this.description = description;
        this.attendees = new ArrayList<Attendee>();
    }

    public Event(Cursor cursor)
    {
        this.id = cursor.getInt(0);
        this.year = cursor.getInt(1);
        this.week = cursor.getInt(2);
        this.day = cursor.getInt(3);
        this.start = new Time(cursor.getString(4));
        this.end = new Time(cursor.getString(5));
        this.description = cursor.getString(6);
    }

    public int getId()
    {
        return id;
    }

    public int getYear()
    {
        return year;
    }

    public int getWeek()
    {
        return week;
    }

    public int getDay()
    {
        return day;
    }

    public Time getStart()
    {
        return start;
    }

    public Time getEnd()
    {
        return end;
    }

    public String getDescription()
    {
        return description;
    }

    public String getAbbreviation()
    {
        return description.substring(0, Math.min(description.length(), 3));
    }

    public ArrayList<Attendee> getAttendees()
    {
        if (attendees == null)
        {
            ArrayList<Attendee> output = new ArrayList<Attendee>();

            SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getReadableDatabase();
            Cursor cursor = db.query("attendee_events_view",
                    new String[]{ "attendee", "name", "location", "type" }, "event = " + this.id, null, null, null, "event", null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                output.add(new Attendee(cursor));
                cursor.moveToNext();
            }

            db.close();

            return output;
        }

        return attendees;
    }

    public ArrayList<Attendee> getByType(Attendee.Type type)
    {
        ArrayList<Attendee> output = new ArrayList<Attendee>();

        for (Attendee attendee : getAttendees())
        {
            if (attendee.getType() == type)
            {
                output.add(attendee);
            }
        }

        return output;
    }

    public ArrayList<Attendee> getClasses()
    {
        return getByType(Attendee.Type.CLASS);
    }

    public ArrayList<Attendee> getStaffs()
    {
        return getByType(Attendee.Type.STAFF);
    }

    public ArrayList<Attendee> getFacilities()
    {
        return getByType(Attendee.Type.FACILITY);
    }

    public int getColor()
    {
        if (color != 0) return color;

        ArrayList<Attendee> source = null;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Xedroid.getContext());
        switch (sharedPref.getString("pref_schedule_color_key", "facility"))
        {
            case "class":
                source = getClasses();
                break;
            case "staff":
                source = getStaffs();
                break;
            case "facility":
                source = getFacilities();
                break;
        }

        if (source == null || source.isEmpty())
        {
            color = 0xff888888;
            return color;
        }

        String in = source.get(0).getName();
        int sum = 0;

        for (int i = 0; i < in.length(); i++)
        {
            sum += in.charAt(i) * (i + 1);
        }

        int hue = (int) (sum * Math.PI * 1000) % 360;

        color = Color.HSVToColor(new float[]{ hue, 0.95f, 0.95f });

        return color;
    }

    public void addAttendee(Attendee attendee)
    {
        this.attendees.add(attendee);
    }

    @Override
    public int compareTo(Event event)
    {
        return start.compareTo(event.getStart());
    }

    public void save(SQLiteDatabase db)
    {
        ContentValues values = new ContentValues();
        if (id != 0) values.put("id", id);
        values.put("year",        year);
        values.put("week",        week);
        values.put("day",         day);
        values.put("description", description);
        values.put("start",       start.toString());
        values.put("end",         end.toString());

        this.id = (int) db.insertWithOnConflict("events", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        values.clear();
        values.put("event", this.id);
        for (Attendee attendee : attendees)
        {
            values.put("attendee", attendee.getId());
            db.insertWithOnConflict("attendee_events", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public void save()
    {
        SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getWritableDatabase();
        save(db);
        db.close();
    }

    public static class Time implements Comparable<Time>
    {
        private int hour;
        private int minute;

        public Time(String str)
        {
            String[] split = str.split(":");

            this.hour = Integer.parseInt(split[0]);
            this.minute = split.length > 1 ? Integer.parseInt(split[1]) : 0;
        }

        public int getHour()
        {
            return hour;
        }

        public int getMinute()
        {
            return minute;
        }

        public float toFloat()
        {
            return hour + (float) minute / 60.f;
        }

        public String toString()
        {
            return hour + ":" + (minute < 10 ? "0" : "") + minute;
        }

        public int compareTo(Time t)
        {
            return java.lang.Float.compare(toFloat(), t.toFloat());
        }
    }
}
