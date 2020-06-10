package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.databases.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;

import java.util.ArrayList;
import java.util.List;


import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;
import okhttp3.Response;

public class MatchListAdapter extends BaseAdapter {
    public static final String TAG = MatchListAdapter.class.getSimpleName();

    public static CompositeDisposable disposables;
    public static List<Thread> helperThreads;

    private List<SimpleCommand> matches;
    private LayoutInflater inflater;
    private LinearLayout.LayoutParams layoutParams;

    public MatchListAdapter(Context ctx) {
        matches = new ArrayList<>();
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        disposables = new CompositeDisposable();

        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = 5;

        helperThreads = new ArrayList<>();
    }

    public void setMatches(List<SimpleCommand> matches) {
        if (this.matches == null) {
            this.matches = new ArrayList<>();
        }
        this.matches.clear();
        this.matches.addAll(matches);
    }

    public void clear() {
        this.matches.clear();
    }

    @Override
    public int getCount() {
        return matches.size();
    }

    @Override
    public SimpleCommand getItem(int position) {
        return matches.get(position);
    }

    @Override
    public long getItemId(int position) {
        return matches.get(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.match_list_item, null);
        view.setLayoutParams(layoutParams);

        final TextView descriptionView = view.findViewById(R.id.matchListDescription);
        final SimpleCommand match = matches.get(position);

        ((TextView) view.findViewById(R.id.matchListCommand)).setText(match.getName());

        String description = match.getDescription();

        if(!description.equals("")) {
            descriptionView.setText(description);
        } else {
            descriptionView.setText(R.string.fetching_data);

            Thread helperThread = new Thread(() -> {
               updateDescription(descriptionView, match);
            });

            helperThreads.add(helperThread);
            helperThread.start();
        }

        return view;
    }

    private void updateDescription(TextView descriptionView, SimpleCommand match) {
        Disposable disposable = fetchDescription(match)
                .subscribeOn(Schedulers.computation())
                .flatMap(response -> {
                    Ubuntu.addDescriptionToSimpleCommand(match, response.body().string());

                    return Single.just(match);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(success -> {
                    descriptionView.setText(match.getDescription().substring(0, 150));
                })
                .doOnError(error -> {
                    descriptionView.setText("Unable to fetch data at this time.");
                })
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                            DatabaseHelper.getInstance().updateCommand(match);
                        }
                        , error -> {
                            Log.e(TAG, error.toString());
                        });

        disposables.add(disposable);
    }

    Single<Response> fetchDescription(SimpleCommand command) {
        Request request = new Request.Builder().url(command.getUrl()).build();
        return Single.defer(() -> Single.just(HttpClient.getClient().newCall(request).execute()));
    }
}
