package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ClassCastException;

public class AttendeesFragment extends ListFragment
{
    private OnAttendeeSelectedListener listener;
    private AttendeesAdapter adapter;
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
        adapter = new AttendeesAdapter(getActivity(), R.layout.attendee_item, R.id.attendee_name, attendees);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id)
    {
        listener.onAttendeeSelected(adapter.getItem(position));
    }

    private class AttendeesAdapter extends ArrayAdapter<Attendee>
    {
        public AttendeesAdapter(Context context, int resource, int textViewResourceId, List<Attendee> objects)
        {
            super(context, resource, textViewResourceId, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = super.getView(position, convertView, parent);

            TextView type = (TextView) view.findViewById(R.id.attendee_type);
            type.setText(getItem(position).getType().label);

            return view;
        }
    }
}