package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.Collections;

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
    private OrganisationAdapter organisations;
    private OrganisationsActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        self = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organisations);

        organisations = new OrganisationAdapter(this);

        ListView organisationsView = (ListView) findViewById(R.id.organisations);
        organisationsView.setAdapter(organisations);

        ArrayList<Organisation> orgs = Organisation.getAll();
        if (orgs.isEmpty())
        {
            new AsyncTask<Void, Void, ArrayList<Organisation>>()
            {
                protected ArrayList<Organisation> doInBackground(Void... _)
                {
                    Xedule.updateOrganisations();
                    return Organisation.getAll();
                }

                protected void onPostExecute(ArrayList<Organisation> orgs)
                {
                    organisations.addFromArrayList(orgs);
                }
            }.execute();
        }
        else
        {
            organisations.addFromArrayList(orgs);
        }

        organisationsView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> listview, View view, int pos, long id)
            {
                try
                {
                    Organisation org = (Organisation) listview.getAdapter().getItem(pos);
                    Intent intent = new Intent(self, LocationsActivity.class);
                    intent.putExtra("organisationId", org.getId());
                    startActivity(intent);
                }
                catch(Exception e)
                {
                    Log.e("Xedroid", "Error: " + e.getMessage());
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

    public void add(Organisation org)
    {
        data.add(org);

        this.notifyDataSetChanged();
    }

    public void addFromArrayList(ArrayList<Organisation> input)
    {
        for (Organisation org : input)
        {
            this.add(org);
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

        view.setText(org.getName());

        return view;
    }
}
