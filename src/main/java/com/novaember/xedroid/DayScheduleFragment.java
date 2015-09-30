package com.novaember.xedroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.Collections;

public class DayScheduleFragment extends Fragment
{
    private PagerSlidingTabStrip tabStrip;
    private ViewPager pager;
    private DaySchedulePagerAdapter pagerAdapter;

    private Attendee attendee;
    private int year;
    private int week;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.dayschedule_fragment, container, false);

        tabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.dayschedule_tabstrip);
        pager = (ViewPager) view.findViewById(R.id.dayschedule_viewpager);

        Bundle args = getArguments();
        attendee = new Attendee(args.getInt("attendeeId", 0));
        year = args.getInt("year", 1970);
        week = args.getInt("week", 1);
        int selectedDay = args.getInt("day", 1);
        int selectedEvent = args.getInt("eventId", 0);

        pagerAdapter = new DaySchedulePagerAdapter(attendee.getId(), year, week, getChildFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(selectedDay - 1);

        tabStrip.setViewPager(pager);

        return view;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            ((ActionBarActivity) getActivity()).getSupportActionBar().setElevation(0);
    }

    public static class DaySchedulePagerAdapter extends FragmentPagerAdapter
    {
        private int attendeeId;
        private int year;
        private int week;

        public DaySchedulePagerAdapter(int attendeeId, int year, int week, FragmentManager fm)
        {
            super(fm);

            this.attendeeId = attendeeId;
            this.year = year;
            this.week = week;
        }

        @Override
        public int getCount()
        {
            return 5;
        }

        public String getPageTitle(int position)
        {
            return (new String[]{"MA", "DI", "WO", "DO", "VR"})[position];
        }

        @Override
        public Fragment getItem(int position)
        {
            return (Fragment) DaySchedulePageFragment.newInstance(attendeeId, year, week, position + 1);
        }
    }

    public static class DaySchedulePageFragment extends ListFragment
    {
        private Attendee attendee;
        private int year;
        private int week;
        private int day;

        private DayScheduleAdapter dayScheduleAdapter;

        static DaySchedulePageFragment newInstance(int attendeeId, int year, int week, int day)
        {
            DaySchedulePageFragment f = new DaySchedulePageFragment();

            Bundle args = new Bundle();
            args.putInt("attendeeId", attendeeId);
            args.putInt("year", year);
            args.putInt("week", week);
            args.putInt("day", day);
            f.setArguments(args);

            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            attendee = new Attendee(getArguments() != null ? getArguments().getInt("attendeeId") : 0);
            year = getArguments() != null ? getArguments().getInt("year") : 1970;
            week = getArguments() != null ? getArguments().getInt("week") : 1;
            day = getArguments() != null ? getArguments().getInt("day") : 1;

            dayScheduleAdapter = new DayScheduleAdapter(attendee, year, week, day, getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.dayschedulepage_fragment, container, false);
            registerForContextMenu(view.findViewById(android.R.id.list));
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);
            setListAdapter(dayScheduleAdapter);
        }

        @Override
        public void onListItemClick(ListView listView, View view, int position, long id)
        {
            if (dayScheduleAdapter.getItemViewType(position) != DayScheduleAdapter.TYPE_EVENT) return;

            listView.showContextMenuForChild(view);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
        {
            super.onCreateContextMenu(menu, view, menuInfo);

            menu.setHeaderTitle("Ga naar rooster van...");

            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
            Event event = dayScheduleAdapter.getItem(info.position);

            int i = 0;
            for (Attendee attendee : event.getAttendees())
            {
                menu.add(0, i++, 0, attendee.getType().getName() + ": " + attendee.getName());
            }
        }

        @Override
        public boolean onContextItemSelected(MenuItem item)
        {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

            Event event = dayScheduleAdapter.getItem(info.position);
            if (event == null) return super.onContextItemSelected(item);

            Attendee attendee = event.getAttendees().get(item.getItemId());
            if (attendee == null) return super.onContextItemSelected(item);

            Intent intent = new Intent(getActivity(), ScheduleActivity.class);
            intent.putExtra("attendeeId", attendee.getId());
            intent.putExtra("year", year);
            intent.putExtra("week", week);
            startActivity(intent);

            return true;
        }
    }

    public static class DayScheduleAdapter extends BaseAdapter
    {
        private Activity activity;
        private ArrayList<Event> data;
        private static LayoutInflater inflater = null;
        private Attendee attendee = null;

        public static final int TYPE_EVENT = 0;
        public static final int TYPE_BREAK = 1;
        public static final int TYPE_MAX_COUNT = 2;

        public DayScheduleAdapter(Attendee attendee, int year, int week, int day, Activity activity)
        {
            this.attendee = attendee;
            this.activity = activity;

            data = attendee.getEvents(year, week, day);
            Collections.sort(data);

            for (int i = 1; i < data.size(); i++)
            {
                if (data.get(i).getStart().compareTo(data.get(i - 1).getEnd()) != 0)
                {
                    data.add(i, null);
                    i++;
                }
            }

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public Event getItem(int position)
        {
            return data.get(position);
        }

        public long getItemId(int position)
        {
            return (long) position;
        }

        public int getCount()
        {
            return data.size();
        }

        public int getViewTypeCount()
        {
            return TYPE_MAX_COUNT;
        }

        public int getItemViewType(int position)
        {
            return data.get(position) == null ? TYPE_BREAK : TYPE_EVENT;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            Event event = data.get(position);

            if (event == null)
            {
                if (convertView == null)
                    convertView = (View) inflater.inflate(R.layout.dayschedule_break_item, null);

                return convertView;
            }

            if (convertView == null)
                convertView = (View) inflater.inflate(R.layout.dayschedule_event_item, null);

            convertView.setTag(event);

            ((TextView) convertView.findViewById(R.id.dayschedule_event_description)).setText(event.getDescription());
            ((TextView) convertView.findViewById(R.id.dayschedule_event_staffs)).setText(TextUtils.join(", ", event.getStaffs()));
            ((TextView) convertView.findViewById(R.id.dayschedule_event_facilities)).setText(TextUtils.join(", ", event.getFacilities()));
            ((TextView) convertView.findViewById(R.id.dayschedule_event_classes)).setText(TextUtils.join(", ", event.getClasses()));

            ((TextView) convertView.findViewById(R.id.dayschedule_event_starttime)).setText(event.getStart().toString());
            ((TextView) convertView.findViewById(R.id.dayschedule_event_endtime)).setText(event.getEnd().toString());

            return convertView;
        }
    }
}
