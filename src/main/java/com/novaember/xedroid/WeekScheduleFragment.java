package com.novaember.xedroid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
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
import android.widget.ProgressBar;
import android.widget.TextView;

public class WeekScheduleFragment extends Fragment implements EventReceiver
{
    private OnEventSelectedListener listener;
    private WeekScheduleView weekScheduleView;
    private Activity activity;

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
                listener.onEventSelected(((WeekScheduleView.EventView) view).getEvent());
            }
        });

        return (View) weekScheduleView;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        this.activity = activity;

        Timer timer = new Timer();
        InvalidateTimer task = new InvalidateTimer(this);
        timer.schedule(task, 60 * 1000, 60 * 1000);
    }

    public void setEvents(final ArrayList<Event> events)
    {
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                weekScheduleView.clear();
                weekScheduleView.setEvents(events);
            }
        });
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
            // activity.invalidateView();
        }
    }
}
