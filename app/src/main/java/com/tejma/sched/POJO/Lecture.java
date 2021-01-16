package com.tejma.sched.POJO;

public class Lecture {
    private String subject, code, faculty, type, time, day;
    private String meet_link, classroom_link;
    private boolean isNotified;

    public Lecture(String subject, String code, String faculty,
                   String type, String time, String day,
                   String meet_link, String classroom_link, boolean isNotified) {
        this.subject = subject;
        this.code = code;
        this.faculty = faculty;
        this.type = type;
        this.time = time;
        this.day = day;
        this.meet_link = meet_link;
        this.classroom_link = classroom_link;
        this.isNotified = isNotified;
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMeet_link() {
        return meet_link;
    }

    public void setMeet_link(String meet_link) {
        this.meet_link = meet_link;
    }

    public String getClassroom_link() {
        return classroom_link;
    }

    public void setClassroom_link(String classroom_link) {
        this.classroom_link = classroom_link;
    }
}

