package com.wua.mc.webuntisapp;

import com.wua.mc.webuntisapp.model.WebUntisClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {


    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getCurrentSchoolYear() throws Exception{
        //JSONObject result = JSONFileReader.readPersonalData(); //hier wird die Datei gelesen


        String username = "Usercampusap2";//result.getString("username");
        String password = "konst6app6";//result.getString("password");

        WebUntisClient wuc = new WebUntisClient(username, password, "HS+Reutlingen");
        JSONObject res = new JSONObject("{\"result\":{\"id\": 7,\"name\": \"2016/2017\",\"startDate\": 20160829,\"endDate\": 20170731}}");
        assertEquals(res.getJSONObject("result").toString(),
                wuc.getCurrentSchoolYear().getJSONObject("response").getJSONObject("result").toString());

    }


    @Test
    public void LoginTest(){
        String username = "Usercampusap2";//result.getString("username");
        String password = "konst6app6";//result.getString("password");

        WebUntisClient wuc = new WebUntisClient(username, password, "HS+Reutlingen");
        JSONObject json= wuc.authenticate();
        System.out.println(json.toString());
        try {

            String sessionID = json.getJSONObject("result").getString("sessionId");
            System.out.println("Session ID : "+sessionID);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void gregorianCalendarTest(){
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY);
        System.out.println("DAY_OF_WEEK: " + (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 7 : calendar.get(Calendar.DAY_OF_WEEK) - 1));
        System.out.println("DATE: " + calendar.get(Calendar.DATE));
        System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
        System.out.println("MONTH: " + (calendar.get(Calendar.MONTH) + 1));
        System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
        System.out.println("FIRST_DAY_OF_WEEK: " + calendar.getFirstDayOfWeek());

        GregorianCalendar temp = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        temp.add(Calendar.DAY_OF_MONTH, 2 - temp.get(Calendar.DAY_OF_WEEK));


        int dayOfWeek = temp.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth =  temp.get(Calendar.DAY_OF_MONTH);
        int month = temp.get(Calendar.MONTH) + 1;
        int year = temp.get(Calendar.YEAR);

        String dateString = dayOfMonth + "." + month + "." + year;

        System.out.println(dateString);
    }

    @Test
    public void jsonStructureTest(){
        boolean throwedException = false;
        String jsonSample = "{\"result\":{\"id\": 7,\"name\": \"2016/2017\",\"startDate\": 20160829,\"endDate\": 20170731}}";
        JSONObject jsonObject = null;

        try{
            jsonObject = new JSONObject(jsonSample);
        }
        catch(JSONException e){
            System.out.println("Invalid string");
        }

        try {
            JSONObject result = jsonObject.getJSONObject("result");
            result.getInt("id");
            result.getString("name");
            result.getInt("startDate");
            result.getInt("endDate");
        }
        catch (JSONException e){
            throwedException = true;
        }

        assertFalse(throwedException);
        
        try {
            jsonObject.getInt("result");
        }
        catch (JSONException e){
            throwedException = true;
        }

        assertTrue(throwedException);
    }

}