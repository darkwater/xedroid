package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LocationsActivity extends ActionBarActivity
{
    private LocationsActivity self;
    private LocationAdapter locations;

    private ProgressBar progressBar;

    private Organisation organisation;

	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
        self = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locations);
 
        Intent intent = getIntent();
        int organisationId = intent.getIntExtra("organisationId", 0);
        organisation = new Organisation(organisationId);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(organisation.getName());
        bar.setDisplayHomeAsUpEnabled(true);

        locations = new LocationAdapter(this);

        progressBar = (ProgressBar) findViewById(R.id.locations_progressbar);

        ListView locationsView = (ListView) findViewById(R.id.locations);
        locationsView.setAdapter(locations);

        ArrayList<Location> locs = organisation.getLocations();
        if (locs.isEmpty())
        {
            refresh();
        }
        else
        {
            locations.addFromArrayList(locs);
            progressBar.setVisibility(View.GONE);
        }

        locationsView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> listview, View view, int pos, long id)
            {
                try
                {
                    Location loc = (Location) listview.getAdapter().getItem(pos);
                    Intent intent = new Intent(self, AttendeesActivity.class);
                    intent.putExtra("locationId", loc.getId());
                    startActivity(intent);
                }
                catch(Exception e)
                {
                    Log.e("Xedroid", "Error while clicking the thing", e);
                }
            }
        });
	}

    public void refresh()
    {
        progressBar.setVisibility(View.VISIBLE);
        locations.clear();

        new AsyncTask<Void, Void, ArrayList<Location>>()
        {
            protected ArrayList<Location> doInBackground(Void... _)
            {
                Xedule.updateLocations(organisation.getId());
                return organisation.getLocations();
            }

            protected void onPostExecute(ArrayList<Location> locs)
            {
                locations.addFromArrayList(locs);
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.locations, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will automatically
        // handle clicks on the Home/Up button, so long as you specify a parent
        // activity in AndroidManifest.xml.
		int id = item.getItemId();

        if (id == R.id.locations_refresh)
        {
            refresh();

            return true;
        }

		if (id == R.id.action_settings)
        {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}

class LocationAdapter extends BaseAdapter
{
    private Activity activity;
    private ArrayList<Location> data;
    private static LayoutInflater inflater = null;

    public LocationAdapter(Activity a)
    {
        activity = a;
        data = new ArrayList<Location>();

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void add(Location loc)
    {
        data.add(loc);
    }

    public void addFromArrayList(ArrayList<Location> input)
    {
        for (Location loc : input)
        {
            this.add(loc);
        }

        this.notifyDataSetChanged();
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

    public Location getItem(int position)
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
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        TextView view = (TextView) convertView;
        if(convertView == null)
            view = (TextView) inflater.inflate(R.layout.location_item, null);

        Location loc = data.get(position);

        view.setText(loc.getName());

        return view;
    }
}
