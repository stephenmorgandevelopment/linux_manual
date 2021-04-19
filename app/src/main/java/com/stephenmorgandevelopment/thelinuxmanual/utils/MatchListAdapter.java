package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.CommandLookupViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MatchListAdapter extends BaseAdapter {
	public static final String TAG = MatchListAdapter.class.getSimpleName();
	private static final SpannableString EMPTY_HTML_SPAN = new SpannableString("");

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
		holder.descriptionView.setText(getFetchDescription(match, holder.descriptionView));

		return view;
	}

	private View getMakeView(View convertView, ViewGroup parent) {
		if (convertView == null) {
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

	private SpannableString getFetchDescription(SimpleCommand match, TextView tv) {
		SpannableString description =
				SpannableString.valueOf(Html.fromHtml(
						match.getDescription(),
						Html.FROM_HTML_MODE_LEGACY));

		if (description.equals(EMPTY_HTML_SPAN) && Helpers.hasInternet()) {
			CommandLookupViewModel.addDisposable(
					UbuntuRepository.getInstance()
							.addDescription(match)
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(
									simpleCommand ->
											tv.setText(new SpannableString(simpleCommand.getDescription())),
									error ->
											Log.e(TAG, error.getMessage())));

			return new SpannableString(Helpers.text(R.string.fetching_data));
		} else if (description.equals(EMPTY_HTML_SPAN) && !Helpers.hasInternet()) {
			return new SpannableString(Helpers.text(R.string.no_internet_descriptions));
		}

		return description;
	}

	private static class MatchViewHolder {
		final TextView headerView, descriptionView;

		public MatchViewHolder(TextView headerView, TextView descriptionView) {
			this.headerView = headerView;
			this.descriptionView = descriptionView;
		}
	}
}
