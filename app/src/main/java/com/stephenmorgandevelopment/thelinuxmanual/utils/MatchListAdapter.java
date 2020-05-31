package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
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
        final TextView descriptionView = view.findViewById(R.id.matchListDescription);

        ((TextView) view.findViewById(R.id.matchListCommand)).setText(matches.get(position).getName());

        String description = matches.get(position).getDescription();
        descriptionView.setText(description);

        if (description.equals("")) {
            Disposable disposable = fetchDescription(matches.get(position))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMapCompletable(response -> {
                        Ubuntu.addDescriptionToSimpleCommand(matches.get(position), response.body().string());
                        return Completable.complete();
                    })
                    .doOnComplete(() -> {
                        descriptionView.setText(matches.get(position).getDescription());
                    })
                    .subscribe();
            disposables.add(disposable);
        }


        return view;
    }

    Single<Response> fetchDescription(SimpleCommand command) {
        Request request = new Request.Builder().url(command.getUrl()).build();
        try {
            return Single.just(HttpClient.getClient().newCall(request).execute());
        } catch (IOException ioe) {
            Log.e(TAG, "IO error fetching description.");
            ioe.printStackTrace();
        }
        return Single.error(new Throwable());
    }
}
