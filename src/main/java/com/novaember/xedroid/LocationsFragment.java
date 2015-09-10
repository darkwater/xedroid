package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.ClassCastException;

public class LocationsFragment extends ListFragment
{
    private OnLocationSelectedListener listener;
    private ArrayAdapter<Location> adapter;
    private Organisation organisation;

    public interface OnLocationSelectedListener
    {
        public void onLocationSelected(Location location);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.locations_fragment, container, false);
    }

    public void setOrganisation(Organisation organisation)
    {
        this.organisation = organisation;
    }

    public Organisation getOrganisation()
    {
        return organisation;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        organisation = new Organisation(getArguments().getInt("organisationId"));

        try
        {
            listener = (OnLocationSelectedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnLocationSelectedListener");
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        ArrayList<Location> locations = organisation.getLocations();
        if (locations.isEmpty())
        {
            new AsyncTask<Void, Void, ArrayList<Location>>()
            {
                protected ArrayList<Location> doInBackground(Void... _)
                {
                    Xedule.updateLocations(organisation);
                    return organisation.getLocations();
                }

                protected void onPostExecute(ArrayList<Location> locations)
                {
                    populateList(locations);
                }
            }.execute();
        }
        else
        {
            populateList(locations);
        }
    }

    public void populateList(List<Location> locations)
    {
        if (getActivity() == null) return; // The activity could have been destroyed since we're coming
                                           //  from a background job

        ((ViewGroup) getView()).findViewById(R.id.list_loading).setVisibility(View.GONE);

        if (locations.isEmpty())
        {
            ((ViewGroup) getView()).findViewById(R.id.list_empty).setVisibility(View.VISIBLE);
        }
        else
        {
            adapter = new ArrayAdapter<Location>(getActivity(), R.layout.location_item, locations);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id)
    {
        listener.onLocationSelected(adapter.getItem(position));
    }
}
