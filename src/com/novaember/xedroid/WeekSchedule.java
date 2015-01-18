package com.novaember.xedroid;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WeekSchedule
{
    private int attendee;
    private int year;
    private int week;
    private ArrayList<Event> events;

    public WeekSchedule(int attendee, int year, int week)
    {
        this.attendee = attendee;
        this.year = year;
        this.week = week;
        this.events = new ArrayList<Event>();
    }

    public void addEvent(Event event)
    {
        events.add(event);
    }

//  public ArrayList<Event> getEvents()
//  {
//      ArrayList<Event> output = new ArrayList<Event>();

//      SQLiteDatabase db = new DatabaseOpenHelper(Xedroid.getContext()).getReadableDatabase();
//      Cursor cursor = db.query("attendee_events_name",
//              new String[]{ "attendee", "event", "year", "week", "day", "description", "start", "end" }, "organisation = " + this.attendee, null, null, null, "name", null);

//      cursor.moveToFirst();
//      while (!cursor.isAfterLast())
//      {
//          output.add(new Location(cursor));
//          cursor.moveToNext();
//      }

//      db.close();

//      return output;
//  }
}
