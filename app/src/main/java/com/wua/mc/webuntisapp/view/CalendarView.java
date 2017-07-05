package com.wua.mc.webuntisapp.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wua.mc.webuntisapp.R;
import com.wua.mc.webuntisapp.presenter.CalendarPresenter;
import com.wua.mc.webuntisapp.presenter.Event;
import com.wua.mc.webuntisapp.presenter.FieldOfStudy;
import com.wua.mc.webuntisapp.presenter.GregorianCalendarFactory;
import com.wua.mc.webuntisapp.presenter.UniversityEvent;
import com.wua.mc.webuntisapp.presenter.iCalendarPresenter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@TargetApi(3)
// modifications by ray : added to the implememted interfaces the OnclickListener
abstract class CalendarView extends Activity implements iCalendarView ,OnClickListener{

    protected iCalendarPresenter.iCalendarDataManagement calendarDataManagement;
    protected iCalendarPresenter.iCalendarWebUntis calendarWebUntis;
    private GregorianCalendar gregCal;

    private CalendarPresenter cp = new CalendarPresenter(this);

    private int eventFieldHeight;
    private int eventFieldWidth;
    private int eventFieldXStart;
    private int eventFieldYStart;
    private int eventFieldYEnd;
    private double heightPerQuarter;
    private final int numberOfQuartersIn24Hours = 24 * 4;

    private DayButton[] dayButtons = new DayButton[7];
    private DayButton currentDayButton;
    ArrayList<EventBoxView> eventBoxes;
    private final Context context = this;

    private DrawerLayout mDrawerLayout;
    private String[] menuItems;

    //-------------------------my variables
    private static final String tag = "calendarView";
    private TextView currentMonth;
    private Button selectedDayMonthYearButton;
    private ImageView prevMonth;
    private ImageView nextMonth;
    private GridView calendarView;
    private Button openMonthlyCalendar;
    private Button setting ;
    private GridCellAdapter adapter; // inner class that manages the days cells.
    private Calendar _calendar;
    @SuppressLint("NewApi")
    private int month, year;
    @SuppressWarnings("unused")
    @SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi" })
    private final DateFormat dateFormatter = new DateFormat();
    private static final String dateTemplate = "MMMM yyyy";
    private GridCellAdapter ga;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        gregCal = new GregorianCalendar(Locale.GERMANY);
        calendarDataManagement = new CalendarPresenter(this);
        calendarWebUntis = (iCalendarPresenter.iCalendarWebUntis) calendarDataManagement;



    }


    private void createDrawer(){
        menuItems = getResources().getStringArray(R.array.menu_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.simple_list_item_1, menuItems));

        /*if(this instanceof GlobalCalendarView){
            getViewByPosition(0, drawerList).setBackgroundColor(Color.RED);
        }
        else{
            getViewByPosition(1, drawerList).setBackgroundColor(Color.RED);
        }*/

        drawerList.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Intent personal = new Intent(CalendarView.this, PersonalCalendarView.class);
                        startActivity(personal);
                        break;
                    case 1:
                        Intent global = new Intent(CalendarView.this, GlobalCalendarView.class);
                        startActivity(global);
                        break;
                    case 2:
                        Intent logout = new Intent(CalendarView.this, MainActivity.class);
                        getCalendarDataManagement().logout();
                        startActivity(logout);
                        break;
                }
            }
        });
    }

    /*public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }*/

    @Override
    abstract public void showEventsOnCalendar(ArrayList<Event>  events);

    @Override
    abstract public void showToast(String text);

    abstract protected void getWeeklyCalendar(GregorianCalendar calendar, FieldOfStudy fieldOfStudy);

    abstract protected String getEventInformation(String eventID);

    abstract protected void setCalendarContentView();

    public void showMenuDrawer(View v){
        mDrawerLayout.openDrawer(Gravity.START);
    }



    protected void buildWeeklyCalendar(){
        setCalendarContentView();

        final ConstraintLayout scrollViewLayout = (ConstraintLayout) findViewById(R.id.day_plan_layout);

        scrollViewLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getDayButtonsAndListen();


                View firstLine = findViewById(R.id.hour00Line);
                View lastLine = findViewById(R.id.hour24Line);

                eventFieldYStart = (int) firstLine.getY();
                eventFieldYEnd = (int) lastLine.getY();
                eventFieldHeight = eventFieldYEnd - eventFieldYStart;

                eventFieldWidth = firstLine.getWidth();
                eventFieldXStart = (int) firstLine.getX();

                heightPerQuarter = (double)eventFieldHeight / (double)numberOfQuartersIn24Hours;


                scrollViewLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                updateCalendar();


                getWeeklyCalendar(gregCal, calendarDataManagement.getSelectedFieldOfStudy());

                createDrawer();

                scrollToCurrentTime();


            }
        });

        //---------------------------------------setting the onclicklistener on the monthly button
        openMonthlyCalendar = (Button) findViewById(R.id.open_month_calendar_button);
        openMonthlyCalendar.setOnClickListener(new OnClickListener(){

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                showMonthlyViewCalendar();

            }
        });
    }

    private void scrollToCurrentTime(){
        ScrollView scrollView = (ScrollView) findViewById(R.id.day_plan);
        int top = scrollView.getTop();
        int bottom = scrollView.getBottom();
        int range = bottom - top;
        double distancePerQuarter = (double)range / (double)(numberOfQuartersIn24Hours);
        int hour = gregCal.get(Calendar.HOUR_OF_DAY);
        int minute = gregCal.get(Calendar.MINUTE);
        double quarter = (double)minuteToQuarter(minute + hourToMinute(hour));
        double calculatedPosition = (distancePerQuarter * (quarter + 35));
        double positionToBeScrolled = calculatedPosition > bottom ? bottom : calculatedPosition;
        scrollView.setScrollY((int)positionToBeScrolled);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void showEventsOnDailyPlan(ArrayList<Event>  events){
        eventBoxes = new ArrayList<>();
        ConstraintLayout scrollViewLayout = (ConstraintLayout) findViewById(R.id.day_plan_layout);

//TODO remove the line 211 : it is for text purposes only:
        for(Event event: events){
            if(event.isEventOnThisDay(gregCal)){
                eventBoxes.add(createEventBoxView(event));
            }
        }

        EventBoxView[] eventBoxesArray = eventBoxes.toArray(new EventBoxView[eventBoxes.size()]);
        calculateHorizontalNeighbours(eventBoxesArray);

        adjustEventsWidths(eventBoxesArray);

        calculateHorizontalPositions(eventBoxesArray);

        for(EventBoxView eventBox: eventBoxes){
            scrollViewLayout.addView(eventBox.getButton());
        }
    }

    private void getDayButtonsAndListen(){
        dayButtons[0] = new DayButton((Button) findViewById(R.id.monday_button));
        dayButtons[1] = new DayButton((Button) findViewById(R.id.tuesday_button));
        dayButtons[2] = new DayButton((Button) findViewById(R.id.wednesday_button));
        dayButtons[3] = new DayButton((Button) findViewById(R.id.thursday_button));
        dayButtons[4] = new DayButton((Button) findViewById(R.id.friday_button));
        dayButtons[5] = new DayButton((Button) findViewById(R.id.saturday_button));
        dayButtons[6] = new DayButton((Button) findViewById(R.id.sunday_button));
        for(final DayButton dayButton : dayButtons){
            dayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDate(dayButton.getYear(), dayButton.getMonth(), dayButton.getDate());
                }
            });
        }
    }

    private void updateCalendar(){
        TextView dateView = (TextView) findViewById(R.id.date);

        int year = gregCal.get(Calendar.YEAR);
        int month = gregCal.get(Calendar.MONTH);
        int date =  gregCal.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = GregorianCalendarFactory.getDayOfWeek(gregCal);

        GregorianCalendar temp = GregorianCalendarFactory.createGregorianCalendarCopy(gregCal);
        temp.add(Calendar.DAY_OF_MONTH, 1 - GregorianCalendarFactory.getDayOfWeek(temp));

        for(int i = 1; i < 8; i++){
            int tempDate = temp.get(Calendar.DAY_OF_MONTH);
            String dateString = "" + tempDate;
            DayButton dayButton = getDayButtonFromDayOfWeek(i);

            dayButton.setDate(tempDate);
            dayButton.setMonth(temp.get(Calendar.MONTH));
            dayButton.setYear(temp.get(Calendar.YEAR));

            dayButton.getButton().setText(dateString);

            temp.add(Calendar.DAY_OF_MONTH, 1);
        }

        String dateString = weekdayNumberToWord(dayOfWeek) + ", " + date + "." + (month + 1) + "." + year;
        dateView.setText(dateString);


        if(currentDayButton != null){
            currentDayButton.getButton().setBackgroundResource(R.drawable.rounded_button_white);
            currentDayButton.getButton().setTextColor(Color.BLACK);
        }
        currentDayButton = getDayButtonFromDayOfWeek(dayOfWeek);
        currentDayButton.getButton().setBackgroundResource(R.drawable.rounded_button_black);
        currentDayButton.getButton().setTextColor(Color.WHITE);

        if(this instanceof GlobalCalendarView){
            final Button fieldOfStudyButton = (Button) findViewById(R.id.filter_and_field_of_study);
            fieldOfStudyButton.setText(calendarDataManagement.getSelectedFieldOfStudy().getName());

            fieldOfStudyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setContentView(R.layout.activity_choose_fieldofstudy);

                    FieldOfStudyChooser fieldOfStudyChooser = new FieldOfStudyChooser((CalendarPresenter) calendarDataManagement, CalendarView.this);
                    Button confirmationButton = fieldOfStudyChooser.getFieldOfStudyConfirmationButton();
                    confirmationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String SelectdFieldOdStudy = ((Spinner) findViewById(R.id.semesterSpinner)).getSelectedItem().toString();


                            FieldOfStudy fos = calendarDataManagement.findChosenFieldOfSTudy(SelectdFieldOdStudy);
                            calendarWebUntis.resetCurrentShownGlobalEvents();
                            calendarDataManagement.setSelectedFieldOfStudy(fos);
                            buildWeeklyCalendar();

                        }
                    });
                }
            });
        }
    }

    private DayButton getDayButtonFromDayOfWeek(int dayOfWeek){
        return dayButtons[dayOfWeek - 1];
    }

    private void showNextDay(){
        gregCal.add(Calendar.DAY_OF_WEEK, 1);
        updateCalendar();
    }

    private void showPreviousDay(){
        gregCal.add(Calendar.DAY_OF_WEEK, -1);
        updateCalendar();
    }

    private void showDate(int year, int month, int date){
        gregCal.set(year, month, date);
        buildWeeklyCalendar();
    }

    private String weekdayNumberToWord(int number){
        String day = "";
        switch (number){
            case 1:
                day = "Monday";
                break;
            case 2:
                day = "Tuesday";
                break;
            case 3:
                day = "Wednesday";
                break;
            case 4:
                day = "Thursday";
                break;
            case 5:
                day = "Friday";
                break;
            case 6:
                day = "Saturday";
                break;
            case 7:
                day = "Sunday";
                break;
        }
        return day;
    }

    private void calculateHorizontalNeighbours(EventBoxView[] eventBoxes){
        for(int i = 0; i <= numberOfQuartersIn24Hours; i++){
            detectNeighboursInQuarter(i, eventBoxes);
        }
    }

    private void detectNeighboursInQuarter(int quarter, EventBoxView[] eventBoxes){
        ArrayList<EventBoxView> eventsOnThisQuarter = new ArrayList<>();
        for (EventBoxView eventBox : eventBoxes){
            if(eventBox.isOnQuarter(quarter)){
                eventsOnThisQuarter.add(eventBox);
            }
        }

        for (EventBoxView eventBox : eventsOnThisQuarter){
            eventBox.setMaxHorizontalNeighbours(eventsOnThisQuarter.size());
        }
    }

    private void adjustEventsWidths(EventBoxView[] eventBoxes){
        for(EventBoxView eventBox: eventBoxes){
            eventBox.setWidth(eventFieldWidth / eventBox.getMaxHorizontalNeighbours());
        }
    }

    private void calculateHorizontalPositions(EventBoxView[] eventBoxes){
        for(int i = 0; i <= numberOfQuartersIn24Hours; i++){
            adjustEventsLefts(i, eventBoxes);
        }
    }

    private void adjustEventsLefts (int quarter, EventBoxView[] eventBoxes){
        ArrayList<EventBoxView> eventsOnThisQuarter = new ArrayList<>();

        for (EventBoxView eventBox : eventBoxes){
            if(eventBox.isOnQuarter(quarter)){
                eventsOnThisQuarter.add(eventBox);
            }
        }

        int positionCounter = 0;
        for (int i = 0; i < eventsOnThisQuarter.size(); i++){
            EventBoxView eventBox = eventsOnThisQuarter.get(i);
            if(!eventBox.isPositioned()){
                while(isPositionTaken(positionCounter, eventsOnThisQuarter)){
                    positionCounter++;
                }
                eventBox.setX(eventFieldXStart + (positionCounter * eventBox.getWidth()));
                eventBox.setPosition(positionCounter);
            }

        }
    }

    private boolean isPositionTaken(int position, ArrayList<EventBoxView> eventBoxes){
        int i = 0;
        boolean taken = false;
        while(i < eventBoxes.size() && !taken){
            taken = eventBoxes.get(i).getPosition() == position;
            i++;
        }
        return taken;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private EventBoxView createEventBoxView(final Event event){

        final EventBoxView eventBox = new EventBoxView(event, this);
        eventBox.setY(calculateEventTop(eventBox));
        eventBox.setHeight(calculateEventHeight(eventBox));

        eventBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEventDetailView(eventBox);
            }
        });

        return eventBox;
    }

    public void openEventDetailView (final EventBoxView eventBoxView){
        LayoutInflater li = LayoutInflater.from(context);
        View view;
        final Event event = eventBoxView.getEvent();

        if (CalendarView.this instanceof GlobalCalendarView){
            view = li.inflate(R.layout.activity_add_event_course, null);
            Button buttonAddEvent = (Button) view.findViewById(R.id.buttonAddEvent);
            Button buttonAddCourse = (Button) view.findViewById(R.id.buttonAddCourse);
            TextView courseInformation = (TextView) view.findViewById(R.id.textCourse);
            buttonAddEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calendarDataManagement.addEvent(((UniversityEvent) event).getUntisID());
                    Toast toast = Toast.makeText(context, "Event added", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.TOP, 0, 0);
                    toast.show();

                }
            });
            buttonAddCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calendarDataManagement.addCourse(((UniversityEvent) event).getUntisID());
                    Toast toast = Toast.makeText(context, "Course added", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.TOP, 0, 0);
                    toast.show();
                }
            });
            courseInformation.setText(reFormatDate(event));
        } else {
            view = li.inflate(R.layout.activity_delete_event_course, null);

            /*Button buttonExportEvent = (Button) view.findViewById(R.id.buttonExportEvent);
            Button buttonExportCourse = (Button) view.findViewById(R.id.buttonExportCourse);*/
            Button buttonDeleteEvent = (Button) view.findViewById(R.id.buttonDeleteEvent);
            Button buttonDeleteCourse = (Button) view.findViewById(R.id.buttonDeleteCourse);
            //colorButtons
            Button colorBlue = (Button) view.findViewById(R.id.colorBlue);
            Button colorPurple = (Button) view.findViewById(R.id.colorPurple);
            Button colorPink = (Button) view.findViewById(R.id.colorPink);
            Button colorOrange = (Button) view.findViewById(R.id.colorOrange);
            Button colorRed = (Button) view.findViewById(R.id.colorRed);
            Button colorYellow = (Button) view.findViewById(R.id.colorYellow);
            Button colorLightgreen = (Button) view.findViewById(R.id.colorLightgreen);
            Button colorGreen = (Button) view.findViewById(R.id.colorGreen);
            Button colorAqua = (Button) view.findViewById(R.id.colorAqua);
            Button colorOcean = (Button) view.findViewById(R.id.colorOcean);

            TextView courseInformation = (TextView) view.findViewById(R.id.textViewEventMenu);
            buttonDeleteEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eventBoxView.removeFromView();
                    calendarDataManagement.deleteEvent(event.getId());
                    Toast toast = Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.TOP, 0, 0);
                    toast.show();
                }
            });
            buttonDeleteCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ArrayList<String> deletedEventIDs = calendarDataManagement.deleteCourse(((UniversityEvent)event).getCourseID());

                    for(EventBoxView eventBoxView : eventBoxes){
                        for(String eventID : deletedEventIDs){
                            if(eventBoxView.getEvent().getId().equals(eventID)){
                                eventBoxView.removeFromView();
                            }
                        }
                    }

                    Toast toast = Toast.makeText(context, "Course deleted", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.TOP, 0, 0);
                    toast.show();
                }
            });

            /*buttonExportEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO
                }
            });
            buttonExportCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //toDO
                }
            });*/


            //ToDo
            colorBlue.setOnClickListener(new OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                   // v.setBackgroundColor(getResources().getColor(R.color.colorPrimary,null));
                    eventBoxView.button.setBackgroundColor(getResources().getColor(R.color.blue,null));
                    eventBoxView.getEventboxEvent().setColor("#babce5");//TODO RAY change this.



                }
            });
            colorPurple.setOnClickListener(new OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    eventBoxView.button.setBackgroundColor(getResources().getColor(R.color.colorPrimary,null));
                    eventBoxView.getEventboxEvent().setColor("#cec1e7");

                }
            });
            colorPink.setOnClickListener(new OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    eventBoxView.button.setBackgroundColor(getResources().getColor(R.color.pink,null));
                    eventBoxView.getEventboxEvent().setColor("#e5bac4");

                }
            });
            colorOrange.setOnClickListener(new OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    eventBoxView.button.setBackgroundColor(getResources().getColor(R.color.orange,null));
                    eventBoxView.getEventboxEvent().setColor("#e5c9ba");

                }
            });
            colorRed.setOnClickListener(new OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    eventBoxView.button.setBackgroundColor(getResources().getColor(R.color.red,null));
                    eventBoxView.getEventboxEvent().setColor("#ff9994");

                }
            });
            colorYellow.setOnClickListener(new OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    eventBoxView.button.setBackgroundColor(getResources().getColor(R.color.yellow,null));
                    eventBoxView.getEventboxEvent().setColor("#e5e2ba");

                }
            });
            colorLightgreen.setOnClickListener(new OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    eventBoxView.button.setBackgroundColor(getResources().getColor(R.color.lightgreen,null));
                    eventBoxView.getEventboxEvent().setColor("#cde5ba");

                }
            });
            colorGreen.setOnClickListener(new OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    eventBoxView.button.setBackgroundColor(getResources().getColor(R.color.green,null));
                    eventBoxView.getEventboxEvent().setColor("#bae5c0");

                }
            });
            colorAqua.setOnClickListener(new OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    eventBoxView.button.setBackgroundColor(getResources().getColor(R.color.aqua,null));
                    eventBoxView.getEventboxEvent().setColor("#bae5da");

                }
            });
            colorOcean.setOnClickListener(new OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    eventBoxView.button.setBackgroundColor(getResources().getColor(R.color.ocean,null));
                    eventBoxView.getEventboxEvent().setColor("#b7d4e7");

                }
            });

            courseInformation.setText(reFormatDate(event));
        }


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setView(view);
        alertDialogBuilder.show();
    }

    public String reFormatDate(Event event){
        String res="";
        String startTimeRes = "";
        String endTimeRes = "";
        try {
            String startTime =event.getStartTime().toString();
            String endTime =event.getEndTime().toString();
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("EEE MMM ww hh:mm:ss Z yyyy");
            Date newDate0 = format.parse(startTime);
            Date newDate1 = format.parse(endTime);
            format = new java.text.SimpleDateFormat("MMM dd,yyyy HH:mm ");
            startTimeRes = format.format(newDate0);
            startTimeRes+= "Uhr";
            endTimeRes = format.format(newDate1);
            endTimeRes+= "Uhr";

        }catch (Exception e){
            Log.e("Format  ","Failed to Reformat Date Class: CalendarView.java");
        }
        return res= event.getName() + "\n" + startTimeRes + "\n" + endTimeRes;
    }
/*
    @Override
    public void onBackPressed(){ //todo why overriding that function?

        getEventInformation("1");

        boolean add = true;

            LayoutInflater li = LayoutInflater.from(context);
        if(add == false) {
            View view = li.inflate(R.layout.activity_delete_event_course, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

            TextView courseInformation = (TextView) view.findViewById(R.id.textCourse);
            Button buttonExportEvent = (Button) view.findViewById(R.id.buttonExportEvent);
            Button buttonExportCourse = (Button) view.findViewById(R.id.buttonExportCourse);
            Button buttonDeleteEvent = (Button) view.findViewById(R.id.buttonDeleteEvent);
            Button buttonDeleteCourse = (Button) view.findViewById(R.id.buttonDeleteCourse);

            buttonExportEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            buttonExportCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            buttonDeleteEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            buttonDeleteCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            alertDialogBuilder.setView(view);
            alertDialogBuilder.show();
        }

        //um alertDialog für add_event_course anzuzeigen
        else{
            View view = li.inflate(R.layout.activity_add_event_course, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

            Button buttonAddEvent = (Button) view.findViewById(R.id.buttonAddEvent);
            Button buttonAddCourse = (Button) view.findViewById(R.id.buttonAddCourse);

            buttonAddEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            buttonAddCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            alertDialogBuilder.setView(view);
            alertDialogBuilder.show();
        }
    }
*/
    private int calculateEventHeight(EventBoxView eventBox){
        int bottom = calculateEventBottom(eventBox);
        int top = calculateEventTop(eventBox);
        return  bottom - top;
    }

    private int calculateEventTop(EventBoxView eventBox){
        return eventFieldYStart + (int)(heightPerQuarter * eventBox.getStartQuarter());
    }

    private int calculateEventBottom(EventBoxView eventBox){
        return eventFieldYStart + (int)(heightPerQuarter * eventBox.getEndQuarter());
    }

    private int convertDateToQuarter(Date date){
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return minuteToQuarter(minute + hourToMinute(hour));

    }

    private int minuteToQuarter(int minute){
        return minute / 15;
    }

    private int hourToMinute(int hour){
        return hour * 60;
    }


    private class EventBoxView {


        private Event event;
        private Button button;
        private int startQuarter;
        private int endQuarter;
        private int y;
        private int x;
        private int width;
        private int height;
        private int maxHorizontalNeighbours;
        private int position = -1;
        private String color = "FF0000";
        private int eventColor = 0;
//"FFFFFF";
        private LinearLayout.LayoutParams layoutParams;

        @RequiresApi(api = Build.VERSION_CODES.M)
        EventBoxView(Event event, Context context){
            this.event = event;
             eventColor =this.GetEventsColor(event);
            this.button = new Button(context);
            this.layoutParams = new LinearLayout.LayoutParams(0, 0);
            this.button.setLayoutParams(this.layoutParams);
            String buttonText = event.toString();
            this.button.setText(buttonText);
            this.button.setBackground(null);
            this.button.setBackgroundColor(eventColor);
            this.button.getBackground().setAlpha(182);

            this.startQuarter = convertDateToQuarter(event.getStartTime());
            this.endQuarter = convertDateToQuarter(event.getEndTime());
            this.maxHorizontalNeighbours = 1;
        }
  //Ray: this function getEventBoxEvent used to set the color oof the Event when the user changes the color of the event on the view
        public Event getEventboxEvent() {
            return this.event;
        }
        // needed thos so when creating an event the color should be difined.

        @RequiresApi(api = Build.VERSION_CODES.M)
        public int GetEventsColor(Event event){
            String eColor=event.getColor();
            int found =getResources().getColor(R.color.gray,null);

            switch (eColor){
                case ("#babce5") : found =getResources().getColor(R.color.blue,null);  // blue
                    break;
                case ("#cec1e7") : found =getResources().getColor(R.color.purple,null);  // purple
                    break;
                case ("#e5bac4") :found = getResources().getColor(R.color.pink,null); //PINK
                    break;
                case ("ff9994") :found = getResources().getColor(R.color.red,null);  //red
                    break;
                case ("#e5c9ba") :found =getResources().getColor(R.color.orange,null); ;//orange
                    break;
                case ("#e7e7e7") :found =getResources().getColor(R.color.lightgray,null); ;//light gray
                    break;
                case ("#e5e2ba") :found =getResources().getColor(R.color.yellow,null); ;//yellow
                    break;
                case ("#cde5ba") :found =getResources().getColor(R.color.lightgreen,null); ;//lightgreen
                    break;
                case ("#bae5c0") :found =getResources().getColor(R.color.green,null); ;//green
                    break;
                case ("#bae5da") :found =getResources().getColor(R.color.aqua,null); ;//aqua
                    break;
                case ("#b7d4e7") :found =getResources().getColor(R.color.ocean,null); ;//ocean
                    break;

            }
              return found;
        }

        Button getButton() {
            return button;
        }

        int getStartQuarter() {
            return startQuarter;
        }

        void setStartQuarter(int startQuarter) {
            this.startQuarter = startQuarter;
        }

        int getEndQuarter() {
            return endQuarter;
        }

        void setEndQuarter(int endQuarter) {
            this.endQuarter = endQuarter;
        }

        int getY() {
            return y;
        }

        void setY(int y) {
            this.y = y;
            this.button.setY(y);
        }

        int getX() {
            return x;
        }

        void setX(int x) {
            this.x = x;
            this.button.setX(x);
        }

        int getWidth() {
            return width;
        }

        void setWidth(int width) {
            this.width = width;
            this.layoutParams.width = width;
            //this.button.setWidth(width);
        }

        int getHeight() {
            return height;
        }

        void setHeight(int height) {
            this.height = height;
            this.layoutParams.height = height;
            //this.button.setHeight(height);
        }

        void setOnClickListener(View.OnClickListener listener){
            this.button.setOnClickListener(listener);
        }

        Event getEvent() {
            return event;
        }

        void setEvent(Event event) {
            this.event = event;
        }

        int getMaxHorizontalNeighbours() {
            return maxHorizontalNeighbours;
        }

        void setMaxHorizontalNeighbours(int maxHorizontalNeighbours) {
            if(maxHorizontalNeighbours > this.maxHorizontalNeighbours){
                this.maxHorizontalNeighbours = maxHorizontalNeighbours;
            }
        }

        boolean isOnQuarter(int quarter){
            return getStartQuarter() <= quarter & getEndQuarter() > quarter;
        }

        boolean isPositioned() {
            return position > -1;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getColor(){
            return this.color;
        }

        public void setColor(String color){
            this.color = color;
        } //TODO IMPLEMENT SETCOLOR RAY

        public void removeFromView(){
            ((ViewGroup) this.button.getParent()).removeView(this.button);
        }

        @Override
        public String toString(){
            return "Name: " + event.getName();
        }

        @Override
        public boolean equals(Object o){
            return (o instanceof EventBoxView) && (event == ((EventBoxView)o).getEvent());
        }
    }

    private class DayButton{
        private Button button;
        private int date;
        private int month;
        private int year;

        DayButton(Button button){
            this.button = button;
        }

        void setOnClickListener(View.OnClickListener onClickListener){
            this.button.setOnClickListener(onClickListener);
        }

        Button getButton() {
            return button;
        }

        void setButton(Button button) {
            this.button = button;
        }

        int getDate() {
            return date;
        }

        void setDate(int date) {
            this.date = date;
        }

        int getMonth() {
            return month;
        }

        void setMonth(int month) {
            this.month = month;
        }

        int getYear() {
            return year;
        }

        void setYear(int year) {
            this.year = year;
        }
    }

    // my methods come bellow here.-----------------------------------------------------------


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showMonthlyViewCalendar()
    {
        setContentView(R.layout.monthly_calendar_view);

        _calendar = Calendar.getInstance(Locale.getDefault());
        month = _calendar.get(Calendar.MONTH) + 1;
        year = _calendar.get(Calendar.YEAR);

        Log.d(tag, "Calendar Instance:= " + "Month: " + month + ""  + "Year: "
                + year);

        selectedDayMonthYearButton = (Button) this.findViewById(R.id.selectedDayMonthYear);
        selectedDayMonthYearButton.setText("Event: " );


        prevMonth = (ImageView) this.findViewById(R.id.prevMonth);
        prevMonth.setOnClickListener(this);

        currentMonth = (TextView) this.findViewById(R.id.currentMonth);
        currentMonth.setText(DateFormat.format(dateTemplate,
                _calendar.getTime()));

        nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
        nextMonth.setOnClickListener(this);
      //  setting = (Button) this.findViewById(R.id.settings);
        /*
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOptionsMenu();
            }
        });

*/
        calendarView = (GridView) this.findViewById(R.id.calendar);


// Initialised
        adapter = new GridCellAdapter(getApplicationContext(),
                R.id.calendar_day_gridcell, month, year);
        adapter.notifyDataSetChanged();
        calendarView.setAdapter(adapter);

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setGridCellAdapterToDate(int month, int year) {
        adapter = new GridCellAdapter(getApplicationContext(),
                R.id.calendar_day_gridcell, month, year);
        _calendar.set(year, month-1, _calendar.get(Calendar.DAY_OF_MONTH));
        currentMonth.setText(DateFormat.format(dateTemplate,
                _calendar.getTime()));
        adapter.notifyDataSetChanged();
        calendarView.setAdapter(adapter);
    }
/*
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_monthly, menu);


        return true;
    }
*/

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v){
        if(v==prevMonth){
            if(month<=1){
                month=12;
                year--;
            }else{
                month--;
            }
            Log.d(tag,"Setting prev Month in GridCellAdapter:"+"Month"+month +"Year"+year);
            setGridCellAdapterToDate(month,year);
        }
        if(v==nextMonth){
            if(month>11){
                month=1;
                year++;
            }else{
                month++;
            }
            Log.d(tag,"Setting prev Month in GridCellAdapter:"+"Month"+month +"Year"+year);
            setGridCellAdapterToDate(month,year);
        }
    }
    @Override
    public void onDestroy(){
        Log.d(tag,"Destroying View.....");
        super.onDestroy();
    }



    //inner class  for monthly view calendar
    @TargetApi(3)
    public class GridCellAdapter extends BaseAdapter implements View.OnClickListener {
        private static final String tag = "GridCellAdapter";
        private final Context _context;
        private final List<String> list;
        private static final int DAY_OFFSET = 1;
        //TODO after importing the project in our main file in git, i will have to change all to dynamically created string to strings created in res files.
        private final String[] weekdays = new String[]{"Sun", "Mon", "Tue",
                "Wed", "Thu", "Fri", "Sat"};
        private final String[] months = {"January", "February", " March ",
                "April", "May", "June", "July", "August", "September",
                "October", "November", "December"};
        private final int[] daysOfMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30,
                31, 30, 31};

        private int daysInMonth;
        private int currentDayOfMonth;
        private int currentWeekDay;
        private Button gridcell;

        private TextView num_events_per_day;  //TODO need to implement this to show the number of event per day on the monthly view of the calendar
        private  HashMap<String, Integer> eventsPerMonthMap =new HashMap<>(); //TODO VARIABLE
        @SuppressWarnings("unused")
        @SuppressLint({"NewApi", "NewApi", "NewApi", "NewApi"})
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat(
                "dd-MMM-yyyy");

        // Days in Current Month
        @RequiresApi(api = Build.VERSION_CODES.N)
        public GridCellAdapter(Context context, int textViewResourceId,
                               int month, int year) {
            super();
            this._context = context;
            this.list = new ArrayList<String>();
            Log.d(tag, "==> Passed in Date FOR Month: " + month + ""
                    + "Year: " + year);
            Calendar calendar = Calendar.getInstance();
            setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
            setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
            Log.d(tag, "New Calendar:= " + calendar.getTime().toString());
            Log.d(tag, "CurrentDayOfWeek :" + getCurrentWeekDay());
            Log.d(tag, "CurrentDayOfMonth :" + getCurrentDayOfMonth());

            // Print Month
            printMonth(month, year);
       if(CalendarView.this instanceof GlobalCalendarView){

       }else{
           eventsPerMonthMap = findNumberOfEventsPerMonth(year, month);
       }
        }

        private String getMonthAsString(int i) {
            return months[i];
        }

        private String getWeekDayAsString(int i) {
            return weekdays[i];
        }

        private int getNumberOfDaysOfMonth(int i) {
            return daysOfMonth[i];
        }

        public String getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
        //TODO implement this me with data from the database

        // retrieve a entries fom the sql databse then iterate to find element by date

        @RequiresApi(api = Build.VERSION_CODES.N)
        private HashMap<String, Integer> findNumberOfEventsPerMonth(int year,
                                                                    int month) {

            HashMap<Integer,Integer> receivedList = new HashMap<>();
               receivedList =calendarWebUntis.getEventsPerMonths(year,month);
            HashMap result = new HashMap<String, Integer>();
            DateFormat dateFormatter = new DateFormat();
            SimpleDateFormat f = new SimpleDateFormat("dd");
            Calendar calendar = Calendar.getInstance();




       for(Integer key : receivedList.keySet()){

           if(receivedList.get(key)!=null && !receivedList.isEmpty()){
               Log.v("Key CV",Integer.toString(key));
               Integer value = receivedList.get(key);
               String DAY = Integer.toOctalString(key);
              // String DAY = DateFormat.format("dd",key).toString();
               Log.v("DAY stored IN MAP",DAY);
               Log.v("DAY'S VALUE",Integer.toString(value));
               Log.v("BELMO CV","LIST NONT EMPTY");
               result.put(DAY,value);
           }

       }

            return result;
    }


        @Override
        public long getItemId(int position) {
            return position;
        }

        //------------------------------------------------------
        /**
         * Prints Month
         *
         * @param mm
         * @param yy
         */
        private void printMonth(int mm, int yy) {
            Log.d(tag,"==> printMonth: mm:" + mm +"" +"yy:" + yy);
            int trailingSpaces = 0;
            int daysInPrevMonth = 0;
            int prevMonth = 0;
            int prevYear = 0;
            int nextMonth = 0;
            int nextYear = 0;

            int currentMonth = mm-1;
            String currentMonthName = getMonthAsString(currentMonth);
            daysInMonth = getNumberOfDaysOfMonth(currentMonth);

            Log.d(tag,"Current Month:" +"" + currentMonthName +" having"
                    + daysInMonth +" days.");


            GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1);
            Log.d(tag,"Gregorian Calendar:=" + cal.getTime().toString());

            if (currentMonth == 11) {
                prevMonth = currentMonth- 1;
                daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
                nextMonth = 0;
                prevYear = yy;
                nextYear = yy + 1;
                Log.d(tag,"*->PrevYear:" + prevYear +" PrevMonth:"
                        + prevMonth +" NextMonth:" + nextMonth
                        +" NextYear:" + nextYear);
            } else if (currentMonth == 0) {
                prevMonth = 11;
                prevYear = yy -1;
                nextYear = yy;
                daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
                nextMonth = 1;
                Log.d(tag,"**–> PrevYear:" + prevYear +" PrevMonth:"
                        + prevMonth +" NextMonth:" + nextMonth
                        +" NextYear:" + nextYear);
            } else {
                prevMonth = currentMonth- 1;
                nextMonth = currentMonth + 1;
                nextYear = yy;
                prevYear = yy;
                daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
                Log.d(tag,"***—> PrevYear:" + prevYear +" PrevMonth:"
                        + prevMonth +" NextMonth:" + nextMonth
                        +" NextYear:" + nextYear);
            }

            int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
            trailingSpaces = currentWeekDay;

            Log.d(tag,"Week Day:" + currentWeekDay +"is"
                    + getWeekDayAsString(currentWeekDay));
            Log.d(tag,"No. Trailing space to Add:" + trailingSpaces);
            Log.d(tag,"No. of Days in Previous Month:" + daysInPrevMonth);

            if (cal.isLeapYear(cal.get(Calendar.YEAR)))
                if (mm == 2)
                    ++daysInMonth;
                else if (mm == 3)
                    ++daysInPrevMonth;

// Trailing Month days
            for (int i = 0; i < trailingSpaces; i++) {
                Log.d(tag,
                        "PREV MONTH:="
                                + prevMonth
                                +" =>"
                                + getMonthAsString(prevMonth)
                                +""
                                + String.valueOf((daysInPrevMonth
                                - trailingSpaces + DAY_OFFSET)
                                + i));
                list.add(String
                        .valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)
                                + i)
                        +"-GREY"
                        +"-"
                        + getMonthAsString(prevMonth)
                        +"-"
                        + prevYear);
            }

// Current Month Days
            for (int i = 1; i <= daysInMonth; i++) {
                Log.d(currentMonthName, String.valueOf(i) +""
                        + getMonthAsString(currentMonth) +"" + yy);
                if (i == getCurrentDayOfMonth()) {
                    list.add(String.valueOf(i) +"-BLUE" + "- "
                            + getMonthAsString(currentMonth) +"-"+ yy);
                } else {
                    list.add(String.valueOf(i) +"-WHITE"+"-"
                            + getMonthAsString(currentMonth) +"-"+ yy);
                }
            }

// Leading Month days
            for (int i = 0; i < list.size() % 7; i++) {
                Log.d(tag,"NEXT MONTH:=" + getMonthAsString(nextMonth));
                list.add(String.valueOf(i + 1) +"-GREY" +"-"
                        + getMonthAsString(nextMonth) +"-" + nextYear);
            }
        }

        //-----------------------------------------------------

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) _context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.screen_gridcell, parent, false);
            }

// Get a reference to the Day gridcell
            gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
            gridcell.setOnClickListener(this);

// ACCOUNT FOR SPACING

            Log.d(tag, "Current Day: "+ getCurrentDayOfMonth());
            String[] day_color = list.get(position).split("-");
            String theday = day_color[0];
            Log.d(tag, "DAY-->: "+ theday);
            String themonth = day_color[2];
            String theyear = day_color[3];
            if ((!eventsPerMonthMap.isEmpty()) && (eventsPerMonthMap != null)) {
                if (eventsPerMonthMap.containsKey(theday)) {
                    num_events_per_day = (TextView) row
                            .findViewById(R.id.num_events_per_day);
                    Integer numEvents = (Integer) eventsPerMonthMap.get(theday);
                    num_events_per_day.setText(numEvents.toString()); // TODO i am setting ther amount of events per day here oin the view : I need to get the evenspaerMonths first
                    selectedDayMonthYearButton.setText("you have "+numEvents.toString()+" Events today");
                }
            }

// Set the Day GridCell
            gridcell.setText(theday);
            gridcell.setTag(theday + "-" + themonth + "-" + theyear);
            Log.d(tag, "Setting GridCell " + theday + "-" + themonth + "-"
                    + theyear);

            if (day_color[1].equals("GREY")) {
                gridcell.setTextColor(getResources().getColor(R.color.gray,null));
            }
            if (day_color[1].equals("WHITE")) {
                gridcell.setTextColor(getResources().getColor(R.color.lightgray02,null));
            }
            if (day_color[1].equals("BLUE")) {
                gridcell.setTextColor(getResources().getColor(R.color.orrange,null));
            }
            return row;
        }
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View view) {


            String date_month_year = (String) view.getTag();
            String[]stringdate = date_month_year.split("-");
            int day = Integer.parseInt(stringdate[0]);
            int year= Integer.parseInt(stringdate[2]);
            int month = convertStringMonthToIntegerMonth(stringdate[1]);
            selectedDayMonthYearButton.setText("Selected:" + date_month_year);
            // call the function re3sponsible for the view changing from monthly to weekly/dayly.
            setCalendarContentView();
            showDate(year,month - 1,day);
            Log.e("Selected date:" , date_month_year);
            try{
               // Date parseDate = dateFormatter.parse(date_month_year);
                Date parseDate = dateFormatter.parse(date_month_year);
                Log.d(tag,"Parse date" + parseDate.toString());



            } catch (ParseException e) {
                e.printStackTrace();
            }


            /**
             try {
             Date parseDate =dateFormatter.parse(date_month_year);
             //  Date parsedDate = dateFormatter.parse(date_month_year);
             Log.d(tag,"Parse date" + parsedDate.toString());

             } catch (ParseException e) {
             e.printStackTrace();
             }
             */
        }
        public int getCurrentDayOfMonth() {
            return currentDayOfMonth;
        }

        private void setCurrentDayOfMonth(int currentDayOfMonth) {
            this.currentDayOfMonth = currentDayOfMonth;
        }

        public void setCurrentWeekDay(int currentWeekDay) {
            this.currentWeekDay = currentWeekDay;
        }

        public int getCurrentWeekDay() {
            return currentWeekDay;
        }
        @RequiresApi(api = Build.VERSION_CODES.N)
        public int convertStringMonthToIntegerMonth(String month){

            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(new SimpleDateFormat("MMM").parse(month));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int monthInt = cal.get(Calendar.MONTH) + 1;

            return monthInt;
        }

    }


    public iCalendarPresenter.iCalendarDataManagement getCalendarDataManagement() {
        return calendarDataManagement;
    }

    public iCalendarPresenter.iCalendarWebUntis getCalendarWebUntis() {
        return calendarWebUntis;
    }




}
