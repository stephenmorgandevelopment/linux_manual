package com.stephenmorgandevelopment.thelinuxmanual;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.stephenmorgandevelopment.thelinuxmanual.databases.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;
import com.stephenmorgandevelopment.thelinuxmanual.utils.PrimaryPagerAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CommandLookupFragment lookupFragment;
    private Toolbar toolbar;
    private TextView progressDialog;
    private ScrollView progressScroller;
    private SyncDialogMonitor syncDialogMonitor;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private PrimaryPagerAdapter pagerAdapter;

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
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (DatabaseHelper.hasDatabase() && DatabaseHelper.getInstance().hasData() && !CommandSyncService.working) {
            addLookupFragment();
        } else {
            syncDatabase();
        }

        String title = getString(R.string.app_name) + " - " + Ubuntu.getReleaseString();
        toolbar.setTitle(title);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (syncDialogMonitor != null && syncDialogMonitor.isAlive()) {
            syncDialogMonitor.interrupt();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!CommandSyncService.working) {
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
                if (!CommandSyncService.working) {
                    if (Helpers.hasInternet()) {
                        DatabaseHelper.getInstance().wipeTable();

                        Intent intent = new Intent();
                        intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);

                        CommandSyncService.enqueueWork(MainActivity.this, intent);

                        clearFragments();
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

        if(CommandLookupFragment.loadingInfo) {
            Toast.makeText(this, "Syncing, use home or apps button to switch apps.", Toast.LENGTH_LONG).show();
            return;
        }

        if (fragMan.getFragments().size() == 1) {
            fragMan.popBackStack();
        }

        super.onBackPressed();
    }

    private void syncDatabase() {
        progressDialog.setVisibility(View.VISIBLE);
        progressScroller.setVisibility(View.VISIBLE);

        progressDialog.setText(R.string.initial_sync_message);

        if (!CommandSyncService.working) {
            Intent intent = new Intent();
            intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);

            CommandSyncService.enqueueWork(MainActivity.this, intent);
        }

        syncDialogMonitor = new SyncDialogMonitor();
        syncDialogMonitor.start();
    }

    protected void changeRelease(Ubuntu.Release release) {
        Preferences.setRelease(release.getName());
        Ubuntu.setRelease(release);
        DatabaseHelper.changeTable(release.getName());

        clearFragments();
        MainActivity.this.recreate();
    }

    private void addLookupFragment() {
        viewPager.setVisibility(View.VISIBLE);

        lookupFragment = CommandLookupFragment.getInstance();

        pagerAdapter = new PrimaryPagerAdapter(getSupportFragmentManager(), lookupFragment);
        viewPager.setAdapter(pagerAdapter);
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

    private class SyncDialogMonitor extends Thread {
        int counter = 0;
        String progress = "";

        @Override
        public void run() {
            while (CommandSyncService.working) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Log.e("MainActivity", "Progress dialog thread interrupted while working.");
                }

                if (!progress.equals(CommandSyncService.getSyncProgress())) {
                    progress = CommandSyncService.getSyncProgress();
                    runOnUiThread(() -> progressDialog.append(progress));
                }

                if (++counter == 20) {
                    runOnUiThread(() -> progressDialog.append("."));
                    counter = 0;
                }
            }

            runOnUiThread(() -> progressDialog.append("\nFinishing up..."));

            try {
                Thread.sleep(350);
            } catch (InterruptedException e) {
                Log.e("MainActivity", "Progress thread interrupted while finishing up.");
            }

            runOnUiThread(() -> {
                progressDialog.setVisibility(View.GONE);
                progressScroller.setVisibility(View.GONE);

                addLookupFragment();
            });
        }
    }
}
