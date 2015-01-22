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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class WeekScheduleActivity extends ActionBarActivity
{
    private WeekScheduleActivity self;

    private WeekScheduleView weekScheduleView;
    private ProgressBar progressBar;

    private Attendee attendee;
    private int year;
    private int week;

    private WeekAdapter weekAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        self = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekschedule);

        Intent intent = getIntent();
        attendee = new Attendee(intent.getIntExtra("attendeeId", 0));

        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);

        weekScheduleView = (WeekScheduleView) findViewById(R.id.weekschedule);
        progressBar = (ProgressBar) findViewById(R.id.weekschedule_progressbar);

        weekAdapter = new WeekAdapter(this, attendee.getLocation().getWeeks());
        weekAdapter.setTitle(attendee.getName());

        ActionBar.OnNavigationListener weekNavigationListener = new ActionBar.OnNavigationListener()
        {
            @Override
            public boolean onNavigationItemSelected(int position, long itemId)
            {
                Week weekObj = weekAdapter.getItem(position);
                year = weekObj.year;
                week = weekObj.week;

                refresh(false);

                return true;
            }
        };

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(weekAdapter, weekNavigationListener);

        bar.setSelectedNavigationItem(weekAdapter.getCount() - 1);

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

    public void refresh(boolean force)
    {
        weekScheduleView.clear();

        if (force || attendee.getWeekScheduleAge(year, week) == 0)
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
        else
        {
            ArrayList<Event> attendees = attendee.getEvents(year, week);
            progressBar.setVisibility(View.GONE);
            weekScheduleView.addFromArrayList(attendees);
            weekScheduleView.invalidate();
        }
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
            refresh(true);

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

class WeekAdapter extends BaseAdapter
{
    private ArrayList<Week> weeks;
    private String title;
    private static LayoutInflater inflater = null;

    public WeekAdapter(Context context, String[] weeks)
    {
        this.weeks = new ArrayList<Week>();
        for (String week : weeks)
        {
            this.weeks.add(new Week(week));
        }

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Week getItem(int position)
    {
        return weeks.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public int getCount()
    {
        return weeks.size();
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.week_item, parent, false);

        ((TextView) convertView).setText(getItem(position).toShortString());

        return convertView;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.week_dropdown_header, null);

        Week week = getItem(position);

        ((TextView) view.findViewById(R.id.weekschedule_attendee_name)).setText(title);
        ((TextView) view.findViewById(R.id.weekschedule_week)).setText(week.toShortString());

        return view;
    }
}

class Week
{
    public final int year;
    public final int week;

    public Week(String week)
    {
        String[] split = week.split("/");
        if (split.length == 2)
        {
            this.year = Integer.parseInt(split[0]);
            this.week = Integer.parseInt(split[1]);
        }
        else
        {
            this.year = 0;
            this.week = 0;

            Log.w("Xedroid", "Invalid week! " + week);
        }
    }

    public Week(int year, int week)
    {
        this.year = year;
        this.week = week;
    }

    public String toString()
    {
        return year + "/" + week;
    }

    public String toNiceString()
    {
        return year + " week " + week;
    }

    public String toShortString()
    {
        return "Week " + week;
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
