package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.Calendar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class WeekScheduleView extends View
{
    private Paint linePaint;
    private Paint backgroundPaint;
    private Paint headerTextPaint;
    private Paint sideTextPaint;
    private Paint timeLinePaint;

    private Paint eventPaint;
    private Paint eventTextPaint;
    private Paint eventColorPaint;

    private float startHour = 8.5f;
    private float endHour = 16.f;
    private int startDay = 1;
    private int endDay = 5;

    private ArrayList<Event> events;

    public WeekScheduleView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        backgroundPaint = new Paint(0);
        backgroundPaint.setColor(0xffe0e0e0);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xffa0a0a0); // TODO: Get system resource
        linePaint.setStrokeWidth(2);

        headerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerTextPaint.setColor(0xff888888);
        headerTextPaint.setTextAlign(Align.CENTER);

        sideTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sideTextPaint.setColor(0xff888888);
        sideTextPaint.setTextAlign(Align.RIGHT);

        timeLinePaint = new Paint(0);
        timeLinePaint.setColor(0x60000000);
        timeLinePaint.setStrokeWidth(4);

        eventPaint = new Paint(0);
        eventPaint.setColor(0xffffffff);

        eventTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eventTextPaint.setColor(0xff101010);
        eventTextPaint.setTextAlign(Align.CENTER);

        eventColorPaint = new Paint(0);
        eventColorPaint.setStrokeWidth(6);

        events = new ArrayList<Event>();
    }

    public void addEvent(Event ev)
    {
        events.add(ev);
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float headerHeight = getPx(40);
        float headerTextHeight = getPx(16);

        float columnWidth = width / (endDay - startDay + 2);
        float columnHeight = height - headerHeight;

        float sideTextPadding = columnWidth / 4.0f;
        float sideTextHeight = getPx(18);

        float eventTextPadding = getPx(6);
        float eventTextHeight = Math.min(getPx(18), columnWidth * 0.8f);

        headerTextPaint.setTextSize(headerTextHeight);
        sideTextPaint.setTextSize(sideTextHeight);
        eventTextPaint.setTextSize(eventTextHeight);

        // Background
        canvas.drawPaint(backgroundPaint);

        // Weekdays
        final String[] weekdays = { "MA", "DI", "WO", "DO", "VR", "ZA", "ZO" };

        for (int i = startDay - 1; i < endDay; i++)
        {
            canvas.drawText(weekdays[i], columnWidth * (1.5f + i), (headerHeight - headerTextHeight) / 2 + headerTextHeight, headerTextPaint);
        }

        // Vertical lines
        for (float x = columnWidth; x < width; x += columnWidth)
        {
            canvas.drawLine((int) x, headerHeight, (int) x, height, linePaint);
        }

        // Hours
        for (int h = 9; h <= 16; h++)
        {
            float hourY = headerHeight + getYFromHour(h, columnHeight);
            canvas.drawText(h + "h", columnWidth - sideTextPadding, hourY + sideTextHeight * 0.32f, sideTextPaint); // н
            canvas.drawLine(columnWidth - sideTextPadding / 2, hourY, columnWidth, hourY, linePaint);
        }

        // Events
        for (Event ev : events)
        {
            float left = columnWidth * ev.day;
            float top = headerHeight + getYFromHour(ev.startHour, columnHeight);
            float right = columnWidth * (ev.day + 1);
            float bottom = headerHeight + getYFromHour(ev.endHour, columnHeight);

            eventColorPaint.setColor(ev.color);

            canvas.drawRect(left, top, right, bottom, eventPaint);
            canvas.drawText(ev.description.substring(0, Math.min(ev.description.length(), 3)), left + columnWidth * 0.5f, top + eventTextPadding + eventTextHeight, eventTextPaint);
            canvas.drawLine(left + 2, top, left + 2, bottom, eventColorPaint);
            canvas.drawLine(left, top, right, top, linePaint);
            canvas.drawLine(left, bottom, right, bottom, linePaint);
        }

        // Time line
        Calendar calendar = Calendar.getInstance();

        float hour = calendar.get(Calendar.HOUR_OF_DAY) + (float) calendar.get(Calendar.MINUTE) / 60;
        float hourY = headerHeight + getYFromHour(hour, columnHeight);

        int day = (calendar.get(Calendar.DAY_OF_WEEK) - 2) % 7 + 1;
        float dayLeft = columnWidth * day;
        float dayRight = dayLeft + columnWidth;

        canvas.drawLine(dayLeft, hourY, dayRight, hourY, timeLinePaint);
    }

    private float getPx(float x)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, getResources().getDisplayMetrics());
    }

    private float getYFromHour(float hour, float height)
    {
        return (hour - startHour) / (endHour - startHour) * height * 0.92f;
    }

    public static class Event
    {
        public int day;
        public float startHour;
        public float endHour;
        public String description;
        public int color;
    }
}
