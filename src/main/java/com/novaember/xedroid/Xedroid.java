package com.novaember.xedroid;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Xedroid extends Application
{
    private static Context context;
    private static SQLiteDatabase writableDatabase;

    public void onCreate()
    {
        super.onCreate();
        Xedroid.context = getApplicationContext();
        Xedroid.writableDatabase = new DatabaseOpenHelper(Xedroid.getContext()).getWritableDatabase();
    }

    public static Context getContext() {
        return Xedroid.context;
    }

    public static SQLiteDatabase getWritableDatabase() {
        return Xedroid.writableDatabase;
    }
}
