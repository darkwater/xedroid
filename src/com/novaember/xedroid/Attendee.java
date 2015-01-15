package com.novaember.xedroid;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Attendee implements Comparable<Attendee>
{
    private int id;
    private String name;
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

    public Attendee(int id, String name, int type)
    {
        this.id = id;
        this.name = name;

        try
        {
            this.type = Type.getById(type);
        }
        catch (Exception e)
        {
            Log.e("Xedule", "Error: " + e.getMessage());
        }
    }

    public Attendee(JSONObject json) throws JSONException
    {
        this(json.getInt("id"), json.getString("name"), json.getInt("type"));
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
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

//  public static ArrayList<Attendee> getAttendees()
//  {
//      ArrayList<Attendee> attendeesArrayList = new ArrayList<Attendee>();

//      try
//      {
//          JSONArray attendeesJSONArray = Xedule.getArray("attendees." + this.id + ".json");

//          for (int i = 0; i < attendeesJSONArray.length(); i++)
//          {
//              JSONObject obj = attendeesJSONArray.getJSONObject(i);

//              attendeesArrayList.add(new Attendee(obj.getInt("id"), obj.getString("name")));
//          }
//      }
//      catch(JSONException e)
//      {
//          Log.e("Xedule", "Couldn't get all attendees", e);
//      }

//      return attendeesArrayList;
//  }
}
