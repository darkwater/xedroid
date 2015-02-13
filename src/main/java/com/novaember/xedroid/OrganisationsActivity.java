package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.Collections;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OrganisationsActivity extends ActionBarActivity
{
    private OrganisationAdapter organisations;
    private OrganisationsActivity self;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        self = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organisations);

        organisations = new OrganisationAdapter(this);

        progressBar = (ProgressBar) findViewById(R.id.organisations_progressbar);

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
                    progressBar.setVisibility(View.GONE);
                }
            }.execute();
        }
        else
        {
            organisations.addFromArrayList(orgs);
            progressBar.setVisibility(View.GONE);
        }

        organisationsView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> listview, View view, int pos, long id)
            {
                try
                {
                    if (listview.getAdapter().getItemId(pos) == -1)
                    {
                        Intent intent = new Intent(self, WeekScheduleActivity.class);
                        intent.putExtra("attendeeId", ((OrganisationAdapter) listview.getAdapter()).getMyAttendee().getId());
                        startActivity(intent);

                        return;
                    }

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
    public void onResume()
    {
        super.onResume();

        SharedPreferences sharedPref = this.getSharedPreferences("global", Context.MODE_PRIVATE);
        Attendee myattendee = new Attendee(sharedPref.getInt(getString(R.string.preference_myschedule_key), 0));
        if (myattendee.populate())
        {
            organisations.setMySchedule(myattendee);
        }
        else
        {
            organisations.setMySchedule(null);
        }
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

        return super.onOptionsItemSelected(item);
    }
}

class OrganisationAdapter extends BaseAdapter
{
    private Activity activity;
    private ArrayList<Organisation> data;
    private static LayoutInflater inflater = null;
    private Attendee myattendee = null;

    private static final int TYPE_LINEARLAYOUT = 0;
    private static final int TYPE_TEXTVIEW = 1;
    private static final int TYPE_MAX_COUNT = 2;

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

    public void setMySchedule(Attendee myattendee)
    {
        this.myattendee = myattendee;
        
        this.notifyDataSetChanged();
    }

    public int getCount()
    {
        return data.size() + ((myattendee != null) ? 1 : 0);
    }

    public Organisation getItem(int position)
    {
        return data.get((int) getItemId(position));
    }

    public long getItemId(int position)
    {
        return position - ((myattendee != null) ? 1 : 0);
    }

    public void sort()
    {
        Collections.sort(data);
    }

    public Attendee getMyAttendee()
    {
        return myattendee;
    }

    public int getViewTypeCount()
    {
        return TYPE_MAX_COUNT;
    }

    public int getItemViewType(int position)
    {
        if (position == 0 && myattendee != null)
        {
            return TYPE_LINEARLAYOUT;
        }

        return TYPE_TEXTVIEW;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (position == 0 && myattendee != null)
        {
            if (convertView == null)
                convertView = (View) inflater.inflate(R.layout.attendee_item, null);

            ((TextView) convertView.findViewById(R.id.attendee_name)).setText(myattendee.getName());
            ((TextView) convertView.findViewById(R.id.attendee_type)).setText(R.string.myschedule_label);

            return convertView;
        }

        if (convertView == null)
            convertView = (View) inflater.inflate(R.layout.organisation_item, null);

        Organisation org = getItem(position);

        ((TextView) convertView).setText(org.getName());

        return convertView;
    }
}
