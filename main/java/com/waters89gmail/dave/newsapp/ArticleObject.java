package com.waters89gmail.dave.newsapp;

/**
 * Created by WatersD on 7/31/2016.
 */
public class ArticleObject {

    private String mHeadline;
    private String mAuthor;
    private String mDatePublished;
    private String mDetailStub;
    private String mWebURL;
    private String mThumbnailURL;


    public ArticleObject(String headline, String author, String publishedDate, String details, String webUrl, String thumbnailUrl){

        this.mHeadline = headline;
        this.mAuthor = author;
        this.mDatePublished = publishedDate;
        this.mDetailStub = details;
        this.mWebURL = webUrl;
        this.mThumbnailURL = thumbnailUrl;
    }

    public String getmHeadline() {
        return mHeadline;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public String getmDatePublished() {
        return mDatePublished;
    }

    public String getmDetailStub() {
        return mDetailStub;
    }

    public String getmWebURL() {
        return mWebURL;
    }

    public String getmThumbnailURL() {
        return mThumbnailURL;
    }
}
