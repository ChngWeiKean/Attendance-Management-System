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

public class StudentPreviousSessionDetailsRecyclerAdapter extends RecyclerView.Adapter<StudentPreviousSessionDetailsRecyclerAdapter.ViewHolder> {
    private List<List<String>> sessionList;
    private Context context;

    public StudentPreviousSessionDetailsRecyclerAdapter(Context context, List<List<String>> sessionList) {
        this.context = context;
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_new_student_previous_session_row, parent, false);
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

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.new_session_row_date);
            startTimeTextView = itemView.findViewById(R.id.new_session_row_start_time);
            endTimeTextView = itemView.findViewById(R.id.new_session_row_end_time);
            attendanceStatusView = itemView.findViewById(R.id.new_session_row_status);
        }
    }
}