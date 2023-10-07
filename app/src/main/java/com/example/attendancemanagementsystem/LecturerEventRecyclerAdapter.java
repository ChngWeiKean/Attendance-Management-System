package com.example.attendancemanagementsystem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

public class LecturerEventRecyclerAdapter extends RecyclerView.Adapter<LecturerEventRecyclerAdapter.ViewHolder> {
    private List<Event> eventList;
    private OnItemClickListener listener;
    private String currentUserId;

    public LecturerEventRecyclerAdapter(List<Event> eventList, String currentUserId) {
        this.currentUserId = currentUserId;
        this.eventList = eventList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView eventImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            eventImage = itemView.findViewById(R.id.event_card_image);

            eventImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    // Create new views.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lecturer_event_card, parent, false);
        return new ViewHolder(itemView);
    }

    // Replace the contents of a view.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = eventList.get(position);

        String imageURL = event.getImages().get(0);

        // Load the image asynchronously
        new LoadImageTask(holder.eventImage).execute(imageURL);
    }

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        LoadImageTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                // Load the image from the URL
                String imageURL = params[0];
                InputStream input = new java.net.URL(imageURL).openStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    // Set the loaded bitmap to the ImageView
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }


    // Return the size of your dataset.
    @Override
    public int getItemCount() {
        return eventList.size();
    }
}