package com.example.attendancemanagementsystem;

public class User {
    private String username;
    private String email;
    private String password;
    private String id;
    private String userType;

    public User() {}

    public User(String username, String email, String password, String userType, String id) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.id = id;
        this.userType = userType;
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
}
