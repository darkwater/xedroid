package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.Collections;

import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AttendeesActivity extends ActionBarActivity
{
    private AttendeeAdapter attendees;
    private AttendeesActivity self;

    private ProgressBar progressBar;
    private EditText searchInput;

    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        self = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendees);

        Intent intent = getIntent();
        location = new Location(intent.getIntExtra("locationId", 0));

        ActionBar bar = getSupportActionBar();
        bar.setTitle(location.getName());
        bar.setDisplayHomeAsUpEnabled(true);

        attendees = new AttendeeAdapter(this);

        progressBar = (ProgressBar) findViewById(R.id.attendees_progressbar);
        searchInput = (EditText) findViewById(R.id.attendees_search);

        ListView attendeesView = (ListView) findViewById(R.id.attendees);
        attendeesView.setAdapter(attendees);

        ArrayList<Attendee> atts = location.getAttendees();
        if (atts.isEmpty())
        {
            refresh();
        }
        else
        {
            attendees.addFromArrayList(atts);
            progressBar.setVisibility(View.GONE);
        }

        attendeesView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> listview, View view, int pos, long id)
            {
                try
                {
                    Attendee attendee = (Attendee) listview.getAdapter().getItem(pos);
                    Intent intent = new Intent(self, WeekScheduleActivity.class);
                    intent.putExtra("attendeeId", attendee.getId());
                    startActivity(intent);
                }
                catch(Exception e)
                {
                    Log.e("Xedroid", "Error: " + e.getMessage());
                }
            }
        });

        EditText searchInput = (EditText) findViewById(R.id.attendees_search);

        // Work around a bug where EditText padding doesn't always get applied from the XML file
        searchInput.setPadding(16, 16, 16, 16);

        searchInput.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3)
            {
                self.attendees.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
            {

            }

            @Override
            public void afterTextChanged(Editable arg0)
            {

            }
        });
    }

    public void refresh()
    {
        searchInput.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        attendees.clear();

        new AsyncTask<ProgressBar, Void, ArrayList<Attendee>>()
        {
            protected ArrayList<Attendee> doInBackground(ProgressBar... pBar)
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

                Xedule.updateAttendees(location.getId(), pBar[0]);
                return location.getAttendees();
            }

            protected void onPostExecute(ArrayList<Attendee> atts)
            {
                attendees.addFromArrayList(atts);
                progressBar.setVisibility(View.GONE);
                searchInput.setVisibility(View.VISIBLE);

                if (searchInput.requestFocus())
                {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        }.execute(progressBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.attendees, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will automatically
        // handle clicks on the Home/Up button, so long as you specify a parent
        // activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.attendees_refresh)
        {
            refresh();

            return true;
        }

        if (id == R.id.action_settings)
        {
            return true;
        }

        if (id == android.R.id.home)
        {
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.putExtra("organisationId", location.getOrganisation().getId());
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
                Intent intent = new Intent(self, LocationsActivity.class);
                intent.putExtra("organisationId", location.getOrganisation().getId());
                startActivity(intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

class AttendeeAdapter extends BaseAdapter implements Filterable
{
    private Activity activity;
    private ArrayList<Attendee> data;
    private ArrayList<Attendee> originalData;
    private AttendeeFilter filter;
    private static LayoutInflater inflater = null;

    public AttendeeAdapter(Activity a)
    {
        activity = a;
        data = new ArrayList<Attendee>();

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void add(Attendee att)
    {
        data.add(att);

        this.notifyDataSetChanged();
    }

    public void addFromArrayList(ArrayList<Attendee> input)
    {
        for (Attendee att : input)
        {
            this.add(att);
        }
    }

    public void clear()
    {
        data.clear();

        this.notifyDataSetChanged();
    }

    public int getCount()
    {
        return data.size();
    }

    public Attendee getItem(int position)
    {
        return data.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void sort()
    {
        Collections.sort(data);
        this.notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if(convertView == null)
            view = inflater.inflate(R.layout.attendee_item, null);


        TextView name = (TextView) view.findViewById(R.id.attendee_name);
        TextView type = (TextView) view.findViewById(R.id.attendee_type);

        Attendee att = data.get(position);

        name.setText(att.getName());
        type.setText(att.getType().label);

        return view;
    }

    @Override
    public Filter getFilter()
    {
        if (filter == null)
            filter = new AttendeeFilter();

        return filter;
    }

    private class AttendeeFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence query)
        {
            FilterResults results = new FilterResults();

            if (originalData == null)
                originalData = new ArrayList<Attendee>(data);

            if (query == null || query.length() == 0)
            {
                results.values = originalData;
                results.count = originalData.size();
            }
            else
            {
                String queryString = query.toString().toLowerCase();

                ArrayList<Attendee> values = new ArrayList<Attendee>(originalData);

                int count = values.size();
                ArrayList<Attendee> newValues = new ArrayList<Attendee>();

                for (int i = 0; i < count; i++)
                {
                    Attendee value = values.get(i);
                    String valueName = value.getName().toLowerCase();

                    if (valueName.indexOf(queryString) >= 0)
                        newValues.add(value);
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            data = (ArrayList<Attendee>) results.values;

            if (results.count > 0) notifyDataSetChanged();
            else notifyDataSetInvalidated();
        }
    }
}
