package com.example.attendancemanagementsystem;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {
    private Context context;
    private List<Uri> imageUris; // Change the list type to Uri
    private List<ImageView> imageViews; // Store ImageViews for Picasso

    public ImagePagerAdapter(Context context, List<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
        this.imageViews = new ArrayList<>(); // Initialize ImageView list
    }

    @Override
    public int getCount() {
        return imageUris.size();
    }

    public void updateImages(List<Uri> newImageUris) {
        imageUris.clear();
        imageUris.addAll(newImageUris);
        notifyDataSetChanged();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.image_pager_item, container, false);

        ImageView imageView = itemView.findViewById(R.id.imageView);
        // Use Picasso to load the image into the ImageView
        Picasso.get().load(imageUris.get(position)).into(imageView);

        // Store the ImageView for future use
        imageViews.add(imageView);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public Uri getImage(int position) {
        return imageUris.get(position);
    }

    public void addImageView(ImageView imageView) {
        imageViews.add(imageView);
    }

    // clear adapter
    public void clear() {
        imageUris.clear();
        imageViews.clear();
        notifyDataSetChanged();
    }
}


