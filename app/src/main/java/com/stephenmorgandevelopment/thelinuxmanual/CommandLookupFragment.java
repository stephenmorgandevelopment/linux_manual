package com.stephenmorgandevelopment.thelinuxmanual;

import android.os.Bundle;
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

import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.utils.MatchListAdapter;

import java.util.List;

import io.reactivex.plugins.RxJavaPlugins;

public class CommandLookupFragment extends Fragment {
    private EditText searchText;
    private ImageButton searchBtn;
    private String searchString;
    private List<SimpleCommand> matchedCommands;
    private ListView matchListView;
    private Adapter matchListAdapter;

    public static CommandLookupFragment getInstance() {
        return new CommandLookupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.command_lookup_fragment, null);
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
        //TODO Repopulate search text and list of matched queries.

    }

    @Override
    public void onPause() {
        super.onPause();
        //TODO Store search text and list of matched queries.

    }
}
