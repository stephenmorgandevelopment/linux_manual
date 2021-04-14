package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.service.autofill.FieldClassification;
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

import androidx.annotation.Dimension;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlAdapter;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.models.SingleTextMatch;
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.CommandLookupViewModel;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.MainActivityViewModel;

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
    private static final SpannableString EMPTY_HTML_SPAN = new SpannableString("");
//            SpannableString.valueOf(Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY));

    private final List<SimpleCommand> matches;
    private final LayoutInflater inflater;
    private final LinearLayout.LayoutParams layoutParams;


    public MatchListAdapter(Context ctx) {
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        matches = new ArrayList<>();
    }

    public void setMatches(List<SimpleCommand> matches) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = getMakeView(convertView, parent);
        MatchViewHolder holder = (MatchViewHolder) view.getTag();

        final SimpleCommand match = matches.get(position);

        holder.headerView.setText(match.getName());
        holder.descriptionView.setText(getFetchDescription(match));

        return view;
    }

    private View getMakeView(View convertView, ViewGroup parent) {
        if(convertView == null) {
            View view = inflater.inflate(R.layout.match_list_item, parent, false);
            view.setLayoutParams(layoutParams);

            MatchViewHolder holder = new MatchViewHolder(
                    view.findViewById(R.id.matchListCommand),
                    view.findViewById(R.id.matchListDescription));

            view.setTag(holder);

            return view;
        }

        return convertView;
    }

    private SpannableString getFetchDescription(SimpleCommand match) {
        SpannableString description =
                SpannableString.valueOf(Html.fromHtml(
                        match.getDescription(),
                        Html.FROM_HTML_MODE_LEGACY));

        if (description.equals(EMPTY_HTML_SPAN)) {
            CommandLookupViewModel.addDisposable(
                    UbuntuRepository
                            .getInstance()
                            .addDescription(match)
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally(this::notifyDataSetChanged)
                            .subscribe());

            return new SpannableString(Helpers.text(R.string.fetching_data));
        } else {
            return description;
        }
    }

    private static class MatchViewHolder {
        final TextView headerView, descriptionView;

        public MatchViewHolder(TextView headerView, TextView descriptionView) {
            this.headerView = headerView;
            this.descriptionView = descriptionView;
        }
    }
}
