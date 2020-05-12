package com.stephenmorgandevelopment.thelinuxmanual.network;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface HttpClientService {

//    @GET("{release}/{languageCode}/{manN}/{commandPage}")
//    Single<Response<String>> fetchUbuntuManPage(@Path("release") String release, @Path("languageCode") String languageCode, @Path("manN") String manN, @Path("commandPage") String commandPage);

    @GET("{release}/{languageCode}/{path}")
    Single<Response<String>> fetchUbuntuManPage(@Path("release") String release, @Path("languageCode") String languageCode, @Path("path") String path);

    @GET("{release}/{languageCode}/{manN}")
    Single<Response<String>> fetchUbuntuManList(@Path("release") String release, @Path("languageCode") String languageCode, @Path("manN") String manN);

    @GET("{release}/{languageCode}")
    Single<Response<String>> fetchUbuntuDirPage(@Path("release") String release, @Path("languageCode") String languageCode);

}
