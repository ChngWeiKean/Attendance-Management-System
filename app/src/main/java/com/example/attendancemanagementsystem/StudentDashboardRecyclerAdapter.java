package com.example.attendancemanagementsystem;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StudentDashboardRecyclerAdapter extends RecyclerView.Adapter<StudentDashboardRecyclerAdapter.ViewHolder> {
    private List<Course> courseList;

    public StudentDashboardRecyclerAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseCodeTextView;
        TextView courseNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCodeTextView = itemView.findViewById(R.id.dashboard_card_course_id);
            courseNameTextView = itemView.findViewById(R.id.dashboard_card_course_name);
        }
    }

    // Create new views.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_dashboard_course_cards, parent, false);
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
