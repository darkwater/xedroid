package com.novaember.xedroid;

import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseOpenHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "xedroid";
    public static final int DATABASE_VERSION = 1;

    private Context context;

    DatabaseOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            InputStream inputStream = context.getResources().openRawResource(R.raw.init_sql);
            byte[] reader = new byte[inputStream.available()];
            while (inputStream.read(reader) != -1) {}

            String[] queries = new String(reader).split("\n-\n");
            for (String query : queries) db.execSQL(query);
        }
        catch (Exception e)
        {
            Log.e("Xedroid", "Could not create database.", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }
}
