package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
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
        View view = inflater.inflate(R.layout.attendees_fragment, container, false);

        EditText searchInput = (EditText) view.findViewById(R.id.attendees_search);

        searchInput.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3)
            {
                adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
            {

            }

            @Override
            public void afterTextChanged(Editable arg0)
            {

            }
        });

        return view;
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

    public void populateList(ArrayList<Attendee> attendees)
    {
        adapter = new AttendeesAdapter(getActivity(), attendees);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id)
    {
        listener.onAttendeeSelected(adapter.getItem(position));
    }

    private class AttendeesAdapter extends BaseAdapter implements Filterable
    {
        private Activity activity;
        private ArrayList<Attendee> data;
        private ArrayList<Attendee> originalData;
        private AttendeeFilter filter;
        private LayoutInflater inflater;

        public AttendeesAdapter(Activity a, ArrayList<Attendee> input)
        {
            activity = a;
            data = input;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        public Attendee getItem(int position)
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
            this.notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = convertView;
            if(convertView == null)
                view = inflater.inflate(R.layout.attendee_item, null);


            TextView name = (TextView) view.findViewById(R.id.attendee_name);
            TextView type = (TextView) view.findViewById(R.id.attendee_type);

            Attendee att = data.get(position);

            name.setText(att.getName());
            type.setText(att.getType().label);

            return view;
        }

        @Override
        public Filter getFilter()
        {
            if (filter == null)
                filter = new AttendeeFilter();

            return filter;
        }

        private class AttendeeFilter extends Filter
        {
            @Override
            protected FilterResults performFiltering(CharSequence query)
            {
                FilterResults results = new FilterResults();

                if (originalData == null)
                    originalData = new ArrayList<Attendee>(data);

                if (query == null || query.length() == 0)
                {
                    results.values = originalData;
                    results.count = originalData.size();
                }
                else
                {
                    String queryString = query.toString().toLowerCase();

                    ArrayList<Attendee> values = new ArrayList<Attendee>(originalData);

                    int count = values.size();
                    ArrayList<Attendee> newValues = new ArrayList<Attendee>();

                    for (int i = 0; i < count; i++)
                    {
                        Attendee value = values.get(i);
                        String valueName = value.getName().toLowerCase();

                        if (valueName.indexOf(queryString) >= 0)
                            newValues.add(value);
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                data = (ArrayList<Attendee>) results.values;

                if (results.count > 0) notifyDataSetChanged();
                else notifyDataSetInvalidated();
            }
        }
    }
}
