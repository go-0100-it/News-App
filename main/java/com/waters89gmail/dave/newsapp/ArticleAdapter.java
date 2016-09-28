package com.waters89gmail.dave.newsapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Created by WatersD on 7/31/2016.
 */
public class ArticleAdapter extends ArrayAdapter<ArticleObject> {

    public ArticleAdapter(Context context, List<ArticleObject> articles) {
        super(context, 0, articles);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;

        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.article_list_view, parent, false);

            /*
            Using a view holder class so that it is only necessary to use find view by id when view is inflated.
             */
            viewHolder = new ViewHolder();

            /*
            Finding views and assigning to viewholders.
             */
            viewHolder.headline = (TextView) listItemView.findViewById(R.id.headline_textview);
            viewHolder.author = (TextView) listItemView.findViewById(R.id.author_textview);
            viewHolder.publishedDate = (TextView) listItemView.findViewById(R.id.published_date_textview);
            viewHolder.publishedDateAltView = (TextView) listItemView.findViewById(R.id.published_date_textview2);
            viewHolder.detailsStub = (TextView) listItemView.findViewById(R.id.detail_stub_textview);
            viewHolder.thumbnail = (ImageView) listItemView.findViewById(R.id.thumbnail_imageview);
            viewHolder.progressBar = (ProgressBar) listItemView.findViewById(R.id.progress);
            viewHolder.imageViewLayout = (RelativeLayout) listItemView.findViewById(R.id.image_view_layout);

            /*
            setting a tag to the view for future reference instead of using find view by id each time.
             */
            listItemView.setTag(viewHolder);

        } else {

            /*
            here the tag is used to reference the view if it has already been inflated and a tag set to the holder.
             */
            viewHolder = (ViewHolder) listItemView.getTag();
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.thumbnail.setVisibility(View.GONE);
        }

        /*
        Getting current object position in Array.
         */
        ArticleObject currentAtricleObject = getItem(position);

        /*
        Checking to see if object exists before setting data to viewHolders.
         */
        if (currentAtricleObject != null) {

            viewHolder.headline.setText(currentAtricleObject.getmHeadline());


            if(currentAtricleObject.getmDatePublished().equals("")){
                viewHolder.publishedDate.setVisibility(View.GONE);
            }else {
                viewHolder.publishedDate.setVisibility(View.VISIBLE);
                viewHolder.publishedDate.setText(currentAtricleObject.getmDatePublished());
            }

            /*
            Adding prefix (Author: ) to author string before setting string to viewHolder.
             */
            String author = getContext().getString(R.string.by)+" "+currentAtricleObject.getmAuthor();
            viewHolder.author.setText(author);

            /*
            Checking if description exists for currentBookObject.  If none exists setting description viewHolder to default of
            * No description available. and reducing text size.
             */
            String detailStub;
            if(currentAtricleObject.getmDetailStub() == null || currentAtricleObject.getmDetailStub().equals("")){
                viewHolder.detailsStub.setTextSize(8);
                viewHolder.detailsStub.setMaxLines(1);
                viewHolder.detailsStub.setTextColor(getContext().getResources().getColor(R.color.pageNavColor));
                detailStub = "\n  "+getContext().getResources().getString(R.string.no_detail);
            }else {
                viewHolder.detailsStub.setMaxLines(4);
                viewHolder.detailsStub.setTextSize(12);
                viewHolder.detailsStub.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
                detailStub = currentAtricleObject.getmDetailStub();
            }
            viewHolder.detailsStub.setText(detailStub);

            /*
            Using Glide to smooth out loading and setting of image to listview.
            added (com.github.bumptech.glide:glide:3.7.0') to build.gradle file.
             */



            if (currentAtricleObject.getmThumbnailURL() != null && !currentAtricleObject.getmThumbnailURL().equals("")){

                viewHolder.imageViewLayout.setVisibility(View.VISIBLE);
                viewHolder.publishedDateAltView.setVisibility(View.GONE);
                viewHolder.detailsStub.setMaxLines(7);
                viewHolder.detailsStub.setMinLines(7);
                DownloadAsyncTask downloadAsyncTask = new DownloadAsyncTask();
                downloadAsyncTask.setViewHolder(viewHolder);
                downloadAsyncTask.execute(currentAtricleObject.getmThumbnailURL());

            }else{
                viewHolder.publishedDateAltView.setText(currentAtricleObject.getmDatePublished());
                viewHolder.detailsStub.setMaxLines(4);
                viewHolder.detailsStub.setMinLines(4);
                viewHolder.imageViewLayout.setVisibility(View.GONE);
                viewHolder.publishedDateAltView.setVisibility(View.VISIBLE);
                /*
            Setting book image to a default stub image before checking for and setting thumbnail image.  Default is for case
            where no image is available.
             */
                viewHolder.thumbnail.setImageResource(R.drawable.placeholder_image);
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.thumbnail.setVisibility(View.VISIBLE);
            }
        }
        return listItemView;
    }

    static class ViewHolder {

        TextView headline;
        TextView author;
        TextView publishedDate;
        TextView publishedDateAltView;
        TextView detailsStub;
        ImageView thumbnail;
        ProgressBar progressBar;
        RelativeLayout imageViewLayout;
    }

    private class DownloadAsyncTask extends AsyncTask<String, Void, Bitmap>{

        ViewHolder viewHolder;

        private void setViewHolder(ViewHolder view){
            this.viewHolder = view;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... urls) {

            //  Auto-generated method stub
           Bitmap thumbnail;
            try {
                URL imageURL = new URL(urls[0]);
                thumbnail = BitmapFactory.decodeStream(imageURL.openStream());
            } catch (IOException e) {
                // TODO: handle exception
                Log.e(Constants.LOG_TAG, "Downloading Image Failed");
                thumbnail = null;
            }
            return thumbnail;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap == null) {
                viewHolder.thumbnail.setImageResource(R.drawable.placeholder_image);
            } else {
                viewHolder.thumbnail.setImageBitmap(bitmap);
            }
            viewHolder.thumbnail.setVisibility(View.VISIBLE);
            viewHolder.progressBar.setVisibility(View.GONE);
        }
    }

}
