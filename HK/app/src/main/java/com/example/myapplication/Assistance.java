package com.example.myapplication;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;



import java.util.Calendar;

public class Assistance extends AppCompatActivity {
    Button bt_date, bt_time;
    TextView tv_date, tv_time, tv_status;
    SwitchCompat sw_set;
    int year, month, day, hour, minute;
    Calendar ca = Calendar.getInstance();


    //日期选择回调监听器
    DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int month, int day) {
            Assistance.this.year = year;
            Assistance.this.month = month;
            Assistance.this.day = day;

            tv_date.setText(year + "年" + (month + 1) + "月" + day + "日");
        }
    };


    //时间选择回调监听器
    TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {

            Assistance.this.hour = hourOfDay;
            Assistance.this.minute = minute;

            tv_time.setText(hourOfDay + "时" + minute + "分");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistance);

        //初始化控件
        bt_date =findViewById(R.id.btn_date);
        bt_time =findViewById(R.id.btn_time);
        sw_set =findViewById(R.id.sw_set);
        tv_date =findViewById(R.id.tv_date);
        tv_time =findViewById(R.id.tv_time);
        tv_status =findViewById(R.id.tv_status);


        //获取当前时间
        year =ca.get(Calendar.YEAR);
        month =ca.get(Calendar.MONTH);
        day =ca.get(Calendar.DAY_OF_MONTH);
        hour =ca.get(Calendar.HOUR_OF_DAY);
        minute =ca.get(Calendar.MINUTE);
        bt_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(Assistance.this,mOnDateSetListener,year,month,day).show();
            }
        });

        bt_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(Assistance.this,mOnTimeSetListener,hour,minute,true).show();
            }
        });

        sw_set.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    setAlarm();
                }else {
                    cancelAlarm();
                }
            }
        });

    }

    /**
     * 设置闹铃
     */
    private void setAlarm() {
        ca = Calendar.getInstance();
        ca.set(year, month, day, hour, minute, 0);
        Intent intent = new Intent(Assistance.this, ALM.class);
        PendingIntent pi = PendingIntent.getActivity(Assistance.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, ca.getTimeInMillis(), pi);
        Toast.makeText(this, "闹钟已设置", Toast.LENGTH_SHORT).show();
        tv_status.setText("闹钟时间：" + year + "年" + (month + 1) + "月" + day + "日" + hour + "时" + minute + "分");

    }

    /**
     * 取消闹铃
     * */

    private void cancelAlarm() {
        Intent intent = new Intent(Assistance.this, ALM.class);
        PendingIntent pi = PendingIntent.getActivity(Assistance.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(pi);
        Toast.makeText(this, "闹钟已取消", Toast.LENGTH_SHORT).show();
        tv_status.setText("not set");
    }
}

