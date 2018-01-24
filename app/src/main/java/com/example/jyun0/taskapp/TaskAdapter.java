package com.example.jyun0.taskapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends BaseAdapter{
    private LayoutInflater layoutInflater = null;
    private List<Task> mTaskList;

    public TaskAdapter(Context context){
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setTaskList(List<Task> taskList){
        mTaskList = taskList;
    }

    @Override
    public int getCount() {
        return mTaskList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTaskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mTaskList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       if(convertView == null){
           convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2,null);
       }

       TextView textView1 = (TextView) convertView.findViewById(android.R.id.text1);
       TextView textView2 = (TextView) convertView.findViewById(android.R.id.text2);
        
       textView1.setText(mTaskList.get(position).getTitle());

       SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy-MM-dd HH:mm", Locale.ENGLISH);
       Date date = mTaskList.get(position).getDate();
       textView2.setText(simpleDateFormat.format(date));

       return convertView;
    }
}
