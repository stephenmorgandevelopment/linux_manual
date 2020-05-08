package com.stephenmorgandevelopment.thelinuxmanual;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;

public class CommandSyncService extends IntentService {

    public static final String DISTRO = "distro";

    public CommandSyncService() {
        super("LinuxManualSync");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final String[] distroNames = getResources().getStringArray(R.array.distro_names);

        String distro = "error";
        if(intent != null && intent.getExtras() != null) {
            distro = intent.getExtras().getString(DISTRO);
            if(distro == null || distro.isEmpty()) {
                return;
            }
        }

        if(distro.equalsIgnoreCase(Ubuntu.NAME)) {
            syncSimpleCommandsUbuntu();
        }

    }


    private void syncSimpleCommandsUbuntu() {
        HttpClient httpClient = HttpClient.getInstance(Ubuntu.BASE_URL);


    }
}
