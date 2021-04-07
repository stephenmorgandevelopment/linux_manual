package com.stephenmorgandevelopment.thelinuxmanual.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.utils.MatchListAdapter;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.MainActivityViewModel;

import java.util.List;
import java.util.Objects;

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

	private MainActivityViewModel viewModel;

	private final String searchTextTrimRegex = "^(/W/s)$";

	public static CommandLookupFragment newInstance() {
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

		disposables = new CompositeDisposable();

		matchListView.setDividerHeight(5);
		matchListAdapter = new MatchListAdapter(requireContext());

		matchListView.setAdapter(matchListAdapter);
		matchListView.setOnItemClickListener(itemClicked);

		searchText.addTextChangedListener(onChangedText);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requireActivity().getOnBackPressedDispatcher().addCallback(backPressedCallback);

		viewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onPrepareOptionsMenu(@NonNull Menu menu) {
		menu.clear();

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onResume() {
		super.onResume();

		Objects.requireNonNull(
				((AppCompatActivity) requireActivity())
						.getSupportActionBar()).setTitle("Search");

		if (matchListAdapter == null) {
			matchListAdapter = new MatchListAdapter(requireContext());
		}

		if (viewModel.getSearchText() != null) {
			searchText.setText(viewModel.getSearchText());
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (disposables.size() > 0) {
			disposables.clear();
		}
	}

	public void cleanup() {
		if (disposables != null) {
			disposables.clear();
		}

		if(matchListAdapter != null) {
			matchListAdapter.removeObservers();
		}
	}

	TextWatcher onChangedText = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() >= 2) {
				String searchQuery = String.valueOf(s).replaceAll("'", "");
				searchQuery = searchQuery.replaceAll("%", "");
				searchQuery = searchQuery.replaceAll(searchTextTrimRegex, "");

				Disposable disposable = Single.just(
						DatabaseHelper.getInstance().partialMatches(searchQuery))
						.subscribeOn(Schedulers.io())
						.observeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.doOnError(error -> {
							Toast.makeText(getContext(), "Invalid character entered", Toast.LENGTH_LONG).show();
						})
						.subscribe(this::updateMatchList
								, error ->
										Log.d(TAG, "SQL error: " + error.toString())
						);

				viewModel.setSearchText(s.toString());
				disposables.add(disposable);
			} else {
				viewModel.setSearchText(null);
				matchListAdapter.clear();
				matchListAdapter.notifyDataSetChanged();
			}
		}

		private void updateMatchList(List<SimpleCommand> list) {
			if (list.size() > 0) {
				matchListAdapter.setMatches(list);
				matchListAdapter.notifyDataSetChanged();
			}
		}
	};

	AdapterView.OnItemClickListener itemClicked = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            long cmdId = matchListAdapter.getItemId(position);

			if (viewModel.isLoading(cmdId) || viewModel.commandsListHasId(cmdId)) {
				return;
			}

			viewModel.setLoading(matchListAdapter.getItemId(position), true);
			viewModel.loadManpage(matchListAdapter.getItem(position));
		}
	};

	OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (searchText.getText() == null || searchText.length() == 0) {
				setEnabled(false);
				getActivity().onBackPressed();
			}

			clear();
		}
	};

	public void clear() {
		cleanup();

		viewModel.setSearchText(null);
		matchListAdapter.clear();
		matchListAdapter.notifyDataSetChanged();

		searchText.setText("");
	}
}
