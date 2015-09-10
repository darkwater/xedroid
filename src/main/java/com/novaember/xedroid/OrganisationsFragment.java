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

public class OrganisationsFragment extends ListFragment
{
    private OnOrganisationSelectedListener listener;
    private ArrayAdapter<Organisation> adapter;

    public interface OnOrganisationSelectedListener
    {
        public void onOrganisationSelected(Organisation organisation);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.organisations_fragment, container, false);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            listener = (OnOrganisationSelectedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnOrganisationSelectedListener");
        }

        ArrayList<Organisation> organisations = Organisation.getAll();
        if (organisations.isEmpty())
        {
            new AsyncTask<Void, Void, ArrayList<Organisation>>()
            {
                protected ArrayList<Organisation> doInBackground(Void... _)
                {
                    Xedule.updateOrganisations();
                    return Organisation.getAll();
                }

                protected void onPostExecute(ArrayList<Organisation> organisations)
                {
                    populateList(organisations);
                }
            }.execute();
        }
        else
        {
            populateList(organisations);
        }
    }

    public void populateList(List<Organisation> organisations)
    {
        if (getActivity() == null) return; // The activity could have been destroyed since we're coming
                                           //  from a background job

        ((ViewGroup) getView()).findViewById(R.id.list_loading).setVisibility(View.GONE);

        if (organisations.isEmpty())
        {
            ((ViewGroup) getView()).findViewById(R.id.list_empty).setVisibility(View.VISIBLE);
        }
        else
        {
            adapter = new ArrayAdapter<Organisation>(getActivity(), R.layout.organisation_item, organisations);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id)
    {
        listener.onOrganisationSelected(adapter.getItem(position));
    }
}
