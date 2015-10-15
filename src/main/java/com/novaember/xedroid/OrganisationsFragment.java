package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.ClassCastException;

public class OrganisationsFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener
{
    private OnOrganisationSelectedListener listener;
    private ArrayAdapter<Organisation> adapter;
    private SwipeRefreshLayout swipeLayout;

    public interface OnOrganisationSelectedListener
    {
        public void onOrganisationSelected(Organisation organisation);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.organisations_fragment, container, false);

        swipeLayout = (SwipeRefreshLayout) ((ViewGroup) v).findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(R.color.colorPrimary);

        return v;
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
    }

    @Override
    public void onStart()
    {
        super.onStart();

        ArrayList<Organisation> organisations = Organisation.getAll();
        if (organisations.isEmpty())
        {
            refresh();
        }
        else
        {
            populateList(organisations);
        }
    }

    @Override
    public void onRefresh()
    {
        refresh();
    }

    public void refresh()
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
                swipeLayout.setRefreshing(false);
            }
        }.execute();
    }

    public void populateList(List<Organisation> organisations)
    {
        super.onStart();

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
