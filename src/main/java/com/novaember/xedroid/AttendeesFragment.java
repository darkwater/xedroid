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

public class AttendeesFragment extends ListFragment
{
    private OnAttendeeSelectedListener listener;
    private ArrayAdapter<Attendee> adapter;
    private Location location;

    public interface OnAttendeeSelectedListener
    {
        public void onAttendeeSelected(Attendee attendee);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.attendees_fragment, container, false);
    }

    public void updateLocation(Location location)
    {
        this.location = location;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        location = new Location(getArguments().getInt("locationId"));

        try
        {
            listener = (OnAttendeeSelectedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnAttendeeSelectedListener");
        }

        ArrayList<Attendee> attendees = location.getAttendees();
        if (attendees.isEmpty())
        {
            new AsyncTask<Void, Void, ArrayList<Attendee>>()
            {
                protected ArrayList<Attendee> doInBackground(Void... _)
                {
                    Xedule.updateAttendees(location, null);
                    return location.getAttendees();
                }

                protected void onPostExecute(ArrayList<Attendee> attendees)
                {
                    populateList(attendees);
                }
            }.execute();
        }
        else
        {
            populateList(attendees);
        }
    }

    public void populateList(List<Attendee> attendees)
    {
        adapter = new ArrayAdapter<Attendee>(getActivity(), R.layout.attendee_item, R.id.attendee_name, attendees);

        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id)
    {
        listener.onAttendeeSelected(adapter.getItem(position));
    }
}
