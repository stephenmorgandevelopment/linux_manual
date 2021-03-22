package com.stephenmorgandevelopment.thelinuxmanual;

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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stephenmorgandevelopment.thelinuxmanual.databases.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;
import com.stephenmorgandevelopment.thelinuxmanual.utils.PrimaryPagerAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CommandLookupFragment lookupFragment;
    private Toolbar toolbar;
    private TextView progressDialog;
    private ScrollView progressScroller;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private PrimaryPagerAdapter pagerAdapter;

    private MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Helpers.init(MainActivity.this.getApplication());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = findViewById(R.id.progressTextView);
        progressScroller = findViewById(R.id.progressScroller);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        tabLayout.setupWithViewPager(viewPager);

        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        String title = getString(R.string.app_name) + " - " + Ubuntu.getReleaseString();
        toolbar.setTitle(title);

        viewModel.getAddPageData().observe(this, updatePagerAdapterObserver);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (DatabaseHelper.hasDatabase() && DatabaseHelper.getInstance().hasData() && !CommandSyncService.isWorking()) {
            addLookupFragment();
        } else {
            startDatabaseSync();
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

        if (lookupFragment != null) {
            lookupFragment.cleanup();
        }

        lookupFragment = null;

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
                        DatabaseHelper.getInstance().wipeTable();

//                        Intent intent = new Intent();
//                        intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);
//
//                        CommandSyncService.enqueueWork(MainActivity.this, intent);

//                        clearFragments();
                        startDatabaseSync();
                        resetScreen();

                        MainActivity.this.recreate();
                    } else {
                        Toast.makeText(MainActivity.this, "Must be connected to the internet.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Already working on it.", Toast.LENGTH_LONG).show();
                }

                return true;
            case R.id.artful:
                changeRelease(Ubuntu.Release.ARTFUL);
                return true;
            case R.id.bionic:
                changeRelease(Ubuntu.Release.BIONIC);
                return true;
            case R.id.cosmic:
                changeRelease(Ubuntu.Release.COSMIC);
                return true;
            case R.id.disco:
                changeRelease(Ubuntu.Release.DISCO);
                return true;
            case R.id.eoan:
                changeRelease(Ubuntu.Release.EOAN);
                return true;
            case R.id.focal:
                changeRelease(Ubuntu.Release.FOCAL);
                return true;
            case R.id.groovy:
                changeRelease(Ubuntu.Release.GROOVY);
                return true;
            case R.id.hirsute:
                changeRelease(Ubuntu.Release.HIRSUTE);
                return true;
            case R.id.precise:
                changeRelease(Ubuntu.Release.PRECISE);
                return true;
            case R.id.trusty:
                changeRelease(Ubuntu.Release.TRUSY);
                return true;
            case R.id.xenial:
                changeRelease(Ubuntu.Release.XENIAL);
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

        if (fragMan.getFragments().size() == 1) {
            fragMan.popBackStack();
        }

        super.onBackPressed();
    }

    private final Observer<Command> updatePagerAdapterObserver = new Observer<Command>() {
        @Override
        public void onChanged(Command command) {
            pagerAdapter.addPage(command);
            viewModel.addCommandToCommandList(command);
            pagerAdapter.notifyDataSetChanged();
        }
    };

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

    protected void changeRelease(Ubuntu.Release release) {
        Preferences.setRelease(release.getName());
        Ubuntu.setRelease(release);
        DatabaseHelper.changeTable(release.getName());

        resetScreen();
        MainActivity.this.recreate();
    }

    private void addLookupFragment() {
        viewPager.setVisibility(View.VISIBLE);

        lookupFragment = CommandLookupFragment.getInstance();

        pagerAdapter = new PrimaryPagerAdapter(getSupportFragmentManager(), lookupFragment);
        if(viewModel.getCommandsList().size() > 0) {
            pagerAdapter.addAllPages(viewModel.getCommandsList());
        }

        viewPager.setAdapter(pagerAdapter);
    }

    private void resetScreen() {
        viewPager.setVisibility(View.GONE);
        viewPager.setAdapter(null);

        pagerAdapter = null;
        lookupFragment = null;
    }

    private void clearFragments() {
        FragmentManager fragMan = getSupportFragmentManager();
        List<Fragment> fragments = fragMan.getFragments();
        for (Fragment frag : fragments) {
            fragMan.popBackStack();
        }
    }

    public PrimaryPagerAdapter getPagerAdapter() {
        return pagerAdapter;
    }

    public void removePage(Command command) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
//        pagerAdapter.destroyItem();
        pagerAdapter.removePage(command);
        pagerAdapter.notifyDataSetChanged();
    }

    public TabLayout getTabLayout() {return tabLayout;}

    public void morphTabs() {
        for(int i = 0; i < pagerAdapter.getCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);

            tab.setCustomView(R.layout.tab_layout);

            tab.view.findViewById(R.id.imageView).setOnClickListener((view) -> {
                // TODO Close tab.
            });
        }
    }

}
