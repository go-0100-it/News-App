package com.waters89gmail.dave.newsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, LoaderManager.LoaderCallbacks<JSONObject> {

    //Loader type
    private static final String RESTART = "restart";
    private static final String INIT = "init";

    //String fragments used to assemble the guardian API URL query.
    private static final String ANDROID_URL_FRAG = "android";
    private static final String SAMSUNG_URL_FRAG = "samsung";
    private static final String PROG_URL_FRAG = "programming%20AND%20(comptuer%20OR%20java%20OR%20html%20OR%20adruino%20OR%20code%20OR%20apps%20OR%20AI)";
    private static final String AUTO_URL_FRAG = "autonomous%20OR%20self-driving%20OR%20selfdriving%20OR%20driverless%20OR%20driver-less%20AND%20(vehicles%20OR%20cars)";
    private static final String ROBO_URL_FRAG = "(robot%20OR%20robotics%20OR%20robo)";

    private  String searchApiUrl;
    private String asyncProgressMessage;
    private String currentSearch;
    private String toolBarTitle;

    private int currentPage; //defined public, used in ArticleLoaderUtil.
    private int totalPages; //defined public, used in ArticleLoaderUtil.

    private TextView prevBtn;
    private TextView nextBtn;
    private TextView pageStaus;
    private TextView emptyViewText;
    private ImageView prevArrow;
    private ImageView nextArrow;
    private ListView articleListView;
    private ScrollView emptyView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private ProgressDialog asyncDialog;

    private ArticleAdapter articleAdapter;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Checking to see if loader exists, if loader exists we can reuse data from existing loader.
        //This will only be true if the device has be rotated.
        if (getSupportLoaderManager().getLoader(0) != null) {
            //Resetting search variables to last saved values.
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            currentSearch = prefs.getString(Constants.CURRENT_SEARCH, "");
            currentPage = prefs.getInt(Constants.CURRENT_PAGE, 1);
            toolBarTitle = prefs.getString(Constants.TOOL_BAR_TITLE, getString(R.string.app_name));

            // if/else loader exists common code seperated out @setNewView
            setNewView();

            //Re-showing progress dialog if loader has not finished.
            if (getSupportLoaderManager().getLoader(0).isStarted()) {
                asyncDialog.setMessage(getString(R.string.continuing_to_load_articles));
                asyncDialog.show();
            }

            //calling startAsyncLoader to initialize loader, this will reuse previously loaded data.
            startAsyncLoader(INIT);
        } else {
            //Clearing search variables for new activity.
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            editor = prefs.edit();
            editor.remove(Constants.CURRENT_SEARCH);
            editor.remove(Constants.CURRENT_PAGE);
            editor.remove(Constants.TOOL_BAR_TITLE);
            editor.remove(Constants.TOTAL_PAGES);
            editor.apply();

            toolBarTitle = getString(R.string.app_name);

            // if/else loader exists common code seperated out @setNewView
            setNewView();

            //opens the menu drawer for selection if there is no list to view.
            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START);
                emptyViewText.setText(getString(R.string.empty_view_message_one));
            }
        }
    }

    private void setNewView() {

        asyncDialog = new ProgressDialog(MainActivity.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Finding views and setting varibles.
        prevBtn = (TextView) findViewById(R.id.previous_btn);
        nextBtn = (TextView) findViewById(R.id.next_btn);
        prevArrow = (ImageView) findViewById(R.id.prev_arrow);
        nextArrow = (ImageView) findViewById(R.id.next_arrow);
        pageStaus = (TextView) findViewById(R.id.page_status);

        //Setting OnClickListeners for custom page navigation bar.
        prevBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        prevArrow.setOnClickListener(this);
        nextArrow.setOnClickListener(this);

        //Functionality in progress
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*TODO Add fuctionality.
                    Option #1
                    Prompt user with "Go to page?" dialog.
                    Dialog will contain an EditText to input page number
                    Will also contain 2 buttons, one Labelled "GO" and the other
                    labelled "STAY"
                    Touching "GO" will restart loader with new URL with desired page number appended.
                    Touching "STAY" will dismiss dialog and do nothing.

                    Option #2
                    Prompt user with dialog with a page numbers spinner.
                    Selecting a number will restart loader with new URL with desired page number appended.

                    Also, need to remove Snackbar below.
                 */
                Snackbar.make(view, getString(R.string.coming_soon), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Setting OnClickListener as source link to www.theguardian.com in the drawer header view
        View headerLayout = navigationView.getHeaderView(0);
        headerLayout.findViewById(R.id.source_text_view).setOnClickListener(this);

        emptyView = (ScrollView) findViewById(R.id.empty_view);
        emptyViewText = (TextView) findViewById(R.id.empty_view_text);
        articleListView = (ListView) findViewById(R.id.list);
        articleAdapter = new ArticleAdapter(this, new ArrayList<ArticleObject>());
        articleListView.setAdapter(articleAdapter);
        articleListView.setEmptyView(emptyView);
    }

    @Override
    public void onBackPressed() {

        //Closing drawer if open on back press
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handling action bar item clicks here.
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*
        * Creating and starting new search type.
        * Setting currentSearch variable to the URL string fragment that is corresponding to search type selected,
        * this fragment is apart of the complete URL string used in the guardian API query httpRequest,
        * the complete URL is assembled @this.startAsyncLoader and is passed in @ArticleLoader.makeHttpRequest.
        *
        * Setting the asyncProgressMessage so the asyncDialog will display what Article type we are fetching.
        * the asyncDialog message is set and shown @(@Override onCreateLoader)
        * Also, setting the toolbar title to indicate Articles Searched.
        */
        if (id != R.id.nav_send && id != R.id.nav_share) {
            switch (id) {
                case R.id.search_android:
                    toolBarTitle = getString(R.string.drawer_menu_item_one);
                    currentSearch = ANDROID_URL_FRAG;
                    asyncProgressMessage = getString(R.string.async_task_progress_message_android);
                    break;
                case R.id.search_samsung:
                    toolBarTitle = getString(R.string.drawer_menu_item_two);
                    currentSearch = SAMSUNG_URL_FRAG;
                    asyncProgressMessage = getString(R.string.async_task_progress_message_samsung);
                    break;
                case R.id.search_programming:
                    toolBarTitle = getString(R.string.drawer_menu_item_three);
                    currentSearch = PROG_URL_FRAG;
                    asyncProgressMessage = getString(R.string.async_task_progress_message_programming);
                    break;
                case R.id.search_autonomous_vehicles:
                    toolBarTitle = getString(R.string.drawer_menu_item_four);
                    currentSearch = AUTO_URL_FRAG;
                    asyncProgressMessage = getString(R.string.async_task_progress_message_autonomous_vehicles);
                    break;
                case R.id.search_robotics:
                    toolBarTitle = getString(R.string.drawer_menu_item_five);
                    currentSearch = ROBO_URL_FRAG;
                    asyncProgressMessage = getString(R.string.async_task_progress_message_robotics);
                    break;
            }

            // In order to return the first page of the new guardian API query results, we're setting the pageNum variable to 1.
            // This variable is used to assemble the URL String needed for the guardian API query.
            currentPage = 1;

            // calling startAsyncLoader and passing in "restart" to restart the loader (to use a new URL).
            startAsyncLoader(RESTART);

        } else {
            View parentLayout = findViewById(R.id.drawer_layout);
            switch (id) {
                case R.id.nav_share:
                        /*TODO Add fuctionality.

                        */
                    Snackbar.make(parentLayout, getString(R.string.coming_soon), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    break;
                case R.id.nav_send:
                        /*TODO Add fuctionality.

                        */
                    Snackbar.make(parentLayout, getString(R.string.coming_soon), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    break;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        /*
        * Starting a new AsyncLoader to load new page of articles.
        *
        * If prev arrow ImageView or the "prev" TextView is pressed pageNum will be indexed backwards,
        * the URL for API query will be updated so the query will return the previous page of articles.
        *
        * If next arrow ImageView or the "next" TextView is pressed pageNum will be indexed forward,
        * the URL for API query will be updated so the query will return the next page of articles.
        */
        if (v.getId() == R.id.source_text_view) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.nav_header_source_URL))));
        } else {
            if (v.getId() == R.id.previous_btn || v.getId() == R.id.prev_arrow) {
                asyncProgressMessage = getString(R.string.loading_previous_page);
                currentPage = currentPage - 1;
            } else {
                asyncProgressMessage = getString(R.string.loading_next_page);
                currentPage = currentPage + 1;
            }
            startAsyncLoader(RESTART);
        }
    }

    private void startAsyncLoader(String startType) {

        //Saving current search criteria and page number to reuse if activity is destroyed.
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        editor.putString(Constants.CURRENT_SEARCH, currentSearch);
        editor.putString(Constants.TOOL_BAR_TITLE, toolBarTitle);
        editor.putInt(Constants.CURRENT_PAGE, currentPage);
        editor.apply();

        switch (startType) {
            case RESTART:
                // Assembling the URL string used for the guardian API query and setting it to the search_URL variable.
                // This variable is used @(@Override onCreateLoader)
                searchApiUrl = getString(R.string.search_api_url_start) + currentSearch + getString(R.string.search_api_url_end) + currentPage;
                // restarting the loader with id 0 and forcing load.
                getSupportLoaderManager().restartLoader(0, null, MainActivity.this).forceLoad();
                break;

            case INIT:
                // initializing the loader with id 0 and forcing load.
                getSupportLoaderManager().initLoader(0, null, MainActivity.this);
                break;
        }
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {

        //setting message of the dialog and showing
        asyncDialog.setMessage(asyncProgressMessage);
        asyncDialog.show();
        return new ArticleLoader(MainActivity.this, searchApiUrl);
    }

    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject jsonResponse) {

        articleAdapter.clear();
        //If null value returned from HTTP Request, LOGGING the error and updating the UI.
        if (jsonResponse == null) {
            updateUi(new ArrayList<ArticleObject>());
            Log.e(Constants.LOG_TAG, "jsonResponse is empty or null");

        } else {
            //If not a null value is returned from HTTP Request, adding all articleList objects to adapter and updating the UI.
            ArrayList articlesList = ArticleLoaderUtil.extractFeatureFromJson(jsonResponse, this);
            articleAdapter.addAll(articlesList);
            updateUi(articlesList);
        }
        asyncDialog.dismiss();
    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
        articleAdapter.clear();
    }

    /*
     * Setting an OnItemClickListener to listview if it contains items, using for loading articles web page in browser.
     * Update the UI to display next or previous buttons if applicable (determined by page numbers returned from search results @Link ArticleLoaderUtil.extractFeatureFromJson lines 40-41)
     */
    private void updateUi(final ArrayList<ArticleObject> articles) {

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentPage = prefs.getInt(Constants.CURRENT_PAGE, 1);
        totalPages = prefs.getInt(Constants.TOTAL_PAGES, 1);

        /*
        If listview is not null or not empty, setting OnItemClickListener for items in listview, that if clicked will load article webUrl in Web Browser.
        Also, setting visibility for buttons if more search results are available to view than are currently contained in listview,
        setting toolbar title to the articles type searched, setting pageStatus to visible and text to display current page number of total pages.
        */
        if (articleListView != null && !articles.isEmpty()) {

            toolbar.setTitle(toolBarTitle);
            pageStaus.setVisibility(View.VISIBLE);
            String statusText = getString(R.string.page) + " " + currentPage + " " + getString(R.string.of) + " " + totalPages;
            pageStaus.setText(statusText);

            articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(articles.get(position).getmWebURL())));
                }
            });

            if (currentPage == totalPages) {
                nextBtn.setVisibility(View.GONE);
                nextArrow.setVisibility(View.GONE);
            } else {
                nextBtn.setVisibility(View.VISIBLE);
                nextArrow.setVisibility(View.VISIBLE);
            }

            if (currentPage == 1) {
                prevBtn.setVisibility(View.GONE);
                prevArrow.setVisibility(View.GONE);
            } else {
                prevBtn.setVisibility(View.VISIBLE);
                prevArrow.setVisibility(View.VISIBLE);
            }
             /*
            If listview is null or empty, setting visibility of text buttons, image buttons, and pageStatus text to gone and,
            changing the emptyView text to inform user of no articles loaded to view.
            */
        } else {
            emptyViewText.setText(getString(R.string.empty_view_message_two));
            pageStaus.setVisibility(View.GONE);
            prevBtn.setVisibility(View.GONE);
            prevArrow.setVisibility(View.GONE);
            nextBtn.setVisibility(View.GONE);
            nextArrow.setVisibility(View.GONE);
        }
    }
}

