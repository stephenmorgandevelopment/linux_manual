package com.stephenmorgandevelopment.thelinuxmanual;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CommandLookupFragment extends Fragment {
    public static final String TAG = CommandLookupFragment.class.getSimpleName();
    private CompositeDisposable disposables;

    private EditText searchText;
    private ListView matchListView;
    private MatchListAdapter matchListAdapter;
    private TextView fetchingDataDialog;

    static boolean loadingInfo = false;

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
        fetchingDataDialog = view.findViewById(R.id.fetchingDataDialog);

        disposables = new CompositeDisposable();

        matchListView.setDividerHeight(5);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (matchListAdapter == null) {
            matchListAdapter = new MatchListAdapter(getContext());
        }

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(loadingInfo) {
                    return;
                }

                if (s.length() >= 2) {
                    String searchText = String.valueOf(s).replaceAll("'", "");
                    searchText = searchText.replaceAll("%", "");

                    Disposable disposable = Single.just(DatabaseHelper.getInstance().partialMatches(searchText))
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .delay(200, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(error -> {
                                Toast.makeText(getContext(), "Invalid character entered", Toast.LENGTH_LONG).show();
                            })
                            .subscribe(list -> {
                                        if (list.size() > 0) {
                                            matchListAdapter.setMatches(list);
                                            matchListAdapter.notifyDataSetChanged();
                                        }
                                    },
                                    error -> {
                                        Log.d(TAG, "SQL error: " + error.toString());
                                    });

                    disposables.add(disposable);
                } else {
                    matchListAdapter.clear();
                    matchListAdapter.notifyDataSetChanged();
                }
            }
        });

        matchListView.setAdapter(matchListAdapter);
        matchListView.setOnItemClickListener(itemClicked);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void cleanup() {
        if (disposables != null) {
            disposables.clear();
        }

        if (MatchListAdapter.helperThreads != null) {
            for (Thread thread : MatchListAdapter.helperThreads) {
                thread.interrupt();
                thread = null;
            }
        }

        if (MatchListAdapter.disposables != null) {
            MatchListAdapter.disposables.clear();
            matchListAdapter = null;
        }

        loadingInfo = false;
    }

    AdapterView.OnItemClickListener itemClicked = new AdapterView.OnItemClickListener() {
        CommandInfoFragment infoFragment;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(loadingInfo) {
                return;
            }

            loadingInfo = true;

            fetchingDataDialog.setVisibility(View.VISIBLE);
            infoFragment = null;

            Disposable disposable = HttpClient.fetchCommandManPage(matchListAdapter.getItem(position).getUrl())
                    .subscribeOn(Schedulers.io())
                    .flatMapCompletable(response -> {
                        if (response.isSuccessful() && response.code() == 200) {
                            infoFragment = CommandInfoFragment.getInstance();
                            infoFragment.setInfo(Ubuntu.crawlForCommandInfo(response.body().string()));

                            return Completable.complete();
                        }

                        return Completable.error(new Throwable("Response returned with code: " + response.code()));
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(error -> {
                        fetchingDataDialog.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error fetching data\n" + error.getMessage(), Toast.LENGTH_LONG).show();
                    })
                    .subscribe(() -> {

                                FragmentManager manager = getActivity().getSupportFragmentManager();

                                manager.beginTransaction()
                                        .add(R.id.fragmentContainer, infoFragment, CommandInfoFragment.TAG)
                                        .addToBackStack(CommandInfoFragment.TAG)
                                        .commit();

                                fetchingDataDialog.setVisibility(View.GONE);

                                loadingInfo = false;
                            },
                            error -> {
                                Log.d(TAG, "Error in fetchCommandPage");
                                error.printStackTrace();
                                loadingInfo = false;
                            });

            disposables.add(disposable);
        }
    };

}
