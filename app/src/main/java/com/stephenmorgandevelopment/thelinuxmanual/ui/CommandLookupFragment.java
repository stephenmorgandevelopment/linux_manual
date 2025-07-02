package com.stephenmorgandevelopment.thelinuxmanual.ui;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.Toast;
//
//import androidx.activity.OnBackPressedCallback;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlApiConverter.Release;
//import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService;
//import com.stephenmorgandevelopment.thelinuxmanual.R;
//import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlApiConverter;
//import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
//import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
//import com.stephenmorgandevelopment.thelinuxmanual.utils.MatchListAdapter;
//import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;
//import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.CommandLookupViewModel;
//import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.MainActivityViewModel;
//
//import java.util.List;
//import java.util.Objects;
//
//import dagger.hilt.android.AndroidEntryPoint;
//
//@AndroidEntryPoint
//public class CommandLookupFragment extends Fragment
//		implements TextWatcher, AdapterView.OnItemClickListener {
//	public static final String TAG = CommandLookupFragment.class.getSimpleName();
//
//	private EditText searchText;
//	private MatchListAdapter matchListAdapter;
//
//	private MainActivityViewModel viewModel;
//	private CommandLookupViewModel lookupModel;
//
//	private Menu optionsMenu;
//
//	public static CommandLookupFragment newInstance() {
//		return new CommandLookupFragment();
//	}
//
//	@Override
//	public void onAttach(@NonNull Context context) {
//		super.onAttach(context);
//
//		requireActivity().getOnBackPressedDispatcher().addCallback(this, clearCommandsSearchOnBack);
//	}
//
//	@Override
//	public void onCreate(@Nullable Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		viewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
//		lookupModel = new ViewModelProvider(requireActivity()).get(CommandLookupViewModel.class);
//
//		setHasOptionsMenu(true);
//	}
//
//	@Nullable
//	@Override
//	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//		View view = inflater.inflate(R.layout.command_lookup_fragment, container, false);
//		view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//
//		return view;
//	}
//
//	@Override
//	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//		super.onViewCreated(view, savedInstanceState);
//
//		searchText = view.findViewById(R.id.searchText);
//		matchListAdapter = new MatchListAdapter(requireContext());
//
//		ListView matchListView = view.findViewById(R.id.matchList);
//		matchListView.setDividerHeight(5);
//		matchListView.setAdapter(matchListAdapter);
//		matchListView.setOnItemClickListener(this);
//
//		searchText.addTextChangedListener(this);
//
//		lookupModel.getMatchListData().observe(getViewLifecycleOwner(), this::updateMatchList);
//	}
//
//	@Override
//	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//		super.onCreateOptionsMenu(menu, inflater);
//		inflater.inflate(R.menu.toolbar_menu, menu);
//
//		optionsMenu = menu;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//		if(item.getItemId() == R.id.refreshMenuBtn) {
//			if (CommandSyncService.isWorking()) {
//				Toast.makeText(getContext(), "Sync service is already running.", Toast.LENGTH_LONG).show();
//				return true;
//			}
//
//			if (Helpers.hasInternet()) {
//				((MainActivity_Legacy)requireActivity()).reSyncDataAndReset();
//			} else {
//				Toast.makeText(getContext(), "Must be connected to the internet.", Toast.LENGTH_LONG).show();
//			}
//
//			return true;
//		} else if(item.getGroupId() == R.id.releaseSubMenu) {
//			viewModel.changeRelease(String.valueOf(item.getTitle()));
//
//			((MainActivity_Legacy)requireActivity()).clearPagerAndCommandList();
//			requireActivity().recreate();
//			return true;
//		}
//
//		return super.onOptionsItemSelected(item);
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//
//		ActionBar actionbar = Objects.requireNonNull(
//				((AppCompatActivity) getActivity())).getSupportActionBar();
//
/// /		if (actionbar != null) {
/// /			actionbar.setTitle("Search - ".concat(Preferences.INSTANCE.getRelease()));
/// /		}
//
//		if (matchListAdapter == null) {
//			matchListAdapter = new MatchListAdapter(requireContext());
//		}
//
//		if (lookupModel.getSearchText() != null) {
//			searchText.setText(lookupModel.getSearchText());
//		}
//	}
//
//	@Override
//	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//	}
//
//	@Override
//	public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//	}
//
//	@Override
//	public void afterTextChanged(Editable s) {
//		if (s.length() >= 2) {
//			lookupModel.searchForMatchesByName(s);
//		} else {
//			lookupModel.setSavedSearchText(null);
//			matchListAdapter.clear();
//			matchListAdapter.notifyDataSetChanged();
//		}
//	}
//
//	public void updateMatchList(List<SimpleCommand> list) {
//		if (list.size() > 0) {
//			matchListAdapter.setMatches(list);
//			matchListAdapter.notifyDataSetChanged();
//		}
//	}
//
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		long cmdId = matchListAdapter.getItemId(position);
//
//		if (viewModel.isLoading(cmdId) || viewModel.commandsListHasId(cmdId)) {
//			return;
//		}
//
//		viewModel.setLoading(cmdId, true);
//		viewModel.loadManpage(matchListAdapter.getItem(position));
//	}
//
//	public OnBackPressedCallback clearCommandsSearchOnBack = new OnBackPressedCallback(true) {
//		@Override
//		public void handleOnBackPressed() {
//			if (searchText.getText() != null && searchText.length() > 0) {
//				clear();
//			} else {
//				setEnabled(false);
//				requireActivity().onBackPressed();
//			}
//		}
//	};
//
//	public void clear() {
//		lookupModel.setSavedSearchText(null);
//
//		if (matchListAdapter != null) {
//			matchListAdapter.clear();
//			matchListAdapter.notifyDataSetChanged();
//		}
//
//		if (searchText != null) {
//			searchText.setText("");
//		}
//	}
//
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//
//		lookupModel.setSavedSearchText(String.valueOf(searchText.getText()));
//	}
//}
