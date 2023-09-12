package com.example.attendancemanagementsystem;

public class CourseSchedule {
    private String courseCode;
    private String day;
    private String endTime;
    private String startTime;

    public String getCourseCode() {
        return courseCode;
    }

    public String getDay() {
        return day;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setDay(String day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "CourseSchedule{" +
                "courseCode='" + courseCode + '\'' +
                ", day='" + day + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
