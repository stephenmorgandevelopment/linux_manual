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
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout fragmentContainer;
    private Toolbar toolbar;
//    private ActionBar toolbar;
//    private ImageButton backArrow;

    public static volatile boolean working = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Helpers.init(MainActivity.this.getApplication());

        fragmentContainer = findViewById(R.id.fragmentContainer);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle(R.string.app_name);

    }


    @Override
    protected void onResume() {
        super.onResume();

        if(DatabaseHelper.hasDatabase()) {
            FragmentManager manager = getSupportFragmentManager();
            Fragment searchFragment = CommandLookupFragment.getInstance();

            manager.beginTransaction().add(R.id.fragmentContainer, searchFragment, CommandLookupFragment.TAG).commit();
        } else {
            final TextView progressDialog = findViewById(R.id.testCounter);
            progressDialog.setVisibility(View.VISIBLE);

            progressDialog.setText("Running initial sync to build local command database.");

            Intent intent = new Intent();
            intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);

            CommandSyncService.enqueueWork(MainActivity.this, intent);

            working = true;
            new Thread() {
                int counter = 0;
                String progress = "";

                @Override
                public void run() {
                    while(working) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Log.e("MainActivity", "Progress thread interrupted while working.");
                        }
                            runOnUiThread(() -> {
                                if(!progress.equals(CommandSyncService.getSyncProgress())) {
                                    progress = CommandSyncService.getSyncProgress();
                                    progressDialog.append(progress);
                                }
                                if(++counter == 10) {
                                    progressDialog.append(".");
                                    counter = 0;
                                }
                            });
                    }

                    runOnUiThread(() -> progressDialog.append("\nFinishing up..."));

                    try {
                        Thread.sleep(350);
                    } catch (InterruptedException e) {
                        Log.e("MainActivity", "Progress thread interrupted while finishing up.");
                    }

                    runOnUiThread(() -> {
                        progressDialog.setVisibility(View.GONE);

                        FragmentManager manager = getSupportFragmentManager();
                        Fragment searchFragment = CommandLookupFragment.getInstance();

                        manager.beginTransaction().add(R.id.fragmentContainer, searchFragment, CommandLookupFragment.TAG).commit();
                    });
                }
            }.start();
        }
        //toolbar.inflateMenu(R.menu.toolbar_menu);
    }

    @Override
    protected void onStop() {
        super.onStop();

        DatabaseHelper.getInstance().close();
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
                Toast.makeText(this, "Has sub-menu: "+item.hasSubMenu(), Toast.LENGTH_LONG).show();

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
        if(getSupportFragmentManager().getFragments().size() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
