package com.novaember.xedroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Xedule
{
    private int cacheTimeout = 60; // in seconds
    private static String apiSite = "http://xedule.novaember.com/";

    public static JSONArray getArray(String location) throws JSONException
    {
        return new JSONArray(get(location));
    }

    public static JSONObject getObject(String location) throws JSONException
    {
        return new JSONObject(get(location));
    }

    private static String get(String location)
    {
        File cacheFile = new File(Xedroid.getContext().getCacheDir(), location);
        String output = null;

        if (cacheFile.exists()) try
        {
            BufferedReader cacheReader = new BufferedReader(new FileReader(cacheFile));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = cacheReader.readLine()) != null) sb.append(line + "\n");

            cacheReader.close();

            if (sb.length() > 0)
            {
                output = sb.toString();
            }
        }
        catch(Exception e) {Log.d("Xedule", "aa", e);} // Do nothing

        if (output == null) try
        {
            output = Fetcher.downloadUrl(apiSite + location);

            FileWriter cacheWriter = new FileWriter(cacheFile);
            cacheWriter.write(output);
            cacheWriter.close();
        }
        catch (IOException e)
        {
            Log.w("Xedule", "Could not write cache file", e);
        }

        return output;
    }

    public static void updateOrganisations()
    {
        try
        {
            JSONArray organisationsJSONArray = Xedule.getArray("organisations.json");

            for (int i = 0; i < organisationsJSONArray.length(); i++)
            {
                JSONObject obj = organisationsJSONArray.getJSONObject(i);

                new Organisation(obj.getInt("id"), obj.getString("name")).save();
            }
        }
        catch(JSONException e)
        {
            Log.e("Xedule", "Couldn't update organisations", e);
        }
    }

    public static void updateLocations(int organisation)
    {
        try
        {
            JSONArray locationsJSONArray = Xedule.getArray("locations." + organisation + ".json");

            for (int i = 0; i < locationsJSONArray.length(); i++)
            {
                JSONObject obj = locationsJSONArray.getJSONObject(i);

                new Location(obj.getInt("id"), obj.getString("name"), organisation).save();
            }
        }
        catch(JSONException e)
        {
            Log.e("Xedule", "Couldn't update locations for organisation #" + organisation, e);
        }
    }

    public static void updateAttendees(int location)
    {
        try
        {
            JSONArray attendeesJSONArray = Xedule.getArray("attendees." + location + ".json");

            for (int i = 0; i < attendeesJSONArray.length(); i++)
            {
                JSONObject obj = attendeesJSONArray.getJSONObject(i);

                new Attendee(obj.getInt("id"), obj.getString("name"), location, obj.getInt("type")).save();
            }
        }
        catch(JSONException e)
        {
            Log.e("Xedule", "Couldn't update attendees for location #" + location, e);
        }
    }
}
