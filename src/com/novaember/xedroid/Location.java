package com.novaember.xedroid;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Location implements Comparable<Location>
{
    private int id;
    private String name;

    public Location(int id)
    {
        this.id = id;
        this.name = "???";
    }

    public Location(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public Location(JSONObject json) throws JSONException
    {
        this.id = json.getInt("id");
        this.name = json.getString("name");
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public int compareTo(Location loc)
    {
        return this.name.compareTo(loc.name);
    }

    public ArrayList<Attendee> getAttendees()
    {
        ArrayList<Attendee> attendeesArrayList = new ArrayList<Attendee>();

        try
        {
            JSONArray attendeesJSONArray = Xedule.getArray("attendees." + this.id + ".json");

            for (int i = 0; i < attendeesJSONArray.length(); i++)
            {
                JSONObject obj = attendeesJSONArray.getJSONObject(i);

                attendeesArrayList.add(new Attendee(obj));
            }
        }
        catch(JSONException e)
        {
            Log.e("Xedule", "Couldn't get all attendees", e);
        }

        return attendeesArrayList;
    }
}
