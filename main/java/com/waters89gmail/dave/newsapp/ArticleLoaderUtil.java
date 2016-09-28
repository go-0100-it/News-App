package com.waters89gmail.dave.newsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by WatersD on 8/2/2016.
 */
public final class ArticleLoaderUtil {

    /**
     * Return an {@link ArticleObject} object by parsing out information
     * about the first article from the input articleJSON string.
     */
    public static ArrayList<ArticleObject> extractFeatureFromJson(JSONObject articleJSON,Context context) {

        final ArrayList<ArticleObject> articles = new ArrayList<>();
        try {
            JSONObject allArticles = articleJSON.getJSONObject("response");
            JSONArray articlesArray = allArticles.getJSONArray("results");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putInt(Constants.TOTAL_PAGES, allArticles.getInt("pages"));
            editor.putInt(Constants.CURRENT_PAGE, allArticles.getInt("currentPage"));
            editor.apply();

            // If there are results in the itemsArray
            if (articlesArray.length() > 0) {

                // Extract out the first feature (which is an article)
                for (int i = 0; i < articlesArray.length(); i++) {
                    JSONObject articleItem = articlesArray.getJSONObject(i);
                    JSONObject articleFields = articleItem.getJSONObject("fields");
                    JSONObject articleBlocks = articleItem.getJSONObject("blocks");
                    JSONArray tagsArray = articleItem.getJSONArray("tags");
                    JSONObject articleTags = tagsArray.optJSONObject(0); //For certain articles there is nothing in the tags array, so this is optJSONObject.
                    JSONArray bodyArray = articleBlocks.getJSONArray("body");
                    JSONObject articleBody = bodyArray.optJSONObject(0); //For certain articles there is nothing in body array, so this is optJSONObject.
                    JSONObject articleMain = articleBlocks.optJSONObject("main");//For certain articles there is no main key in the block, so this is optJSONObject.

                    // Extract out the headline, author, details stub, published date, and urls.
                    String headline = articleFields.optString("headline");

                    //Sometimes there is nothing in the "byline" field, so this is optString and only getting String if not blank.
                    //If "byline" field is blank checking other source for author name. Other source is @"tags".  Checking if tags is present and
                    //then if names are present.
                    String firstName;
                    String lastName;
                    String author;
                    if(!articleFields.optString("byline").equals("")){
                        author = articleFields.getString("byline");
                    }else{
                        if (articleTags != null) {
                            if(!articleTags.optString("firstName").equals("")) {
                                firstName = articleTags.getString("firstName");
                            }else{
                                firstName = " *** ";
                            }
                            if(!articleTags.getString("lastName").equals("")) {
                                lastName = articleTags.getString("lastName");
                            }else{
                                lastName = " *** ";
                            }
                            author = capitalizeNames(firstName) + " " + capitalizeNames(lastName);
                        } else {
                            author = "unknown"; //Add unknown if no "byline" or names strings are found.
                        }
                    }

                    //For certain articles articleMain will have a null value, if this first source is not present checking for alternate source @body.
                    //If no value is found datePublished is set to empty String.
                    //The empty String is dealt with later in the adapter.
                    String datePublished;
                    if (articleMain != null){
                        datePublished = formatDateAndTime(articleMain.getString("createdDate"));
                    }else{
                        if (articleBody != null){
                            datePublished = formatDateAndTime(articleBody.getString("createdDate"));
                        }else {
                            datePublished = "";
                        }
                    }

                    //Believe it or not....For certain articles the article body will have a null value. If null, detailsStub is set to empty String.
                    //The empty String is dealt with later in the adapter.
                    String detailsStub;
                    if (articleBody != null){
                        detailsStub = articleBody.getString("bodyTextSummary");
                    }else{
                        detailsStub = "";
                    }

                    String webURL = articleItem.getString("webUrl");
                    String thumbnailURL = articleFields.optString("thumbnail");

                    //Creating ArticleObject and adding to articles array.
                    ArticleObject acticleObject = new ArticleObject(headline, author, datePublished, detailsStub, webURL, thumbnailURL);
                    articles.add(acticleObject);
                }
                // when for loop is finished adding ArticleObjects returning completed list of articles.
                return new ArrayList<>(articles);
            }
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "Problem parsing the articles JSON results", e);
        }
        return null;
    }
    //method used to Capitalize first name and last name if strings when obtained from "tags" key
    private static String capitalizeNames(String name){
        return name.substring(0,1).toUpperCase() + name.substring(1);
    }

    //Formatting date for better viewing
    private static String formatDateAndTime(String dateAndTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        Date d = null;
        String newDate;
        try {
            d = sdf.parse(dateAndTime);
            sdf.applyPattern("EEE. MMM dd, yyyy hh:mm:ss a");
            newDate = sdf.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return newDate;
    }
}
