package com.stephenmorgandevelopment.thelinuxmanual.repos;
//
//import android.content.Intent;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.LiveData;
//
//import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService;
/// /import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper;
//import com.stephenmorgandevelopment.thelinuxmanual.data.LocalStorage;
//import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase;
//import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.UbuntuHtmlApiConverter;
//import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
//import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItemKt;
//import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
//import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
//import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
//
//import java.io.IOException;
//import java.util.Map;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//
//import io.reactivex.Single;
//import io.reactivex.schedulers.Schedulers;
//import okhttp3.Response;
//
//@Singleton
//public class UbuntuRepository_Legacy implements ManPageRepository {
//	public static final String TAG = "UbuntuRepository";
//
//	@NonNull
//	private final HttpClient mHttpClient;
/// /	private final DatabaseHelper mDatabaseHelper;
//	private final SimpleCommandsDatabase mRoomdb;
//	private final LocalStorage mLocalStorage;
//
//	@Inject
//	public UbuntuRepository(
//			@NonNull final HttpClient httpClient,
////			@NonNull final DatabaseHelper databaseHelper,
//			@NonNull final SimpleCommandsDatabase roomdb,
//			@NonNull final LocalStorage localStorage
//	) {
//		mHttpClient = httpClient;
////		mDatabaseHelper = databaseHelper;
//		mRoomdb = roomdb;
//		mLocalStorage = localStorage;
//	}
//
//	public Single<Map<String, String>> getCommandData(@NonNull SimpleCommand simpleCommand) {
//		if (mLocalStorage.hasCommand(simpleCommand.getId())) {
//			try {
//				return Single.just(mLocalStorage.loadCommand(simpleCommand.getId()).getData());
//			} catch (IOException ioe) {
//				Log.i(TAG, "Unexpected file error loading - " + simpleCommand.getName() + ": " + ioe.getMessage());
//			}
//		}
//
//		if (Helpers.hasInternet()) {
//			return fetchCommandData(simpleCommand.getUrl())
//					.observeOn(Schedulers.io())
//					.doAfterSuccess((dataMap) -> {
//						mLocalStorage.saveCommand(new Command(simpleCommand.getId(), dataMap));
//					});
//		}
//
//		return Single.error(new Throwable("Must have internet."));
//	}
//
//	public Command getCommandFromStorage(SimpleCommand simpleCommand) {
//		try {
//			return mLocalStorage.loadCommand(simpleCommand.getId());
//		} catch (IOException ioe) {
//			Log.i(TAG, "Unexpected file error loading - " + simpleCommand.getName() + ": " + ioe.getMessage());
//		}
//
//		return null;
//	}
//
//	public Single<Map<String, String>> fetchCommandData(String pageUrl) {
//		return mHttpClient.fetchCommandManPage(pageUrl)
//				.subscribeOn(Schedulers.io())
//				.observeOn(Schedulers.computation())
//				.flatMap(UbuntuHtmlApiConverter::crawlForCommandInfo);
//	}
//
////	public synchronized Single<List<SimpleCommand>> getPartialMatches(String searchQuery) {
////		return Single.just(
////				mDatabaseHelper.partialMatches(searchQuery))
////				.subscribeOn(Schedulers.io());
////	}
//
////	public Single<SimpleCommand> addDescription(SimpleCommand match) {
////		return mHttpClient.fetchDescription(match)
////				.subscribeOn(Schedulers.io())
////				.observeOn(Schedulers.computation())
////				.flatMap(response -> mapResponseToSimpleCommand(response, match))
////				.observeOn(Schedulers.io())
//////				.doAfterSuccess(mDatabaseHelper::updateCommand);
////				.doAfterSuccess(simpleCommand -> {
////					mRoomdb.dao().insert(MatchingItemKt.toMatchingItem(simpleCommand));
////				});
////	}
//
//	private Single<SimpleCommand> mapResponseToSimpleCommand(
//				Response response, SimpleCommand match)
//			throws IOException {
//
//		return Single.just(
//				match.setDescriptionReturnSimpleCommand(
//						UbuntuHtmlApiConverter.crawlForDescription(response.body().string())));
//
////		final ResponseBody body = response.body();
////		if (body != null) {
////			return Single.just(
////					match.setDescriptionReturnSimpleCommand(
////							UbuntuHtmlApiConverter.crawlForDescription(body.string()
////							)
////					)
////			);
////		} else {
////			return Single.just(match);
////		}
//	}
//
//	public LiveData<String> launchSyncService() {
//		if (!CommandSyncService.isWorking()) {
//			Intent intent = new Intent();
//			intent.putExtra(CommandSyncService.DISTRO, UbuntuHtmlApiConverter.NAME);
//
//			return CommandSyncService.enqueueWork(Helpers.getApplicationContext(), intent);
//		}
//
//		return CommandSyncService.getProgress();
//	}
//}
