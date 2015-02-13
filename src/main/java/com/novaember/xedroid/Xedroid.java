package com.novaember.xedroid;

import android.app.Application;
import android.content.Context;

public class Xedroid extends Application
{
    private static Context context;

    public void onCreate()
    {
        super.onCreate();
        Xedroid.context = getApplicationContext();
    }

    public static Context getContext() {
        return Xedroid.context;
    }
}
