package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.Collections;

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
    private AttendeeAdapter attendees;
    private AttendeesActivity self;

    private int locationId;
    private String locationName;

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
                    Intent intent = new Intent(self, WeekScheduleActivity.class);
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
    public Type type;

    public enum Type
    {
        CLASS    (1, R.string.attendee_type_class),
        STAFF    (2, R.string.attendee_type_staff),
        FACILITY (3, R.string.attendee_type_facility);

        public final int id;
        public final int label;

        private Type(int id, int label)
        {
            this.id = id;
            this.label = label;
        }

        public static Type getById(int id) throws Exception
        {
            switch (id)
            {
                case 1:  return Type.CLASS;
                case 2:  return Type.STAFF;
                case 3:  return Type.FACILITY;
                default: throw new Exception("Invalid attendee type: " + String.valueOf(id));
            }
        }
    }

    public Attendee(int id, String name, int type)
    {
        this.id = id;
        this.name = name;

        try
        {
            this.type = Type.getById(type);
        }
        catch (Exception e)
        {
            Log.e("Xedule", "Error: " + e.getMessage());
        }
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
                this.addAttendee(new Attendee(att.getInt("id"), att.getString("name"), att.getInt("type")));
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
        View view = convertView;
        if(convertView == null)
            view = inflater.inflate(R.layout.attendee_item, null);


        TextView name = (TextView) view.findViewById(R.id.attendee_name);
        TextView type = (TextView) view.findViewById(R.id.attendee_type);

        Attendee att = data.get(position);

        name.setText(att.name);
        type.setText(att.type.label);

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
