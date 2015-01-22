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

import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
            refresh(year, week);
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

    public void refresh(final int year, final int week)
    {
        weekScheduleView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.weekschedule, menu);

        SharedPreferences sharedPref = this.getSharedPreferences("global", Context.MODE_PRIVATE);
        boolean isMine = sharedPref.getInt(getString(R.string.preference_myschedule_key), 0) == attendee.getId();
        MenuItem item = menu.findItem(R.id.weekschedule_star);
        item.setChecked(isMine);
        item.setIcon(isMine ? R.drawable.ic_star_white_48dp : R.drawable.ic_star_outline_white_48dp);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will automatically
        // handle clicks on the Home/Up button, so long as you specify a parent
        // activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.weekschedule_star)
        {
            SharedPreferences sharedPref = this.getSharedPreferences("global", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            if (!item.isChecked())
            {
                editor.putInt(getString(R.string.preference_myschedule_key), attendee.getId());

                item.setChecked(true);
                item.setIcon(R.drawable.ic_star_white_48dp);
            }
            else
            {
                editor.putInt(getString(R.string.preference_myschedule_key), 0);

                item.setChecked(false);
                item.setIcon(R.drawable.ic_star_outline_white_48dp);
            }

            editor.commit();

            return true;
        }

        if (id == R.id.weekschedule_refresh)
        {
            refresh(2015, 4);

            return true;
        }

        if (id == R.id.action_settings)
        {
            return true;
        }

        if (id == android.R.id.home)
        {
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.putExtra("locationId", attendee.getLocation().getId());
            if (NavUtils.shouldUpRecreateTask(this, upIntent))
            {
                // This activity is NOT part of this app's task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                        // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                        // Navigate up to the closest parent
                        .startActivities();
            }
            else
            {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                Intent intent = new Intent(self, AttendeesActivity.class);
                intent.putExtra("locationId", attendee.getLocation().getId());
                startActivity(intent);
            }
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
