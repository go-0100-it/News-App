package com.waters89gmail.dave.newsapp;

/**
 * Created by WatersD on 8/6/2016.
 */
public class Constants {
    public Constants() {}

    //Log tag
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    //Shared Prefs Key names
    public static final String CURRENT_PAGE = "currentPage"; //defined public, used in ArticleLoaderUtil.
    public static final String TOTAL_PAGES = "totalPages"; //defined public, used in ArticleLoaderUtil.
    public static final String CURRENT_SEARCH = "currentSreach";
    public static final String TOOL_BAR_TITLE = "toolBarTilte";

}
