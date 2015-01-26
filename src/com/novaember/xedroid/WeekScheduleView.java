package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WeekScheduleView extends LinearLayout
{
    private float startHour = 8.5f;
    private float endHour = 16.f;
    private int startDay = 1;
    private int endDay = 5;

    private float hourHeight = 64; // dp

    private boolean currentWeek = false;

    private ArrayList<EventView> events;
    private ArrayList<AbsoluteLayout> dayColumns;

    public WeekScheduleView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        inflate(context, R.layout.weekscheduleview, this);

        events = new ArrayList<EventView>();
    }

    public void addEvent(Event event)
    {
        EventView eventView = new EventView(this.getContext(), null, event);

        events.add(eventView);

        ((ViewGroup)
        ((ViewGroup) findViewById(R.id.weekschedule_daycolumns))
            .getChildAt(event.getDay()))
            .addView(eventView);

        float height = getPx((event.getEnd().getFloat() - event.getStart().getFloat()) * hourHeight + 1);
        float y = getPx((event.getStart().getFloat() - startHour) * hourHeight);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) height);
        params.topMargin = (int) y;

        ((TextView) eventView.findViewById(R.id.weekschedule_event_primary_text)).setText(event.getDescription());

        if (event.getFacilities().size() >= 1)
            ((TextView) eventView.findViewById(R.id.weekschedule_event_secondary_text)).setText(event.getFacilities().get(0).getName());

        eventView.findViewById(R.id.weekschedule_event_color).setBackgroundColor(event.getColor());

        eventView.setLayoutParams(params);
    }

    public void clear()
    {
        events.clear();

        for (int i = 1; i <= 5; i++)
        {
            ViewGroup column = ((ViewGroup) ((ViewGroup)
                        findViewById(R.id.weekschedule_daycolumns))
                        .getChildAt(i));

            column.removeViews(1, column.getChildCount() - 1);
        }
    }
    
    public void addFromArrayList(ArrayList<Event> input, ProgressBar progressBar)
    {
        progressBar.setMax(input.size());
        progressBar.setIndeterminate(false);
        int progress = 0;

        for (Event event : input)
        {
            addEvent(event);

            progressBar.setProgress(++progress);
        }
    }

    public void setCurrentWeek(boolean currentWeek)
    {
        this.currentWeek = currentWeek;
    }

    private float getPx(float x)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, getResources().getDisplayMetrics());
    }

    public static class EventView extends RelativeLayout
    {
        private Event event;

        public EventView(Context context, AttributeSet attrs, Event event)
        {
            super(context, attrs);

            inflate(context, R.layout.weekschedule_event, this);

            this.event = event;
        }

        public Event getEvent()
        {
            return event;
        }
    }
}
