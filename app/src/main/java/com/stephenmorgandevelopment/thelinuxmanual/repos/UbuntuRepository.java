package com.stephenmorgandevelopment.thelinuxmanual.repos;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService;
import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.data.LocalStorage;
import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlApiConverter;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.Release;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;

public class UbuntuRepository implements ManPageRepository {
	private static UbuntuRepository instance;
	public static final String TAG = "UbuntuRepository";

	public static UbuntuRepository getInstance() {
		if (instance == null) {
			instance = new UbuntuRepository();
		}
		return instance;
	}

	private UbuntuRepository() {}

	public Single<Map<String, String>> getCommandData(SimpleCommand simpleCommand) {
		final LocalStorage storage = LocalStorage.getInstance();

		if (storage.hasCommand(simpleCommand.getId())) {
			try {
				return Single.just(storage.loadCommand(simpleCommand.getId()).getData());
			} catch (IOException ioe) {
				Log.i(TAG, "Unexpected file error loading - " + simpleCommand.getName() + ": " + ioe.getMessage());
			}
		}

		if (Helpers.hasInternet()) {
			return fetchCommandData(simpleCommand.getUrl())
					.observeOn(Schedulers.io())
					.doAfterSuccess((dataMap) -> {
						storage.saveCommand(new Command(simpleCommand.getId(), dataMap));
					});
		}

		return Single.error(new Throwable("Must have internet."));
	}

	public Command getCommandFromStorage(SimpleCommand simpleCommand) {
		final LocalStorage storage = LocalStorage.getInstance();

		try {
			return storage.loadCommand(simpleCommand.getId());
		} catch (IOException ioe) {
			Log.i(TAG, "Unexpected file error loading - " + simpleCommand.getName() + ": " + ioe.getMessage());
		}

		return null;
	}

	public Single<Map<String, String>> fetchCommandData(String pageUrl) {
		return HttpClient.fetchCommandManPage(pageUrl)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.computation())
				.flatMap(UbuntuHtmlApiConverter::crawlForCommandInfo);
	}

	public synchronized Single<List<SimpleCommand>> getPartialMatches(String searchQuery) {
		return Single.just(
				DatabaseHelper.getInstance().partialMatches(searchQuery))
				.subscribeOn(Schedulers.io());
	}

	public Single<SimpleCommand> addDescription(SimpleCommand match) {
		return HttpClient.fetchDescription(match)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.computation())
				.flatMap(response -> mapResponseToSimpleCommand(response, match))
				.observeOn(Schedulers.io())
				.doAfterSuccess(simpleCommand ->
						DatabaseHelper.getInstance().updateCommand(simpleCommand));
	}

	private Single<SimpleCommand> mapResponseToSimpleCommand(
				Response response, SimpleCommand match)
			throws IOException {

		//TODO or not
		//TODO Blob n the question???

		//TODO Blob = more effecient:
		return Single.just(
				match.setDescriptionReturnSimpleCommand(
						UbuntuHtmlApiConverter.crawlForDescription(response.body().string())));

		//TODO NonBlob = better readable:
//		String description = UbuntuHtmlAdapter.crawlForDescription(response.body().string());
//		match.setDescriptionReturnSimpleCommand(description);
//
//		return Single.just(match);
	}

	public LiveData<String> launchSyncService() {
		if (!CommandSyncService.isWorking()) {
			Intent intent = new Intent();
			intent.putExtra(CommandSyncService.DISTRO, UbuntuHtmlApiConverter.NAME);

			return CommandSyncService.enqueueWork(Helpers.getApplicationContext(), intent);
		}

		return CommandSyncService.getProgress();
	}
}
