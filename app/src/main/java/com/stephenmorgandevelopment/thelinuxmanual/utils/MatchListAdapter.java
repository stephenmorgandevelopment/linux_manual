package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.databases.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MatchListAdapter extends BaseAdapter {
    public static final String TAG = MatchListAdapter.class.getSimpleName();

    public static CompositeDisposable disposables;

    private List<SimpleCommand> matches;
    private LayoutInflater inflater;

    public MatchListAdapter(Context ctx) {
        matches = new ArrayList<>();
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        disposables = new CompositeDisposable();
    }

    public void setMatches(List<SimpleCommand> matches) {
        if (this.matches == null) {
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
        final TextView descriptionView = view.findViewById(R.id.matchListDescription);
        final SimpleCommand match = matches.get(position);

        ((TextView) view.findViewById(R.id.matchListCommand)).setText(match.getName());

        String description = match.getDescription();

        if(!description.equals("")) {
            descriptionView.setText(description);
        } else {
            descriptionView.setText(R.string.fetching_data);

            Disposable disposable = fetchDescription(match)
                    .subscribeOn(Schedulers.computation())
                    .flatMap(response -> {
                        Log.d(TAG, "Inside flatMap for " + match.getName());
                        Ubuntu.addDescriptionToSimpleCommand(match, response.body().string());

                        return Single.just(match);
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess(success -> {
                        descriptionView.setText(match.getDescription());
                    })
                    .doOnError(error -> {
                        Log.e(TAG, "Error pulling description for " + match.getName());
                        error.printStackTrace();
                    })
                    .observeOn(Schedulers.io())
//                    .doFinally(() -> {
//                        Log.i(TAG, "Updating database for " + match.getName() + "id=" + match.getId());
//                        DatabaseHelper.getInstance().updateCommand(match);
//                        Log.i(TAG, "Running on thread: " + Thread.currentThread().getName() + " : pid-" + Thread.currentThread().getId());
//                    })
                    .subscribe(response -> {
                                Log.i(TAG, "Updating database for " + match.getName() + "id=" + match.getId());
                                DatabaseHelper.getInstance().updateCommand(match);
                                Log.i(TAG, "Running on thread: " + Thread.currentThread().getName() + " : pid-" + Thread.currentThread().getId());
                            }
                            , error -> {
                                Log.e(TAG, error.toString());
                            });

            disposables.add(disposable);
        }

        return view;
    }


    Single<Response> fetchDescription(SimpleCommand command) {
        Log.d(TAG, "In fetchDescription for " + command.getName());

        Request request = new Request.Builder().url(command.getUrl()).build();
        return Single.defer(() -> Single.just(HttpClient.getClient().newCall(request).execute()));
    }
}
