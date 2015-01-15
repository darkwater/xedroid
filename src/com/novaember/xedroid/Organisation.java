package com.novaember.xedroid;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

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

    public Organisation(JSONObject json) throws JSONException
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
        if (name != null) return name;

        try
        {
            JSONArray organisations = Xedule.getArray("organisations.json");

            for (int i = 0; i < organisations.length(); i++)
            {
                if (organisations.getJSONObject(i).getInt("id") == this.id)
                {
                    this.name = organisations.getJSONObject(i).getString("name");
                    break;
                }
            }
        }
        catch (JSONException e)
        {
            Log.e("Xedroid", "Could not get name of Organisation #" + this.id, e);
            this.name = "???";
        }

        return name;
    }

    @Override
    public int compareTo(Organisation org)
    {
        return this.name.compareTo(org.name);
    }

    public static ArrayList<Organisation> getAll()
    {
        ArrayList<Organisation> organisationsArrayList = new ArrayList<Organisation>();

        try
        {
            JSONArray organisationsJSONArray = Xedule.getArray("organisations.json");

            for (int i = 0; i < organisationsJSONArray.length(); i++)
            {
                JSONObject obj = organisationsJSONArray.getJSONObject(i);

                organisationsArrayList.add(new Organisation(obj.getInt("id"), obj.getString("name")));
            }
        }
        catch(JSONException e)
        {
            Log.e("Xedule", "Couldn't get all organisations", e);
        }

        return organisationsArrayList;
    }

    public ArrayList<Location> getLocations()
    {
        ArrayList<Location> locationsArrayList = new ArrayList<Location>();

        try
        {
            JSONArray locationsJSONArray = Xedule.getArray("locations." + this.id + ".json");

            for (int i = 0; i < locationsJSONArray.length(); i++)
            {
                JSONObject obj = locationsJSONArray.getJSONObject(i);

                locationsArrayList.add(new Location(obj));
            }
        }
        catch(JSONException e)
        {
            Log.e("Xedule", "Couldn't get locations for (" + this.id + ") " + this.name, e);
        }

        return locationsArrayList;
    }
}
