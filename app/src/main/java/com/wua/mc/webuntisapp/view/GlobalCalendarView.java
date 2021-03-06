package com.wua.mc.webuntisapp.view;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.wua.mc.webuntisapp.R;
import com.wua.mc.webuntisapp.presenter.CalendarPresenter;
import com.wua.mc.webuntisapp.presenter.Event;
import com.wua.mc.webuntisapp.presenter.FieldOfStudy;
import com.wua.mc.webuntisapp.presenter.Filter;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class GlobalCalendarView extends CalendarView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        Bundle extras = getIntent().getExtras();

        String chosenFieldOfStudy;
        String id;
        String filterID;
        String fieldOfStudyName;
        FieldOfStudy fieldOfStudy;


        if (extras != null) {
            chosenFieldOfStudy = extras.getString("SelectedFieldOfStudy");
            id = extras.getString("id");
            filterID = extras.getString("filterID");
            fieldOfStudyName = extras.getString("name");
            fieldOfStudy = new FieldOfStudy(id, fieldOfStudyName, chosenFieldOfStudy, true, filterID);
        } else {
            fieldOfStudy = calendarDataManagement.getSelectedFieldOfStudy();
        }
        calendarDataManagement.setSelectedFieldOfStudy(fieldOfStudy);
        buildWeeklyCalendar();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void showEventsOnCalendar(ArrayList<Event> events) {
        showEventsOnDailyPlan(events);
    }

    @Override
    public void showToast(String text) {
        Context context = getApplicationContext();
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionMenu(Menu menu) {
        return false;
    }

    @Override
    protected void getWeeklyCalendar(GregorianCalendar calendar, FieldOfStudy fieldOfStudy) {
        getCalendarWebUntis().getWeeklyCalendarGlobal(this, calendar, fieldOfStudy);
    }

    @Override
    protected String getEventInformation(String eventID) {
        return getCalendarWebUntis().getEventInformationGlobal(eventID);
    }

    @Override
    protected void setCalendarContentView() {
        setContentView(R.layout.activity_global_calendar);
    }
}