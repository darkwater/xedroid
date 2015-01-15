package com.novaember.xedroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
}
