package com.example.attendancemanagementsystem;

import java.util.List;

public class Event {

    private String title;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private String venue;
    private String eventID;
    private String userID;
    private String approvalStatus;
    private List<String> images;

    public Event() {
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getVenue() {
        return venue;
    }

    public String getEventID() {
        return eventID;
    }

    public String getUserID() {
        return userID;
    }

    public List<String> getImages() {
        return images;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public void addImage(String image) {
        images.add(image);
    }

    public void removeImage(String image) {
        images.remove(image);
    }

    public void clearImages() {
        images.clear();
    }

    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", venue='" + venue + '\'' +
                ", eventID='" + eventID + '\'' +
                ", userID='" + userID + '\'' +
                ", approvalStatus='" + approvalStatus + '\'' +
                ", images=" + images +
                '}';
    }

}
