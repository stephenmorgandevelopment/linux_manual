package com.stephenmorgandevelopment.thelinuxmanual.network;

import okhttp3.Response;

public interface HttpApi {
    HttpApi instance = null;

    /** Implement singleton pattern / creator pattern and have only one HttpApi instance.
     *
     * @return The single instance to make Http / Api call on.
     */
//    static HttpApi getInstance() {
//        if(instance == null) {
//
//        }
//        return instance;
//    }

    Object fetchSimpleCommands();

    Object fetchManPage(String pageUrl);

}
