 package com.example.jyun0.taskapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.support.v7.widget.Toolbar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;

 public class inputActivity extends AppCompatActivity {

    private int Year, Month, Day, Hour, Minute;
    private Button dateButton, timeButton;
    private EditText titleEdit, contentEdit;
    private Task task;
    private View.OnClickListener OnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(inputActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            Year = year;
                            Month = monthOfYear;
                            Day = dayOfMonth;
                            String dateString = Year + "/" + String.format("%02d",(Month + 1)) + "/" + String.format("%02d", Day);
                            dateButton.setText(dateString);
                        }
                    }, Year, Month, Day);
            datePickerDialog.show();
        }
    };

    private View.OnClickListener OnTimeClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            TimePickerDialog timePickerDialog = new TimePickerDialog(inputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            Hour = hourOfDay;
                            Minute = minute;
                            String timeString = String.format("%02d",Hour) + ":" + String.format("%02d",Minute);
                            timeButton.setText(timeString);
                        }
                    }, Hour, Minute, false);
            timePickerDialog.show();
        }
    };

    private View.OnClickListener OnDoneClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            addTask();
            finish();
        }
    };

     @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        //ActionBarを設定する
         Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
         setSupportActionBar(toolbar);
         if(getSupportActionBar() != null){
             getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         }

         //UI部品の設定
         dateButton = (Button)findViewById(R.id.date_button);
         dateButton.setOnClickListener(OnDateClickListener);
         timeButton = (Button)findViewById(R.id.times_button);
         timeButton.setOnClickListener(OnTimeClickListener);
         findViewById(R.id.done_button).setOnClickListener(OnDoneClickListener);
         titleEdit = (EditText)findViewById(R.id.title_edit_text);
         contentEdit = (EditText)findViewById(R.id.content_edit_text);

         // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
         Intent intent = getIntent();
         int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
         Realm realm = Realm.getDefaultInstance();

         // Task の id が taskId のものが検索され、findFirst() によって最初に見つかったインスタンスが返され
         //task へ代入される
         task = realm.where(Task.class).equalTo("id",taskId).findFirst();
         realm.close();


         if(task == null){
             // 新規作成の場合
             Calendar calendar = Calendar.getInstance();
             Year = calendar.get(Calendar.YEAR);
             Month = calendar.get(Calendar.MONTH);
             Day = calendar.get(Calendar.DAY_OF_MONTH);
             Hour = calendar.get(Calendar.HOUR_OF_DAY);
             Minute = calendar.get(Calendar.MINUTE);
         }else{
             // 更新の場合
             titleEdit.setText(task.getTitle());
             contentEdit.setText(task.getContents());

             Calendar calendar = Calendar.getInstance();
             calendar.setTime(task.getDate());
             Year = calendar.get(Calendar.YEAR);
             Month = calendar.get(Calendar.MONTH);
             Day = calendar.get(Calendar.DAY_OF_MONTH);
             Hour = calendar.get(Calendar.HOUR_OF_DAY);
             Minute = calendar.get(Calendar.MINUTE);

             String dateString = Year + "/" + String.format("%02d",(Month + 1)) +
                     String.format("%02d", Day);
             String timeString = String.format("%02d",Hour) + ":" +
                     String.format("%02d", Minute);
             dateButton.setText(dateString);
             timeButton.setText(timeString);
         }
     }

     private void addTask(){
         Realm realm = Realm.getDefaultInstance();
         realm.beginTransaction();

         if(task == null){
             // 新規作成の場合
             task = new Task();

             RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();

             int identifier;
             if(taskRealmResults.max("id") != null){
                 identifier = taskRealmResults.max("id").intValue() + 1;
             }else{
                 identifier = 0;
             }
             task.setId(identifier);
         }

         String title = titleEdit.getText().toString();
         String content = contentEdit.getText().toString();

         task.setTitle(title);
         task.setContents(content);
         GregorianCalendar calendar = new GregorianCalendar(Year,Month,Day,Hour,Minute);
         Date date = calendar.getTime();
         task.setDate(date);

         realm.copyToRealmOrUpdate(task);
         realm.commitTransaction();

         realm.close();

         Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
         resultIntent.putExtra(MainActivity.EXTRA_TASK, task.getId());
         PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                 this,
                 task.getId(),
                 resultIntent,
                 PendingIntent.FLAG_UPDATE_CURRENT
         );

         AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
         alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), resultPendingIntent);
     }
}
