package com.novaember.xedroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

public class WeekScheduleFragment extends Fragment implements EventReceiver
{
    private OnEventSelectedListener listener;
    private WeekScheduleView weekScheduleView;
    private Attendee attendee;
    private int year;
    private int week;
    private int weekday;

    private boolean refreshing;

    public interface OnEventSelectedListener
    {
        public void onEventSelected(Event event);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        weekScheduleView = (WeekScheduleView) inflater.inflate(R.layout.weekschedule_fragment, container, false);
        weekScheduleView.setWeek(year, week);

        weekScheduleView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                if (listener != null) listener.onEventSelected(((WeekScheduleView.EventView) view).getEvent());
                else Log.w("Xedroid", "WeekScheduleFragment's listener is null!");
            }
        });

        return (View) weekScheduleView;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            listener = (OnEventSelectedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnEventSelectedListener");
        }

        Timer timer = new Timer();
        InvalidateTimer task = new InvalidateTimer(this);
        timer.schedule(task, 60 * 1000, 60 * 1000);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        ((ScheduleActivity) getActivity()).refresh(false);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        ((ActionBarActivity) getActivity()).getSupportActionBar().setElevation(getPx(8));
    }

    private float getPx(float x)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, getResources().getDisplayMetrics());
    }

    public void setEvents(final ArrayList<Event> events)
    {
        weekScheduleView.clear();
        weekScheduleView.setEvents(events);
    }

    public void setWeek(int year, int week)
    {
        this.year = year;
        this.week = week;

        if (weekScheduleView != null) weekScheduleView.setWeek(year, week);
    }

    private class InvalidateTimer extends TimerTask
    {
        WeekScheduleFragment fragment;

        public InvalidateTimer(WeekScheduleFragment fragment)
        {
            this.fragment = fragment;
        }

        @Override
        public void run()
        {
        }
    }
}
