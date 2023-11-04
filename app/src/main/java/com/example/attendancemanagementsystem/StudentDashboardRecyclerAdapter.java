package com.example.attendancemanagementsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class StudentDashboardRecyclerAdapter extends RecyclerView.Adapter<StudentDashboardRecyclerAdapter.ViewHolder> {
    private List<Course> courseList;
    private OnItemClickListener listener;

    public StudentDashboardRecyclerAdapter(List<Course> courseList) {
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
        ImageView courseImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCodeTextView = itemView.findViewById(R.id.student_dashboard_card_course_id);
            courseNameTextView = itemView.findViewById(R.id.student_dashboard_card_course_name);
            courseImageView = itemView.findViewById(R.id.student_dashboard_card_image);

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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_dashboard_course_cards, parent, false);
        return new ViewHolder(itemView);
    }

    // Replace the contents of a view.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.courseCodeTextView.setText(course.getCourseCode());
        holder.courseNameTextView.setText(course.getCourseName());

        if (course.getCourseImageURL() != null && !course.getCourseImageURL().isEmpty()) {
            Picasso.get().load(course.getCourseImageURL()).into(holder.courseImageView);
        } else {
            holder.courseImageView.setImageResource(R.drawable.dashboard_card_image);
        }
    }

    // Return the size of your dataset.
    @Override
    public int getItemCount() {
        return courseList.size();
    }
}
