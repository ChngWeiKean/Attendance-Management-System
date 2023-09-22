package com.example.attendancemanagementsystem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

public class StudentEventRecyclerAdapter extends RecyclerView.Adapter<StudentEventRecyclerAdapter.ViewHolder> {
    private List<Event> eventList;
    private OnItemClickListener listener;
    private OnEditButtonClickListener editListener;
    private OnDeleteButtonClickListener deleteListener;
    private String currentUserId;

    public StudentEventRecyclerAdapter(List<Event> eventList, String currentUserId) {
        this.currentUserId = currentUserId;
        this.eventList = eventList;
    }

    public interface OnEditButtonClickListener {
        void onEditButtonClick(int position);
    }

    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClick(int position);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnEditButtonClickListener(OnEditButtonClickListener listener) {
        this.editListener = listener;
    }

    public void setOnDeleteButtonClickListener(OnDeleteButtonClickListener listener) {
        this.deleteListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView eventImage;
        ImageButton editButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            eventImage = itemView.findViewById(R.id.event_card_image);
            editButton = itemView.findViewById(R.id.edit_event_button);
            deleteButton = itemView.findViewById(R.id.delete_event_button);

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

            editButton.setOnClickListener(view -> {
                if (editListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        editListener.onEditButtonClick(position);
                    }
                }
            });

            deleteButton.setOnClickListener(view -> {
                if (deleteListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        deleteListener.onDeleteButtonClick(position);
                    }
                }
            });
        }
    }

    // Create new views.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_event_card, parent, false);
        return new ViewHolder(itemView);
    }

    // Replace the contents of a view.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = eventList.get(position);

        String imageURL = event.getImages().get(0);

        // Load the image asynchronously
        new LoadImageTask(holder.eventImage).execute(imageURL);

        // Find the button container view in the card layout
        LinearLayout buttonsContainer = holder.itemView.findViewById(R.id.event_card_buttons_container);

        // Check if the event's userId matches the current user's ID
        if (event.getUserID().equals(currentUserId)) {
            // If they match, set the buttons container to visible
            buttonsContainer.setVisibility(View.VISIBLE);
        } else {
            // If they don't match, hide the buttons container
            buttonsContainer.setVisibility(View.GONE);
        }
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