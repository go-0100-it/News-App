package com.waters89gmail.dave.newsapp;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by WatersD on 8/2/2016.
 */
public class ArticleLoader extends AsyncTaskLoader<JSONObject>{

    String searchApiUrl = "";

    public ArticleLoader(Context context,String url) {
        super(context);

        this.searchApiUrl = url;
    }

    @Override
    public JSONObject loadInBackground() {

        //Calling method to create a URL from String.
        URL url = createUrl(searchApiUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "IOException thrown, problem making Http Request", e);
        }

        //Checking to see if response from Http Request is not null, if null return early with a null value.
        //If not null creating a JSONObject from HTTP Request response string and handling exception.
        JSONObject baseJsonResponse = null;
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }else{
            try {
                baseJsonResponse = new JSONObject(jsonResponse);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Returning JSONObject or null value in MainActivity @ (@Override onLoadFinished)
        return baseJsonResponse;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(Constants.LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            Log.e(Constants.LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
                Log.e(Constants.LOG_TAG, "Reading from stream");
            } else {
                Log.e(Constants.LOG_TAG, "Error with HttpRequest.  Error response code: " + urlConnection.getResponseCode());

            }
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "IOException thrown, problem retriving guardian JSON results", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}
