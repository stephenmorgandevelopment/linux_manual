package com.stephenmorgandevelopment.thelinuxmanual.network;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface HttpClientService {

    @GET("{release}/{languageCode}/{manN}/{commandPage}")
    Observable<Response<String>> fetchUbuntuManPage(@Path("release") String release, @Path("languageCode") String languageCode, @Path("manN") String manN, @Path("commandPage") String commandPage);

    @GET("{release}/{languageCode}/{manN}")
    Observable<Response<String>> fetchUbuntuManList(@Path("release") String release, @Path("languageCode") String languageCode, @Path("manN") String manN);

}
