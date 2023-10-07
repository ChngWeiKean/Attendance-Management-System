package com.example.attendancemanagementsystem;

import java.util.Map;

public class CourseSession {
    private String courseCode;
    private String date;
    private String startTime;
    private String endTime;
    private Integer sessionID;
    // A dictionary of student IDs and their attendance status
    private Map<String, String> studentAttendanceStatus;

    public CourseSession() {}

    public CourseSession(String courseCode, String date, String startTime, String endTime, Integer sessionID, Map<String, String> studentAttendanceStatus) {
        this.courseCode = courseCode;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sessionID = sessionID;
        this.studentAttendanceStatus = studentAttendanceStatus;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getDate() {
        return date;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public Integer getSessionID() {
        return sessionID;
    }

    public Map<String, String> getStudentAttendanceStatus() {
        return studentAttendanceStatus;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setSessionID(Integer sessionID) {
        this.sessionID = sessionID;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStudentAttendanceStatus(Map<String, String> studentAttendanceStatus) {
        this.studentAttendanceStatus = studentAttendanceStatus;
    }

}
