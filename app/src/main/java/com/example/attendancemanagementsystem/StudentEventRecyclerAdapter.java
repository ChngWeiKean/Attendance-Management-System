package com.example.attendancemanagementsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentEventRecyclerAdapter extends RecyclerView.Adapter<StudentEventRecyclerAdapter.ViewHolder> {
    private List<Course> courseList;
    private OnItemClickListener listener;

    public StudentEventRecyclerAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseCodeTextView;
        TextView courseNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // courseCodeTextView = itemView.findViewById(R.id.dashboard_card_course_id);
            // courseNameTextView = itemView.findViewById(R.id.dashboard_card_course_name);

            itemView.setOnClickListener(new View.OnClickListener() {
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_event_card, parent, false);
        return new ViewHolder(itemView);
    }

    // Replace the contents of a view.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.courseCodeTextView.setText(course.getCourseCode());
        holder.courseNameTextView.setText(course.getCourseName());
    }

    // Return the size of your dataset.
    @Override
    public int getItemCount() {
        return courseList.size();
    }
}