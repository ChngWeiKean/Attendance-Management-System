package com.example.attendancemanagementsystem;

import java.util.List;

public class Course {

    private String courseCode;
    private String courseName;
    private List<Integer> courseSchedules;
    private String lecturer;
    private List<String> students;
    private Boolean hasOngoingSession;
    private List<String> courseSessions;
    private String courseImageURL;

    public String getCourseCode() {
        return courseCode;
    }

    public String getLecturer() {
        return lecturer;
    }

    public List<Integer> getCourseSchedules() {
        return courseSchedules;
    }

    public String getCourseName() {
        return courseName;
    }

    public List<String> getStudents() {
        return students;
    }

    public Boolean getHasOngoingSession() {
        return hasOngoingSession;
    }

    public List<String> getCourseSessions() {
        return courseSessions;
    }

    public String getCourseImageURL() { return courseImageURL; }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCourseSchedules(List<Integer> courseSchedules) {
        this.courseSchedules = courseSchedules;
    }

    public void addCourseSchedule(Integer courseSchedule) {
        courseSchedules.add(courseSchedule);
    }

    public void removeCourseSchedule(Integer courseSchedule) {
        courseSchedules.remove(courseSchedule);
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public void setStudents(List<String> students) {
        this.students = students;
    }

    public void setHasOngoingSession(Boolean hasOngoingSession) {
        this.hasOngoingSession = hasOngoingSession;
    }

    public void setCourseSessions(List<String> courseSessions) {
        this.courseSessions = courseSessions;
    }

    public void setCourseImageURL(String courseImageURL) {
        this.courseImageURL = courseImageURL;
    }

    public void addCourseSession(String courseSessionID) {
        courseSessions.add(courseSessionID);
    }

    public void addStudent(String student) {
        students.add(student);
    }

    public void removeStudent(String student) {
        students.remove(student);
    }
}
