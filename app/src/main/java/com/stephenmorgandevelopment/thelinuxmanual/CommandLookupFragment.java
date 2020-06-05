package com.stephenmorgandevelopment.thelinuxmanual;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.stephenmorgandevelopment.thelinuxmanual.databases.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.utils.MatchListAdapter;

import java.io.IOException;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class CommandLookupFragment extends Fragment {
    public static final String TAG = CommandLookupFragment.class.getSimpleName();
    Disposable disposable;

    private EditText searchText;
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
//                        matchListView.setAdapter(matchListAdapter);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        matchListView.setAdapter(matchListAdapter);
        matchListView.setOnItemClickListener(itemClicked);
    }

    @Override
    public void onPause() {
        super.onPause();


        //TODO Store search text and list of matched queries.

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(MatchListAdapter.disposables != null) {
            MatchListAdapter.disposables.dispose();
            MatchListAdapter.disposables.clear();
        }
    }


    AdapterView.OnItemClickListener itemClicked = new AdapterView.OnItemClickListener() {
        CommandInfoFragment infoFragment;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            disposable = HttpClient.fetchCommandManPage(matchListAdapter.getItem(position).getUrl())
                    .subscribeOn(Schedulers.io())
                    .flatMapCompletable(response -> {
                        if(response.isSuccessful() && response.code() == 200) {
                            infoFragment = CommandInfoFragment.getInstance();
                            infoFragment.setInfo(Ubuntu.crawlForCommandInfo(response.body().string()));

                            return Completable.complete();
                        }

                       return Completable.error(new Throwable("Response returned with code: " + response.code()));
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete(() -> {
                        FragmentManager manager = getActivity().getSupportFragmentManager();
                        manager.beginTransaction().add(R.id.fragmentContainer, infoFragment, CommandInfoFragment.TAG).addToBackStack(CommandInfoFragment.TAG).commit();
                    })
                    .subscribe(() -> {

                    }, error -> {
                        Toast.makeText(getContext(), "Error fetching data\n" + error.getMessage(), Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    });
        }
    };
}
