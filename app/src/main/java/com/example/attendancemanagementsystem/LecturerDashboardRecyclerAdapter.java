package com.example.attendancemanagementsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LecturerDashboardRecyclerAdapter extends RecyclerView.Adapter<LecturerDashboardRecyclerAdapter.ViewHolder> {
    private List<Course> courseList;
    private OnItemClickListener listener;

    public LecturerDashboardRecyclerAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onEditClick(int position);

        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseCodeTextView;
        TextView courseNameTextView;
        ImageButton editButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCodeTextView = itemView.findViewById(R.id.lecturer_dashboard_card_course_id);
            courseNameTextView = itemView.findViewById(R.id.lecturer_dashboard_card_course_name);

            editButton = itemView.findViewById(R.id.lecturer_dashboard_card_edit_button);
            deleteButton = itemView.findViewById(R.id.lecturer_dashboard_card_delete_button);

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

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onEditClick(position);
                        }
                    }
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }

    // Create new views.
    @Override
    public LecturerDashboardRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lecturer_dashboard_course, parent, false);
        return new LecturerDashboardRecyclerAdapter.ViewHolder(itemView);
    }

    // Replace the contents of a view.
    @Override
    public void onBindViewHolder(LecturerDashboardRecyclerAdapter.ViewHolder holder, int position) {
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
