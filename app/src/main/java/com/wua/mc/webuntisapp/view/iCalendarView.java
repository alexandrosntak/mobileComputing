package com.wua.mc.webuntisapp.view;
import com.wua.mc.webuntisapp.presenter.Event;


public interface iCalendarView {

	void showEventsOnCalendar(Event[] events);

	void showToast(String text);

}