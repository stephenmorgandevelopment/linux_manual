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
    private ImageButton backArrow;

    public static volatile boolean working = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Helpers.init(MainActivity.this.getApplication());

        fragmentContainer = findViewById(R.id.fragmentContainer);
        toolbar = findViewById(R.id.toolbar);
//        toolbar = getSupportActionBar();
        setSupportActionBar(toolbar);

        toolbar.setTitle(R.string.app_name);
        //backArrow = findViewById(R.id.backArrow);

//        Button testButton = findViewById(R.id.testButton);
//        testButton.setOnClickListener((v) -> {
//            if(!working) {
//
//                Intent intent = new Intent();
//                intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);
//                intent.putExtra(CommandSyncService.SYNC_TYPE, CommandSyncService.SYNC_NAMES);
//
//                CommandSyncService.enqueueWork(MainActivity.this, intent);
//
//                working = true;
//            }
//        });

    }


    @Override
    protected void onResume() {
        super.onResume();

        if(checkForFiles()) {
            FragmentManager manager = getSupportFragmentManager();
            Fragment searchFragment = CommandLookupFragment.getInstance();

            manager.beginTransaction().add(R.id.fragmentContainer, searchFragment, CommandLookupFragment.TAG).commit();
        } else {
            final TextView progressDialog = findViewById(R.id.testCounter);
            progressDialog.setVisibility(View.VISIBLE);

            Intent intent = new Intent();
            intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);
            intent.putExtra(CommandSyncService.SYNC_TYPE, CommandSyncService.SYNC_NAMES);

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

                        if(Ubuntu.getCommandsList() != null && Ubuntu.getCommandsList().size() > 0) {
                            runOnUiThread(() -> {
                                if(!progress.equals(CommandSyncService.getSyncProgress())) {
                                    progress = CommandSyncService.getSyncProgress();
                                    progressDialog.append(progress);
                                }
                                if(++counter == 10) {
                                    progressDialog.append(".");
                                    counter = 0;
                                }

//                                progressDialog.setText("SimpleCommands: " + Ubuntu.getCommandsList().size());
                            });
                        }
                    }

                    runOnUiThread(() -> progressDialog.setText(R.string.finishing_up));

                    try {
                        Thread.sleep(350);
                    } catch (InterruptedException e) {
                        Log.e("MainActivity", "Progress thread interrupted while finishing up.");
                    }
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

    private boolean menuCreated = false;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(!menuCreated) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
            menuCreated = true;
        }
        return true;
        //return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refreshMenuBtn:
                //TODO Search all commands and cache all data in background.
                if(!working) {
                    working = true;

                    Intent intent = new Intent();
                    intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);
                    intent.putExtra(CommandSyncService.SYNC_TYPE, CommandSyncService.SYNC_NAMES);

                    CommandSyncService.enqueueWork(MainActivity.this, intent);
                } else {
                    Toast.makeText(this, "Already working on it.", Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.changeReleaseBtn:
                //TODO Create a list menu with releases in man pages.

                break;
            case R.id.storeOfflineBtn:
                //TODO Begin by pulling all commands and caching them in database with description.
                //TODO Download all data in background and cache as Json String.

                break;
            default:

                break;
        }
        return true;
    }

    private void toggleBackArrow() {
        if(backArrow.getVisibility() == View.VISIBLE) {
            backArrow.setVisibility(View.GONE);
        } else {
            backArrow.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkForFiles() {
        File jsonDir = Helpers.getFilesDir();
        File[] files = jsonDir.listFiles();

        if(files.length >= 8) {
            return true;
        }

        return false;
    }


}
