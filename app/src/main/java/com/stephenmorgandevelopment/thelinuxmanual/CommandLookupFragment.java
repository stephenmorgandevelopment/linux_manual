package com.stephenmorgandevelopment.thelinuxmanual;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.stephenmorgandevelopment.thelinuxmanual.databases.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.utils.MatchListAdapter;

import java.util.List;

import io.reactivex.plugins.RxJavaPlugins;

public class CommandLookupFragment extends Fragment {
    public static final String TAG = CommandLookupFragment.class.getSimpleName();

    private EditText searchText;
    private ImageButton searchBtn;
    private String searchString;
    private List<SimpleCommand> matchedCommands;
    private ListView matchListView;
    private MatchListAdapter matchListAdapter;

    public static CommandLookupFragment getInstance() {
        return new CommandLookupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.command_lookup_fragment, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchText = view.findViewById(R.id.searchText);
        searchBtn = view.findViewById(R.id.searchBtn);
        matchListView = view.findViewById(R.id.matchList);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public void onResume() {
        super.onResume();
        if(matchListAdapter == null) {
            matchListAdapter = new MatchListAdapter(getContext());
        }

        //TODO Repopulate search text and list of matched queries.

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count >= 2) {
                    String searchText = String.valueOf(s);
                    List<SimpleCommand> matches = DatabaseHelper.getInstance().partialMatches(searchText);

                    if(matches != null && matches.size() > 0) {
                        matchListAdapter.setMatches(matches);
                        matchListAdapter.notifyDataSetChanged();
                        matchListView.setAdapter(matchListAdapter);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        matchListView.setAdapter(matchListAdapter);

    }

    @Override
    public void onPause() {
        super.onPause();

        if(MatchListAdapter.disposables != null) {
            MatchListAdapter.disposables.clear();
        }

        //TODO Store search text and list of matched queries.

    }
}
