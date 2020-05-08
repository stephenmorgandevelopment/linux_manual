package com.stephenmorgandevelopment.thelinuxmanual;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout fragmentContainer;
    private Toolbar toolbar;
    private ImageButton backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentContainer = findViewById(R.id.fragmentContainer);
        toolbar = findViewById(R.id.toolbar);
        backArrow = findViewById(R.id.backArrow);


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
