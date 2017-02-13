package com.example.deathcode.downloadts.utils;

import android.net.Uri;
import android.util.Log;

/**
 * Created by rajora_sd on 2/13/2017.
 */


public class Utility{

    public static String getUrl(String query)
    {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("youtube0973.herokuapp.com")
                .appendPath("api")
                .appendPath("info")
                .appendQueryParameter("url", query)
                .appendQueryParameter("flatten", "False");
        String myUrl = builder.build().toString();
        Log.e("LINK", myUrl);
        return  myUrl;
    }

}