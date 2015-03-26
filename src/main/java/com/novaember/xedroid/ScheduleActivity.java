package com.novaember.xedroid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScheduleActivity extends ActionBarActivity implements WeekScheduleFragment.OnEventSelectedListener
{
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private EventReceiver currentFragment;

    private Attendee attendee;
    private int year;
    private int week;
    private int weekday;

    private boolean refreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_activity);

        Intent intent = getIntent();
        attendee = new Attendee(intent.getIntExtra("attendeeId", 0));
        year = intent.getIntExtra("year", 1970);
        week = intent.getIntExtra("week", 1);
        weekday = intent.getIntExtra("weekday", 1);

        if (attendee.getId() == 0)
        {
            SharedPreferences sharedPref = this.getSharedPreferences("global", Context.MODE_PRIVATE);
            attendee = new Attendee(sharedPref.getInt("myschedule", 0));
        }

        if (attendee.getId() == 0)
        {
            try
            {
                Intent newIntent = new Intent(this, ClassSelectionActivity.class);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(newIntent);
                finish();
            }
            catch(Exception e)
            {
                Log.e("Xedroid", "Error: " + e.getMessage());
            }

            return;
        }

        if (year == 1970 && week == 1)
        {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            week = calendar.get(Calendar.WEEK_OF_YEAR);
            weekday = calendar.get(Calendar.DAY_OF_WEEK);
        }

        WeekScheduleFragment weekScheduleFragment = new WeekScheduleFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.schedule_fragment, weekScheduleFragment).commit();
        currentFragment = weekScheduleFragment;

        resetActionBarTitle();
        refresh(false);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.setDrawerListener(drawerToggle);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
    }

    public void onEventSelected(Event event)
    {
        DayScheduleFragment dayScheduleFragment = new DayScheduleFragment();

        Bundle args = new Bundle();
        args.putInt("attendeeId", attendee.getId());
        args.putInt("year", year);
        args.putInt("week", week);
        args.putInt("day", event.getDay());
        args.putInt("eventId", event.getId());
        dayScheduleFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.schedule_fragment, dayScheduleFragment)
            .addToBackStack(null)
            .commit();

        getSupportActionBar().setElevation(0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void resetActionBarTitle()
    {
        ActionBar bar = getSupportActionBar();
        bar.setTitle(attendee.getName());
        bar.setSubtitle("Week " + week);
    }

    public void refresh(final boolean force)
    {
        if (refreshing) return;
        refreshing = true;

        currentFragment.setWeek(year, week);

        new AsyncTask<Void, Void, Void>()
        {
            protected Void doInBackground(Void... _)
            {
                try
                {
                    Looper.prepare();
                    new Handler();
                }
                catch (Exception e)
                {
                    // TODO: Investigate (Lollipop needs a looper for whatever reason)
                }

                if (force || attendee.getWeekScheduleAge(year, week) == 0)
                {
                    Xedule.updateEvents(attendee.getId(), year, week);
                    Xedule.updateLocations(attendee.getLocation().getOrganisation());
                }

                currentFragment.setEvents(attendee.getEvents(year, week));

                return null;
            }

            protected void onPostExecute(Void _)
            {
                refreshing = false;
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.weekschedule, menu);

        SharedPreferences sharedPref = this.getSharedPreferences("global", Context.MODE_PRIVATE);
        boolean isMine = sharedPref.getInt("myschedule", 0) == attendee.getId();
        MenuItem item = menu.findItem(R.id.weekschedule_star);
        item.setChecked(isMine);
        item.setIcon(isMine ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_outline_white_24dp);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        int id = item.getItemId();

        if (id == R.id.weekschedule_weekselect)
        {
            showDatePickerDialog();

            return true;
        }

        if (id == R.id.weekschedule_star)
        {
            SharedPreferences sharedPref = this.getSharedPreferences("global", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            if (!item.isChecked())
            {
                editor.putInt("myschedule", attendee.getId());

                item.setChecked(true);
                item.setIcon(R.drawable.ic_star_white_24dp);
            }
            else
            {
                editor.putInt("myschedule", 0);

                item.setChecked(false);
                item.setIcon(R.drawable.ic_star_outline_white_24dp);
            }

            editor.commit();

            return true;
        }

        if (id == R.id.weekschedule_refresh)
        {
            refresh(true);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showDatePickerDialog()
    {
        DialogFragment dialog = new DatePickerFragment();
        dialog.show(getSupportFragmentManager(), "datePicker");
    }

    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            // Calculate default selected date
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.WEEK_OF_YEAR, week);
            c.set(Calendar.DAY_OF_WEEK, weekday);

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Get dialog's DatePicker
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            DatePicker picker = dialog.getDatePicker();

            // Calculate minimum date
            Calendar min = (Calendar) c.clone();
            min.clear();
            min.set(Calendar.YEAR, 2014);
            min.set(Calendar.WEEK_OF_YEAR, 35);

            // Calculate maximum date
            Calendar max = Calendar.getInstance();
            max.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

            // Configure picker
            picker.setMinDate(min.getTimeInMillis());
            picker.setMaxDate(max.getTimeInMillis());

            return dialog;
        }

        public void onDateSet(DatePicker view, int year_, int month, int day)
        {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(Calendar.YEAR, year_);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);

            year = year_;
            week = c.get(Calendar.WEEK_OF_YEAR);
            weekday = c.get(Calendar.DAY_OF_WEEK);

            refresh(false);
        }
    }
}
