package com.novaember.xedroid;

import android.widget.ListView;

import java.util.ArrayList;

public interface EventReceiver
{
    public void setEvents(ArrayList<Event> events);
    public void setWeek(int year, int week);
}
