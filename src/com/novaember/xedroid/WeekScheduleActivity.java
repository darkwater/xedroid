package com.novaember.xedroid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

public class WeekScheduleActivity extends ActionBarActivity
{
    private WeekScheduleActivity self;

    private WeekScheduleView weekScheduleView;
    private ProgressBar progressBar;

    private Attendee attendee;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        self = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekschedule);

        Intent intent = getIntent();
        attendee = new Attendee(intent.getIntExtra("attendeeId", 0));

        ActionBar bar = getSupportActionBar();
        bar.setTitle(attendee.getName());
        bar.setDisplayHomeAsUpEnabled(true);

        weekScheduleView = (WeekScheduleView) findViewById(R.id.weekschedule);
        progressBar = (ProgressBar) findViewById(R.id.weekschedule_progressbar);

        final int year = 2015;
        final int week = 4;

        if (attendee.getWeekScheduleAge(year, week) == 0)
        {
            weekScheduleView.setVisibility(View.GONE);

            new AsyncTask<Void, Void, ArrayList<Event>>()
            {
                protected ArrayList<Event> doInBackground(Void... _)
                {
                    Xedule.updateEvents(attendee.getId(), year, week);
                    return attendee.getEvents(year, week);
                }

                protected void onPostExecute(ArrayList<Event> atts)
                {
                    weekScheduleView.addFromArrayList(atts);
                    progressBar.setVisibility(View.GONE);
                    weekScheduleView.setVisibility(View.VISIBLE);
                    invalidateView();
                }
            }.execute();
        }
        else
        {
            ArrayList<Event> attendees = attendee.getEvents(year, week);
            progressBar.setVisibility(View.GONE);
            weekScheduleView.addFromArrayList(attendees);
        }

        Timer timer = new Timer();
        InvalidateTimer task = new InvalidateTimer(this);
        timer.schedule(task, 60 * 1000, 60 * 1000);

//        WeekScheduleView weekScheduleView = (WeekScheduleView) findViewById(R.id.weekSchedule);
//        weekScheduleView.setAdapter(weekSchedule);
//
//        weekScheduleView.setOnItemClickListener(new OnItemClickListener()
//        {
//            public void onItemClick(AdapterView listview, View view, int pos, long id)
//            {
//                try
//                {
//                    WeekSchedule org = (WeekSchedule) listview.getAdapter().getItem(pos);
//                    Intent intent = new Intent(self, LocationsActivity.class);
//                    intent.putExtra("weekScheduleId", org.id);
//                    intent.putExtra("weekScheduleName", org.name);
//                    startActivity(intent);
//                }
//                catch(Exception e)
//                {
//                    Log.e("Xedroid", "Error: " + e.getMessage());
//                }
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will automatically
        // handle clicks on the Home/Up button, so long as you specify a parent
        // activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void invalidateView()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                weekScheduleView.invalidate();
            }
        });
    }
}

final class InvalidateTimer extends TimerTask
{
    WeekScheduleActivity activity;

    public InvalidateTimer(WeekScheduleActivity activity)
    {
        this.activity = activity;
    }

    @Override
    public void run()
    {
        activity.invalidateView();
    }
}
