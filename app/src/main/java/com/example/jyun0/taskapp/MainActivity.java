package com.example.jyun0.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;


public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_TASK = "com.example.jyun0.taskapp.MainActivity.TASK";
    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object o) {
            reloadListView();
        }
    };

    private ListView listView;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(MainActivity.this, inputActivity.class);
               startActivity(intent);
            }
        });

        //Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // ListViewの設定
        taskAdapter = new TaskAdapter(MainActivity.this);
        listView = (ListView) findViewById(R.id.listView1);

        // ListViewをタップしたときの処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                // 入力・編集する画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, inputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());

                startActivity(intent);
            }
        });

        // ListViewを長押ししたときの処理
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                // タスクを削除する
                final Task task = (Task) parent.getAdapter().getItem(position);

                //ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("DELETE");
                builder.setMessage(task.getTitle() + "Are you sure do you want to delete?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RealmResults<Task> results = mRealm.where(Task.class).equalTo(
                                "id",task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL",null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        // アプリ起動時に表示テスト用のタスクを作成する
       // addTaskForTest();
        reloadListView();
    }

    private void reloadListView() {
        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAllSorted
                ("date", Sort.DESCENDING);

        // 上記の結果を、TaskList としてセットする
        taskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));

        // TaskのListView用のアダプタに渡す
        listView.setAdapter(taskAdapter);

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        mRealm.close();
    }

//    private void addTaskForTest(){
//        Task task = new Task();
//        task.setTitle("work");
//        task.setContents("Write program and PUSH");
//        task.setDate(new Date());
//        task.setId(0);
//        mRealm.beginTransaction();
//        mRealm.copyToRealmOrUpdate(task);
//        mRealm.commitTransaction();
//    }

}
