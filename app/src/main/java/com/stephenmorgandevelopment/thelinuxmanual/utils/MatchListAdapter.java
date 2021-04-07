package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlAdapter;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;
import okhttp3.Response;

public class MatchListAdapter extends BaseAdapter {
    public static final String TAG = MatchListAdapter.class.getSimpleName();
    private static final SpannableString EMPTY_HTML_SPAN =
            SpannableString.valueOf(Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY));

    private final UbuntuRepository repository;

    private final LifecycleOwner lifecycleOwner;
    private final List<LiveData<String>> liveDataList;

    private final List<SimpleCommand> matches;
    private final LayoutInflater inflater;
    private final LinearLayout.LayoutParams layoutParams;

    public MatchListAdapter(Context ctx) {
        matches = new ArrayList<>();
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = 5;

        lifecycleOwner = (LifecycleOwner) ctx;
        liveDataList = new ArrayList<>();

        repository = UbuntuRepository.getInstance();
    }

    public void setMatches(List<SimpleCommand> matches) {
        this.matches.clear();
        this.matches.addAll(matches);
    }

    public void clear() {
        removeObservers();
        this.matches.clear();
    }

    public void removeObservers() {
        for(LiveData<String> lv : liveDataList) {
            lv.removeObservers(lifecycleOwner);
        }
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

        SpannableString description = SpannableString.valueOf(Html.fromHtml(match.getDescription(), Html.FROM_HTML_MODE_LEGACY));

        if (!description.equals(EMPTY_HTML_SPAN)) {
            descriptionView.setText(description);
        } else {
            descriptionView.setText(R.string.fetching_data);

            final LiveData<String> liveDescription = repository.updateDescription(match);
            final Observer<String> disposableObserver = updatedDescription -> setFormattedText(updatedDescription, descriptionView);

            liveDescription.observe(lifecycleOwner, disposableObserver);
            liveDataList.add(liveDescription);
        }

        return view;
    }

    private void setFormattedText(String text, TextView descriptionView) {
        Spanned htmlText = Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);

        Spanned spannedText = htmlText.length() > 150
                ? (Spanned) htmlText.subSequence(0, 149)
                : htmlText;

        descriptionView.setText(spannedText, TextView.BufferType.SPANNABLE);
    }
}
