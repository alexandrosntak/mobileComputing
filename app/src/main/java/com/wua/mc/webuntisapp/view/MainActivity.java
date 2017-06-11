package com.wua.mc.webuntisapp.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wua.mc.webuntisapp.R;
import com.wua.mc.webuntisapp.model.DatabaseManager;
import com.wua.mc.webuntisapp.presenter.CalendarPresenter;

import static com.wua.mc.webuntisapp.R.layout.activity_add_color;
import static com.wua.mc.webuntisapp.R.layout.activity_add_event_course;
import static com.wua.mc.webuntisapp.R.layout.activity_choose_fieldofstudy;
import static com.wua.mc.webuntisapp.R.layout.activity_personal_calendar;

public class MainActivity extends Activity {

    CalendarPresenter cp = new CalendarPresenter();
    static boolean firstLogin=true;
    DatabaseManager dbmgr = new DatabaseManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (firstLogin){
            Button loginButton = (Button)this.findViewById(R.id.loginButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    try {
                        EditText username   = (EditText)findViewById(R.id.usernameField);
                        EditText password   = (EditText)findViewById(R.id.passwordField);

                        String u= username.getText().toString();
                        String p = password.getText().toString();
                        Log.v(u,p );

                        cp.login(username.getText().toString(), password.getText().toString());
                        dbmgr.loginDB(username.getText().toString(), password.getText().toString());

                        Log.v("statusLogin","Login Successfull");
                        setContentView(activity_choose_fieldofstudy);
                        Button buttonSelectColor = (Button) findViewById(R.id.buttonSelectColor);
                        buttonSelectColor.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                setContentView(activity_add_event_course);
                            }
                        });
                        firstLogin=false;
                    }catch (Exception e){
                        Log.v("statusLogin","Login Failed");
                        Toast errorToast = Toast.makeText(getApplication() , "Login failed", Toast.LENGTH_SHORT);
                        errorToast.show();

                    }
                }
            });

        }else{
            setContentView(activity_personal_calendar);
        }
        
//        Intent intent1 = new Intent(this, GlobalCalendarView.class);
//        startActivity(intent1);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.logout:
                Toast.makeText(MainActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.export_calendar:
                Toast.makeText(MainActivity.this, "Export Calendar", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
