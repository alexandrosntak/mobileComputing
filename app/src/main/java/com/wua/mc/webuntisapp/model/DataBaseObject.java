package com.wua.mc.webuntisapp.model;

public class DataBaseObject {

    private long course_id;
    private String course_name;
    private String course_lecturer;
    private int course_color;
    private int course_untis_id;

    private long event_id;
    private String event_room;
    private long event_timestamp_start;
    private long event_timestamp_end;
    private String event_name;
    private int event_color;
    private String event_type;

    private long authenticated;

    public DataBaseObject(){}

    public DataBaseObject(String course_name, String course_lecturer, int course_color, int course_untis_id, long course_id){
        this.setCourse_id(course_id);
        this.setCourse_name(course_name);
        this.setCourse_lecturer(course_lecturer);
        this.setCourse_color(course_color);
        this.setCourse_untis_id(course_untis_id);

    }

    public DataBaseObject(String event_room, long event_timestamp_start, long event_timestamp_end, String event_name, int event_color, String event_type, long event_id, long courseId){

        this.setEvent_id(event_id);
        this.setEvent_room(event_room);
        this.setEvent_timestamp_start(event_timestamp_start);
        this.setEvent_timestamp_end(event_timestamp_end);
        this.setEvent_name(event_name);
        this.setEvent_color(event_color);
        this.setEvent_type(event_type);
        this.setCourse_id(courseId);
    }

    public DataBaseObject(long authenticated){

        this.setAuthenticated(authenticated);

    }

    public void setCourse_id(long course_id) {
        this.course_id = course_id;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public void setCourse_lecturer(String course_lecturer) {
        this.course_lecturer = course_lecturer;
    }

    public void setCourse_color(int course_color) {
        this.course_color = course_color;
    }

    public void setCourse_untis_id(int course_untis_id) {
        this.course_untis_id = course_untis_id;
    }

    public void setEvent_id(long event_id) {
        this.event_id = event_id;
    }

    public void setEvent_room(String event_room) {
        this.event_room = event_room;
    }

    public void setEvent_timestamp_start(long event_timestamp_start) {
        this.event_timestamp_start = event_timestamp_start;
    }

    public void setEvent_timestamp_end(long event_timestamp_end) {
        this.event_timestamp_end = event_timestamp_end;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public void setEvent_color(int event_color) {
        this.event_color = event_color;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public void setAuthenticated(long authenticated) {
        this.authenticated = authenticated;
    }

    public long getCourse_id() {

        return course_id;
    }

    public String getCourse_name() {
        return course_name;
    }

    public String getCourse_lecturer() {
        return course_lecturer;
    }

    public int getCourse_color() {
        return course_color;
    }

    public int getCourse_untis_id() {
        return course_untis_id;
    }

    public long getEvent_id() {
        return event_id;
    }

    public String getEvent_room() {
        return event_room;
    }

    public long getEvent_timestamp_start() {
        return event_timestamp_start;
    }

    public long getEvent_timestamp_end() {
        return event_timestamp_end;
    }

    public String getEvent_name() {
        return event_name;
    }

    public int getEvent_color() {
        return event_color;
    }

    public String getEvent_type() {
        return event_type;
    }

    public long getAuthenticated() {
        return authenticated;
    }

    @Override
    public String toString() {
        String output = "";
        if(course_name!=null){
            output += "COURSE_ID: " + course_id + "\n COURSE_NAME: " + course_name + "\n COURSE_LECTURER: " + course_lecturer + "\n COURSE_COLOR: " + course_color + "\n UNTIS_ID: " + course_untis_id;
        }
        else if (event_name!=null){
            output += "EVENT_ID: " + event_id + "\n EVENT_ROOM: " + event_room + "\n EVENT_START: " + event_timestamp_start + "\n EVENT_END: " + event_timestamp_end +
                    "\n EVENT_NAME: " + event_name + "\n EVENT_COLOR: " + event_color + "\n EVENT_TYPE " + event_type;
        }
        else {
            output += authenticated;
        }
        return output;
    }


}
