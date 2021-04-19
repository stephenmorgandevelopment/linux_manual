package com.stephenmorgandevelopment.thelinuxmanual.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService;
import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.data.LocalStorage;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.PrimaryPagerAdapter;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.CommandLookupViewModel;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.MainActivityViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
	private CommandLookupFragment lookupFragment;
	private TextView progressDialog;
	private ScrollView progressScroller;
	private ViewPager2 viewPager;
	private TabLayout tabLayout;
	private PrimaryPagerAdapter pagerAdapter;

	private MainActivityViewModel viewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Helpers.init(MainActivity.this.getApplication());
		viewModel = new ViewModelProvider(MainActivity.this).get(MainActivityViewModel.class);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setTitle(R.string.app_name);

		progressDialog = findViewById(R.id.progressTextView);
		progressScroller = findViewById(R.id.progressScroller);

		viewPager = findViewById(R.id.viewPager);
		tabLayout = findViewById(R.id.tabLayout);

		lookupFragment = CommandLookupFragment.newInstance();

		viewModel.getAddPageData().observe(this, onPageAddedObserver);
		viewModel.getOnErrorData().observe(this, onErrorObserver);
	}

	private final Observer<Command> onPageAddedObserver = (command) -> {
		if (command == null || viewModel.getCommandsList().contains(command)) {
			return;
		}

		pagerAdapter.addPage(command.getId(), command.getShortName());
		viewModel.addCommandToCommandList(command);
		pagerAdapter.notifyDataSetChanged();
	};

	private final Observer<Throwable> onErrorObserver = (throwable) ->
			Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();

	@Override
	protected void onResume() {
		super.onResume();

		if (DatabaseHelper.hasDatabase() && DatabaseHelper.getInstance().hasData() && !CommandSyncService.isWorking()) {
			checkSavedStateAndAddLookupFragment();
		} else if (Helpers.hasInternet()) {
			viewModel.syncDatabase();
			viewModel.getSyncProgress().observe(this, syncProgressCallback);

			progressDialog.setVisibility(View.VISIBLE);
			progressScroller.setVisibility(View.VISIBLE);
		} else {
			displayNoDataNoInternetMessage();
		}
	}

	private void checkSavedStateAndAddLookupFragment() {
		if(viewModel.hasSavedState() && viewModel.getCommandsList().size() == 0) {
			Disposable disposable = viewModel.loadSavedCommands()
					.observeOn(AndroidSchedulers.mainThread())
					.doOnComplete(this::addLookupFragment)
					.observeOn(Schedulers.io())
					.subscribe(viewModel::addCommandToCommandList);

			MainActivityViewModel.addDisposable(disposable);
		} else {
			addLookupFragment();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		viewModel.getAddPageData().removeObservers(this);
		viewModel.getOnErrorData().removeObservers(this);

		if (isFinishing()) {
			if (!CommandSyncService.isWorking()) {
				DatabaseHelper.getInstance().close();
			}

			CommandLookupViewModel.cleanup();
			MainActivityViewModel.cleanup();

			lookupFragment = null;
		}

		Helpers.cleanup();
	}

	@Override
	public void onBackPressed() {
		if (viewPager.getCurrentItem() != 0) {
			viewPager.setCurrentItem(0);
			return;
		}

		super.onBackPressed();
	}

	public void reSyncDataAndReset() {
		clearPagerAndCommandList();
		LocalStorage.getInstance().wipeAll();
		DatabaseHelper.getInstance().wipeTable();

		viewModel.syncDatabase();
		recreate();
	}

	public void clearPagerAndCommandList() {
		viewPager.setCurrentItem(0);
		viewModel.clearAddPageData();

		for (Command command : viewModel.getCommandsList()) {
			pagerAdapter.removePage(command.getId());
			pagerAdapter.notifyDataSetChanged();
		}

		viewModel.clearCommandsList();
	}

	private final Observer<String> syncProgressCallback = (syncProgress) -> {
		if (syncProgress.equals(CommandSyncService.COMPLETE_TAG)) {
			progressDialog.setVisibility(View.GONE);
			progressScroller.setVisibility(View.GONE);

			addLookupFragment();

			viewModel.getSyncProgress().removeObservers(this);
			return;
		}

		progressDialog.append(syncProgress);
	};

	public void removePage(long id) {
		removePage(viewModel.getCommandFromListById(id));
	}

	public void removePage(Command command) {
		int position = viewPager.getCurrentItem();

		if (command.getId() == pagerAdapter.getItemId(position)) {
			pagerAdapter.removePage(command.getId());
			viewModel.removeCommandFromCommandList(command);
			pagerAdapter.notifyDataSetChanged();
			viewPager.setCurrentItem(position - 1);
			viewModel.clearAddPageData();
		}
	}

	private void addLookupFragment() {
		viewPager.setVisibility(View.VISIBLE);
		tabLayout.setVisibility(View.VISIBLE);

		pagerAdapter = new PrimaryPagerAdapter(MainActivity.this, lookupFragment);

		if (viewModel.getCommandsList().size() > 0) {
			pagerAdapter.addAllPages(viewModel.getCommandsList());
		}

		viewPager.setAdapter(pagerAdapter);

		new TabLayoutMediator(tabLayout, viewPager,
				(tab, position) -> tab.setText(pagerAdapter.getPageTitle(position))).attach();
	}

	private void displayNoDataNoInternetMessage() {
		progressDialog.setVisibility(View.VISIBLE);
		progressScroller.setVisibility(View.VISIBLE);

		progressDialog.setTextSize(30f);
		progressDialog.setText(R.string.no_data_no_internet);
	}
}
