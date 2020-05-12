package com.stephenmorgandevelopment.thelinuxmanual;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

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

import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout fragmentContainer;
    private Toolbar toolbar;
    private ImageButton backArrow;

    public static volatile boolean working = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Helpers.init(MainActivity.this.getApplication());

        fragmentContainer = findViewById(R.id.fragmentContainer);
        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle(R.string.app_name);
        //backArrow = findViewById(R.id.backArrow);

        Button testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener((v) -> {
            if(!working) {

                Intent intent = new Intent();
                intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);

                CommandSyncService.enqueueWork(MainActivity.this, intent);

                working = true;
            }
        });

        new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.e("MainActivity", "Interrupted");
                    }

                    if(Ubuntu.getCommandsList() != null && Ubuntu.getCommandsList().size() > 0) {
                        runOnUiThread(() -> {
                            ((TextView)findViewById(R.id.testCounter)).setText("SimpleCommands: " + Ubuntu.getCommandsList().size());
                        });
                    }
                }
            }
        }.start();
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
                Intent intent = new Intent();
                intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);

                CommandSyncService.enqueueWork(MainActivity.this, intent);

                break;
            case R.id.changeReleaseBtn:
                //TODO Create a list menu with releases in man pages.

                break;
            case R.id. storeOfflineBtn:
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
}
