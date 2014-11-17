package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.TextView;

public class OrganisationsActivity extends ActionBarActivity
{
    OrganisationAdapter organisations;
    OrganisationsActivity self;

	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
        self = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organisations);

        organisations = new OrganisationAdapter(this);
        new FetchOrganisationsTask().execute("http://xedule.novaember.com/organisations.json");

        ListView organisationsView = (ListView) findViewById(R.id.organisations);
        organisationsView.setAdapter(organisations);

        organisationsView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView listview, View view, int pos, long id)
            {
                try
                {
                    Organisation org = (Organisation) listview.getAdapter().getItem(pos);
                    Intent intent = new Intent(self, LocationsActivity.class);
                    intent.putExtra("organisationId", org.id);
                    intent.putExtra("organisationName", org.name);
                    startActivity(intent);
                }
                catch(Exception e)
                {
                    Log.d("Xedroid", "Error: " + e.getMessage());
                }
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

    private class FetchOrganisationsTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls)
        {
            // params comes from the execute() call: params[0] is the url.
            try
            {
                Log.d("Xedroid", "brb fetching " + urls[0]);
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
                Log.d("Xedroid", "Result! " + result.length());
                Log.d("Xedroid", result);
                organisations.addOrganisationsFromJSON(result);
            }
            catch (JSONException e)
            {
                Log.e("Xedroid", "Error: " + e.getMessage());
            }
        }
    }
}

class Organisation implements Comparable<Organisation>
{
    public int id;
    public String name;

    public Organisation(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    @Override
    public int compareTo(Organisation org)
    {
        return this.name.compareTo(org.name);
    }
}

class OrganisationAdapter extends BaseAdapter
{
    private Activity activity;
    private ArrayList<Organisation> data;
    private static LayoutInflater inflater = null;

    public OrganisationAdapter(Activity a)
    {
        activity = a;
        data = new ArrayList<Organisation>();

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addOrganisation(Organisation org)
    {
        data.add(org);
    }

    public void addOrganisationsFromJSON(String input) throws JSONException
    {
        try
        {
            JSONArray arr = new JSONArray(input);

            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject org = arr.getJSONObject(i);
                this.addOrganisation(new Organisation(org.getInt("id"), org.getString("name")));
            }

            this.sort();
            this.notifyDataSetChanged();
        }
        catch (JSONException e)
        {
            Log.d("Xedroid", "Error! " + e.getMessage());
        }
    }

    public int getCount()
    {
        return data.size();
    }

    public Organisation getItem(int position)
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
            view = (TextView) inflater.inflate(R.layout.organisation_item, null);

        Organisation org = data.get(position);

        view.setText(org.name);

        return view;
    }
}
