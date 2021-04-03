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
import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlAdapter;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;
import com.stephenmorgandevelopment.thelinuxmanual.utils.PrimaryPagerAdapter;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CommandLookupFragment lookupFragment;
    private Toolbar toolbar;
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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = findViewById(R.id.progressTextView);
        progressScroller = findViewById(R.id.progressScroller);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        lookupFragment = new CommandLookupFragment();

        pagerAdapter = new PrimaryPagerAdapter(MainActivity.this, lookupFragment);

        String title = getString(R.string.app_name) + " - " + UbuntuHtmlAdapter.getReleaseString();
        toolbar.setTitle(title);

        viewModel.getAddPageData().observe(this, updatePagerAdapterObserver);
        viewModel.getOnErrorData().observe(this, onErrorObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (DatabaseHelper.hasDatabase() && DatabaseHelper.getInstance().hasData() && !CommandSyncService.isWorking()) {
            addLookupFragment();
        } else if(Helpers.hasInternet()) {
            startDatabaseSync();
        } else {
            displayNoDataNoInternetMessage();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!CommandSyncService.isWorking()) {
            DatabaseHelper.getInstance().close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        viewModel.getAddPageData().removeObserver(updatePagerAdapterObserver);

        if(isFinishing()) {
            if (lookupFragment != null) {
                lookupFragment.cleanup();
            }

            lookupFragment = null;
        }

        Helpers.cleanup();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (toolbar.getMenu().findItem(R.id.refreshMenuBtn) == null) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refreshMenuBtn:
                if (!CommandSyncService.isWorking()) {
                    if (Helpers.hasInternet()) {
                        syncDataAndReset();
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

    @Override
    public void onBackPressed() {
        FragmentManager fragMan = getSupportFragmentManager();

        if(viewModel.isLoading(-1L)) {
            Toast.makeText(this, "Syncing, use home or apps button to switch apps.", Toast.LENGTH_LONG).show();
            return;
        }

        if(viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0);
        }

        super.onBackPressed();
    }

    private void syncDataAndReset() {
        DatabaseHelper.getInstance().wipeTable();
        startDatabaseSync();

        viewModel.clearCommandsList();
        viewModel.clearAddPageData();

        resetScreen();
        MainActivity.this.recreate();
    }

    private final Observer<Command> updatePagerAdapterObserver = new Observer<Command>() {
        @Override
        public void onChanged(Command command) {
            if(command == null || viewModel.getCommandsList().contains(command)) {
                return;
            }

            pagerAdapter.addPage(command.getId(), command.getShortName());
            viewModel.addCommandToCommandList(command);
            pagerAdapter.notifyDataSetChanged();
        }
    };

    private final Observer<Throwable> onErrorObserver = throwable ->
            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();

    public void removePage(long id) {
        removePage(viewModel.getCommandFromListById(id));
    }

    public void removePage(Command command) {
        int position = viewPager.getCurrentItem();
        if(pagerAdapter.getItemId(position) == command.getId()) {
            pagerAdapter.removePage(command.getId());
            viewModel.removeCommandFromCommandList(command);
            pagerAdapter.notifyDataSetChanged();
            viewPager.setCurrentItem(position - 1);
            viewModel.clearAddPageData();
        }
    }

    private void startDatabaseSync() {
        progressDialog.setVisibility(View.VISIBLE);
        progressScroller.setVisibility(View.VISIBLE);

        viewModel.syncDatabase();

        viewModel.getSyncProgress().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.equals(CommandSyncService.COMPLETE_TAG)) {
                    progressDialog.setVisibility(View.GONE);
                    progressScroller.setVisibility(View.GONE);

                    addLookupFragment();
                }

                progressDialog.append(s);
            }
        });
    }

    private void displayNoDataNoInternetMessage() {
        progressDialog.setVisibility(View.VISIBLE);
        progressScroller.setVisibility(View.VISIBLE);

        progressDialog.setTextSize(30f);
        progressDialog.setText(R.string.no_data_no_internet);
    }

    protected void changeRelease(UbuntuHtmlAdapter.Release release) {
        Preferences.setRelease(release.getName());
        UbuntuHtmlAdapter.setRelease(release);
        DatabaseHelper.changeTable(release.getName());

        viewModel.clearCommandsList();
        viewModel.clearAddPageData();

        resetScreen();
        MainActivity.this.recreate();
    }

    private void addLookupFragment() {
        viewPager.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.VISIBLE);

        if(viewModel.getCommandsList().size() > 0) {
            pagerAdapter.addAllPages(viewModel.getCommandsList());
        }

        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(pagerAdapter.getPageTitle(position))
        ).attach();
    }

    private void resetScreen() {
        viewPager.setVisibility(View.GONE);
        viewPager.setAdapter(null);

        pagerAdapter = null;
        lookupFragment = null;
    }
}
