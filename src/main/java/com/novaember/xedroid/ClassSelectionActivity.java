package com.novaember.xedroid;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
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

public class ClassSelectionActivity extends ActionBarActivity implements OrganisationsFragment.OnOrganisationSelectedListener,
                                                                         LocationsFragment.OnLocationSelectedListener,
                                                                         AttendeesFragment.OnAttendeeSelectedListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classselection);

        if (savedInstanceState == null)
        {
            if (findViewById(R.id.classselection_fragment) != null) // Single-pane view
            {
                OrganisationsFragment organisationsFragment = new OrganisationsFragment();

                getSupportFragmentManager().beginTransaction().add(R.id.classselection_fragment, organisationsFragment).commit();
            }
//            else // Multi-pane view
//            {
//                OrganisationsFragment organisationsFragment = new OrganisationsFragment();
//                LocationsFragment locationsFragment = new LocationsFragment();
//                AttendeesFragment attendeesFragment = new AttendeesFragment();
//
//                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.organisations_fragment, organisationsFragment)
//                    .add(R.id.locations_fragment, locationsFragment)
//                    .add(R.id.attendees_fragment, attendeesFragment)
//                    .commit();
//            }
        }
    }

    public void onOrganisationSelected(Organisation organisation)
    {
        ActionBar bar = getSupportActionBar();
        bar.setTitle(organisation.getName());

        LocationsFragment locationsFragment = null; //(LocationsFragment) getSupportFragmentManager().findFragmentById(R.id.locations_fragment);

        if (locationsFragment != null) // Multi-pane view
        {
            locationsFragment.updateOrganisation(organisation);
        }
        else // Single-pane view
        {
            locationsFragment = new LocationsFragment();

            Bundle args = new Bundle();
            args.putInt("organisationId", organisation.getId());
            locationsFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                .replace(R.id.classselection_fragment, locationsFragment)
                .addToBackStack(null)
                .commit();
        }
    }

    public void onLocationSelected(Location location)
    {
        ActionBar bar = getSupportActionBar();
        bar.setTitle(location.getName());

        AttendeesFragment attendeesFragment = null; //(AttendeesFragment) getSupportFragmentManager().findFragmentById(R.id.attendees_fragment);

        if (attendeesFragment != null) // Multi-pane view
        {
            attendeesFragment.updateLocation(location);
        }
        else // Single-pane view
        {
            attendeesFragment = new AttendeesFragment();

            Bundle args = new Bundle();
            args.putInt("locationId", location.getId());
            attendeesFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                .replace(R.id.classselection_fragment, attendeesFragment)
                .addToBackStack(null)
                .commit();
        }
    }

    public void onAttendeeSelected(Attendee attendee)
    {
        try
        {
            Intent intent = new Intent(this, WeekScheduleActivity.class);
            intent.putExtra("attendeeId", attendee.getId());
            startActivity(intent);
        }
        catch(Exception e)
        {
            Log.e("Xedroid", "Error: " + e.getMessage());
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
