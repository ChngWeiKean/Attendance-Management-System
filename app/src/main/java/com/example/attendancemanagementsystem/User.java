package com.example.attendancemanagementsystem;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String email;
    private String password;
    private String id;
    private String userType;
    private List<String> courses;

    public User() {}

    public User(String username, String email, String password, String userType, String id) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.id = id;
        this.userType = userType;
        this.courses= new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserType() {
        return userType;
    }

    public String getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getCourses() {
        return courses;
    }

    public void setCourses(List<String> courses) {
        this.courses = courses;
    }

    public void addCourseId(String classId) {
        courses.add(classId);
    }

    public void removeCourseId(String classId) {
        courses.remove(classId);
    }
}
