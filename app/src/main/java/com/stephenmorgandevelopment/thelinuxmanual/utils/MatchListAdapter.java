package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;

import java.util.ArrayList;
import java.util.List;

public class MatchListAdapter extends BaseAdapter {
    private List<SimpleCommand> matches;
    private LayoutInflater inflater;

    public MatchListAdapter(Context ctx) {
        matches = new ArrayList<>();
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setMatches(List<SimpleCommand> matches) {
        if(this.matches == null) {
            this.matches = new ArrayList<>();
        }
        this.matches.clear();
        this.matches.addAll(matches);
    }


    @Override
    public int getCount() {
        return matches.size();
    }

    @Override
    public Object getItem(int position) {
        return matches.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.match_list_item, null);

        ((TextView) view.findViewById(R.id.matchListCommand)).setText(matches.get(position).getName());
        ((TextView) view.findViewById(R.id.matchListDescription)).setText(matches.get(position).getDescription());

        return view;
    }
}
