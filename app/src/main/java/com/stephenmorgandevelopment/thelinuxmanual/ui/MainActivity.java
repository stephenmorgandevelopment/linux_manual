package com.stephenmorgandevelopment.thelinuxmanual.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService;
import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.data.LocalStorage;
import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlAdapter;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;
import com.stephenmorgandevelopment.thelinuxmanual.utils.PrimaryPagerAdapter;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.CommandLookupViewModel;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
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

		viewModel = new ViewModelProvider((ViewModelStoreOwner) MainActivity.this).get(MainActivityViewModel.class);

		String title = getString(R.string.app_name) + " - " + UbuntuHtmlAdapter.getReleaseString();
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setTitle(title);

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
			addLookupFragment();
		} else if (Helpers.hasInternet()) {
			startDatabaseSync();

			progressDialog.setVisibility(View.VISIBLE);
			progressScroller.setVisibility(View.VISIBLE);
		} else {
			displayNoDataNoInternetMessage();
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

			lookupFragment = null;
		}

		Helpers.cleanup();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(menu.findItem(R.id.refreshMenuBtn) == null) {
			getMenuInflater().inflate(R.menu.toolbar_menu, menu);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refreshMenuBtn:
				if (!CommandSyncService.isWorking()) {
					if (Helpers.hasInternet()) {
						reSyncDataAndReset();
					} else {
						Toast.makeText(MainActivity.this, "Must be connected to the internet.", Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(this, "Already working on it.", Toast.LENGTH_LONG).show();
				}

				return true;
			case R.id.artful:
				changeRelease(UbuntuHtmlAdapter.Release.ARTFUL);
				return true;
			case R.id.bionic:
				changeRelease(UbuntuHtmlAdapter.Release.BIONIC);
				return true;
			case R.id.cosmic:
				changeRelease(UbuntuHtmlAdapter.Release.COSMIC);
				return true;
			case R.id.disco:
				changeRelease(UbuntuHtmlAdapter.Release.DISCO);
				return true;
			case R.id.eoan:
				changeRelease(UbuntuHtmlAdapter.Release.EOAN);
				return true;
			case R.id.focal:
				changeRelease(UbuntuHtmlAdapter.Release.FOCAL);
				return true;
			case R.id.groovy:
				changeRelease(UbuntuHtmlAdapter.Release.GROOVY);
				return true;
			case R.id.hirsute:
				changeRelease(UbuntuHtmlAdapter.Release.HIRSUTE);
				return true;
			case R.id.precise:
				changeRelease(UbuntuHtmlAdapter.Release.PRECISE);
				return true;
			case R.id.trusty:
				changeRelease(UbuntuHtmlAdapter.Release.TRUSY);
				return true;
			case R.id.xenial:
				changeRelease(UbuntuHtmlAdapter.Release.XENIAL);
				return true;
			default:

				break;
		}
		return super.onOptionsItemSelected(item);
	}

	boolean exitClick = false;

	@Override
	public void onBackPressed() {
		FragmentManager fragMan = getSupportFragmentManager();

		if (viewModel.isLoading(-1L) && !exitClick) {
			Toast.makeText(this, "Syncing data, press again to exit.", Toast.LENGTH_LONG).show();
			exitClick = true;
			return;
		}

		if (viewPager.getCurrentItem() != 0) {
			viewPager.setCurrentItem(0);
			return;
		}

		super.onBackPressed();
	}

	private void reSyncDataAndReset() {
		clearPagerAndCommandList();
		LocalStorage.getInstance().wipeAll();
		DatabaseHelper.getInstance().wipeTable();

		startDatabaseSync();
		recreate();
	}

	private void clearPagerAndCommandList() {
		viewPager.setCurrentItem(0);
		viewModel.clearAddPageData();

		for (Command command : viewModel.getCommandsList()) {
			pagerAdapter.removePage(command.getId());
			pagerAdapter.notifyDataSetChanged();
		}

		viewModel.clearCommandsList();
	}

	protected void changeRelease(UbuntuHtmlAdapter.Release release) {
		Preferences.setRelease(release.getName());
		UbuntuHtmlAdapter.setRelease(release);
		DatabaseHelper.changeTable(release.getName());

		clearPagerAndCommandList();
		LocalStorage.getInstance().wipeAll();

		startDatabaseSync();
		recreate();
	}

	private void startDatabaseSync() {
		viewModel.syncDatabase();

		viewModel.getSyncProgress().observe(this, (syncProgress) -> {
			if (syncProgress.equals(CommandSyncService.COMPLETE_TAG)) {
				progressDialog.setVisibility(View.GONE);
				progressScroller.setVisibility(View.GONE);

				addLookupFragment();

				viewModel.getSyncProgress().removeObservers(this);
				return;
			}

			progressDialog.append(syncProgress);
		});
	}

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

		new TabLayoutMediator(
				tabLayout,
				viewPager,
				(tab, position) ->
						tab.setText(pagerAdapter.getPageTitle(position))
		).attach();
	}

	private void displayNoDataNoInternetMessage() {
		progressDialog.setVisibility(View.VISIBLE);
		progressScroller.setVisibility(View.VISIBLE);

		progressDialog.setTextSize(30f);
		progressDialog.setText(R.string.no_data_no_internet);
	}
}
