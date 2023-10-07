package com.example.attendancemanagementsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PreviousSessionDetailsRecyclerAdapter extends RecyclerView.Adapter<PreviousSessionDetailsRecyclerAdapter.ViewHolder> {
    private List<List<String>> sessionList;
    private OnSessionDetailsButtonClickListener sessionDetailsButtonClickListener;
    private Context context;

    public PreviousSessionDetailsRecyclerAdapter(Context context, List<List<String>> sessionList, OnSessionDetailsButtonClickListener listener) {
        this.context = context;
        this.sessionList = sessionList;
        this.sessionDetailsButtonClickListener = listener;
    }

    public void setOnSessionDetailsButtonClickListener(OnSessionDetailsButtonClickListener listener) {
        this.sessionDetailsButtonClickListener = listener;
    }

    public interface OnSessionDetailsButtonClickListener {
        void onSessionDetailsButtonClicked(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_new_past_session_detail_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<String> sessionData = sessionList.get(position);

        if (sessionData != null && sessionData.size() >= 5) {
            holder.dateTextView.setText(sessionData.get(0));
            holder.startTimeTextView.setText(sessionData.get(1));
            holder.endTimeTextView.setText(sessionData.get(2));
            holder.attendanceStatusView.setText(sessionData.get(3));

            int colorResourceId = context.getResources().getIdentifier(sessionData.get(4), "color", context.getPackageName());
            int color = ContextCompat.getColor(context, colorResourceId);
            holder.attendanceStatusView.setBackgroundColor(color);

            // Set an OnClickListener for the button
            holder.sessionDetailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Call the callback method when the button is clicked
                    int clickedPosition = holder.getBindingAdapterPosition(); // Get the current position
                    if (sessionDetailsButtonClickListener != null && clickedPosition != RecyclerView.NO_POSITION) {
                        sessionDetailsButtonClickListener.onSessionDetailsButtonClicked(clickedPosition);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView startTimeTextView;
        public TextView endTimeTextView;
        public Button attendanceStatusView;
        public Button sessionDetailsButton;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.new_session_row_date);
            startTimeTextView = itemView.findViewById(R.id.new_session_row_start_time);
            endTimeTextView = itemView.findViewById(R.id.new_session_row_end_time);
            attendanceStatusView = itemView.findViewById(R.id.new_session_row_status);
            sessionDetailsButton = itemView.findViewById(R.id.new_session_row_details);
        }
    }
}

