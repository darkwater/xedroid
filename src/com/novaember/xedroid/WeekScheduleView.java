package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class WeekScheduleView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener
{
    private float startHour = 8.5f;
    private float endHour = 16.f;
    private int startDay = 1;
    private int endDay = 5;

    private float hourHeight = 64; // dp

    private boolean currentWeek = false;

    private final Context context;
    private final WeekScheduleView self;

    private ArrayList<EventView> events;
    private ArrayList<ViewGroup> dayColumns;

    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    public WeekScheduleView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        inflate(context, R.layout.weekscheduleview, this);

        this.context = context;
        this.self = this;

        events = new ArrayList<EventView>();
        dayColumns = new ArrayList<ViewGroup>();

        for (int i = 0; i < 6; i++)
        {
            dayColumns.add(
                    ((ViewGroup)
                    ((ViewGroup)
                        findViewById(R.id.weekschedule_daycolumns))
                        .getChildAt(i)));

            Log.d("Xedroid", i + ": " + dayColumns.get(i));
        }

        for (int h = (int) Math.ceil(startHour); h <= endHour; h++)
        {
            View marker = inflate(context, R.layout.weekschedule_hourmarker, null);
            ((TextView) marker.findViewById(R.id.weekschedule_hourmarker_label)).setText(h + "h");
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.topMargin = (int) getPx(((h - startHour) * hourHeight));
            marker.setLayoutParams(params);
            dayColumns.get(0).addView(marker);
        }
    }

    public void addEvent(final Event event)
    {
        ((Activity) context).runOnUiThread(new Runnable() { public void run()
        {
            EventView eventView = new EventView(context, null, event);

            events.add(eventView);
            dayColumns.get(event.getDay()).addView(eventView);

            float height = getPx((event.getEnd().getFloat() - event.getStart().getFloat()) * hourHeight + 1);
            float y = getPx((event.getStart().getFloat() - startHour) * hourHeight);

            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, (int) height);
            params.topMargin = (int) y;

            ((TextView) eventView.findViewById(R.id.weekschedule_event_primary_text)).setText(event.getAbbreviation());

            if (event.getFacilities().size() >= 1)
                ((TextView) eventView.findViewById(R.id.weekschedule_event_secondary_text)).setText(event.getFacilities().get(0).getName());

            eventView.findViewById(R.id.weekschedule_event_color).setBackgroundColor(event.getColor());

            eventView.setLayoutParams(params);
            eventView.setOnClickListener(self);
            eventView.setOnLongClickListener(self);
        } });
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

    public void setOnClickListener(View.OnClickListener onClickListener)
    {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener)
    {
        this.onLongClickListener = this.onLongClickListener;
    }

    public void onClick(View view)
    {
        if (onClickListener != null) onClickListener.onClick(view);
    }

    public boolean onLongClick(View view)
    {
        if (onLongClickListener != null) return onLongClickListener.onLongClick(view);

        return false;
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

            this.setBackgroundColor(0xffffffff);
        }

        public Event getEvent()
        {
            return event;
        }
    }
}
