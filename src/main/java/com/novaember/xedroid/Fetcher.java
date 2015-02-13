package com.novaember.xedroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class Fetcher
{
    public static String downloadUrl(String urlstr)
    {
        Log.d("Xedule", "Downloading " + urlstr);

        InputStream is = null;

        try {
            URL url = new URL(urlstr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Start the query
            conn.connect();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readStream(is);
            return contentAsString;
        }
        catch (Exception e)
        {
            Log.e("Xedule", "Error downloading " + urlstr, e);
            return "";
        }
        finally
        {
            try
            {
                if (is != null)
                    is.close();
            }
            catch (Exception e) {}
        }
    }

    private static String readStream(InputStream in)
    {
        BufferedReader reader = null;

        try
        {
            reader = new BufferedReader(new InputStreamReader(in));

            String line;
            String out = "";
            while ((line = reader.readLine()) != null)
            {
                out += line;
            }

            return out;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }
}
