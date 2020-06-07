package com.stephenmorgandevelopment.thelinuxmanual;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.stephenmorgandevelopment.thelinuxmanual.databases.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout fragmentContainer;
    private Toolbar toolbar;
    private TextView progressDialog;
    private ScrollView progressScroller;

    public static volatile boolean working = false;

    private SyncDialogMonitor syncDialogMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Helpers.init(MainActivity.this.getApplication());

        fragmentContainer = findViewById(R.id.fragmentContainer);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        String title = getString(R.string.app_name) + " - " + Ubuntu.getReleaseString();     //.concat(Ubuntu.getReleaseString());
//        toolbar.setTitle(title);

        progressDialog = findViewById(R.id.progressTextView);
        progressScroller = findViewById(R.id.progressScroller);

    }


    @Override
    protected void onResume() {
        super.onResume();

        if(DatabaseHelper.hasDatabase()  && DatabaseHelper.getInstance().getCommandById(1) != null) {
            FragmentManager manager = getSupportFragmentManager();
            Fragment searchFragment = CommandLookupFragment.getInstance();

            manager.beginTransaction()
                    .add(R.id.fragmentContainer, searchFragment, CommandLookupFragment.TAG)
                    .addToBackStack(CommandLookupFragment.TAG)
                    .commit();
        } else {
            progressDialog.setVisibility(View.VISIBLE);
            progressScroller.setVisibility(View.VISIBLE);

            progressDialog.setText("Running initial sync to build local command database.");

            Intent intent = new Intent();
            intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);

            CommandSyncService.enqueueWork(MainActivity.this, intent);

            working = true;
            syncDialogMonitor = new SyncDialogMonitor();
            syncDialogMonitor.start();
        }

        String title = getString(R.string.app_name) + " - " + Ubuntu.getReleaseString();
        toolbar.setTitle(title);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(syncDialogMonitor != null && syncDialogMonitor.isAlive()) {
            syncDialogMonitor.interrupt();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(!working) {
            DatabaseHelper.getInstance().close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //Menu Code to try later.  With the removal of onPrepareOptionsMenu
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

     */

    private boolean menuCreated = false;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(!menuCreated) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
            menuCreated = true;
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
                if(!working) {
                    working = true;

                    Intent intent = new Intent();
                    intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);

                    CommandSyncService.enqueueWork(MainActivity.this, intent);
                } else {
                    Toast.makeText(this, "Already working on it.", Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.changeReleaseBtn:
                //TODO Create a list menu with releases in man pages.


                break;
            case R.id.artful:
                changeRelease(Ubuntu.Release.ARTFUL);
                break;
            case R.id.bionic:
                changeRelease(Ubuntu.Release.BIONIC);
                break;
            case R.id.cosmic:
                changeRelease(Ubuntu.Release.COSMIC);
                break;
            case R.id.disco:
                changeRelease(Ubuntu.Release.DISCO);
                break;
            case R.id.eoan:
                changeRelease(Ubuntu.Release.EOAN);
                break;
            case R.id.focal:
                changeRelease(Ubuntu.Release.FOCAL);
                break;
            case R.id.groovy:
                changeRelease(Ubuntu.Release.GROOVY);
                break;
            case R.id.precise:
                changeRelease(Ubuntu.Release.PRECISE);
                break;
            case R.id.trusty:
                changeRelease(Ubuntu.Release.TRUSY);
                break;
            case R.id.xenial:
                changeRelease(Ubuntu.Release.XENIAL);
                break;

//            case R.id.storeOfflineBtn:
                //TODO Begin by pulling all commands and caching them in database with description.
                //TODO Download all data in background and cache as Json String.

//                break;
            default:

                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        if(getSupportFragmentManager().getFragments().size() > 2) {
//            getSupportFragmentManager().popBackStack();
//        } else {
//            super.onBackPressed();
//        }
    }

    private void changeRelease(Ubuntu.Release release) {
        Preferences.setRelease(release.getName());
        Ubuntu.setRelease(release.getName());
        DatabaseHelper.changeTable(release.getName());

        FragmentManager fragMan = getSupportFragmentManager();
        List<Fragment> fragments = fragMan.getFragments();
        for(Fragment frag : fragments) {
            fragMan.popBackStack();
        }

//        fragmentContainer.removeAllViews();

        MainActivity.this.recreate();
    }

    private class SyncDialogMonitor extends Thread {
        int counter = 0;
        String progress = "";

        @Override
        public void run() {
            while(working) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Log.e("MainActivity", "Progress dialog thread interrupted while working.");
                }

                if(!progress.equals(CommandSyncService.getSyncProgress())) {
                    progress = CommandSyncService.getSyncProgress();
                    runOnUiThread(() -> progressDialog.append(progress));
                }

                if(++counter == 20) {
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

                FragmentManager manager = getSupportFragmentManager();
                Fragment searchFragment = CommandLookupFragment.getInstance();

                manager.beginTransaction().add(R.id.fragmentContainer, searchFragment, CommandLookupFragment.TAG).commit();
            });
        }
    }
}
