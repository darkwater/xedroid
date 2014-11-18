package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

public class AttendeesActivity extends ActionBarActivity
{
    AttendeeAdapter attendees;
    AttendeesActivity self;

    int locationId;
    String locationName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        self = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendees);

        Intent intent = getIntent();
        locationId = intent.getIntExtra("locationId", 0);
        locationName = intent.getStringExtra("locationName");

        ActionBar bar = getSupportActionBar();
        bar.setTitle(locationName);
        bar.setDisplayHomeAsUpEnabled(true);

        attendees = new AttendeeAdapter(this);
        new FetchAttendeesTask().execute("http://xedule.novaember.com/attendees." + locationId + ".json");

        ListView attendeesView = (ListView) findViewById(R.id.attendees);
        attendeesView.setAdapter(attendees);

        attendeesView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView listview, View view, int pos, long id)
            {
                try
                {
                    Attendee att = (Attendee) listview.getAdapter().getItem(pos);
                    Intent intent = new Intent(self, AttendeesActivity.class);
                    intent.putExtra("attendeeId", att.id);
                    intent.putExtra("attendeeName", att.name);
                    startActivity(intent);
                }
                catch(Exception e)
                {
                    Log.e("Xedroid", "Error: " + e.getMessage());
                }
            }
        });

        EditText searchInput = (EditText) findViewById(R.id.search_attendees);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will automatically
        // handle clicks on the Home/Up button, so long as you specify a parent
        // activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class FetchAttendeesTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls)
        {
            // params comes from the execute() call: params[0] is the url.
            try
            {
                return Fetcher.downloadUrl(urls[0]);
            }
            catch (Exception e)
            {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            try
            {
                attendees.addAttendeesFromJSON(result);
            }
            catch (JSONException e)
            {
                Log.e("Xedroid", "Error: " + e.getMessage());
            }
        }
    }
}

class Attendee implements Comparable<Attendee>
{
    public int id;
    public String name;

    public Attendee(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    @Override
    public int compareTo(Attendee att)
    {
        return this.name.compareTo(att.name);
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

    public void addAttendee(Attendee att)
    {
        data.add(att);
    }

    public void addAttendeesFromJSON(String input) throws JSONException
    {
        try
        {
            JSONArray arr = new JSONArray(input);

            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject att = arr.getJSONObject(i);
                this.addAttendee(new Attendee(att.getInt("id"), att.getString("name")));
            }

            this.sort();
        }
        catch (JSONException e)
        {
            Log.e("Xedroid", "Error! " + e.getMessage());
        }
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
        TextView view = (TextView) convertView;
        if(convertView == null)
            view = (TextView) inflater.inflate(R.layout.attendee_item, null);

        Attendee att = data.get(position);

        view.setText(att.name);

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
                    String valueName = value.name.toLowerCase();

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
