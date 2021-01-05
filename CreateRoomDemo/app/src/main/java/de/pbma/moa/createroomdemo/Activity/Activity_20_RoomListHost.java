package de.pbma.moa.createroomdemo.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

import de.pbma.moa.createroomdemo.ListAdapter_20_HostRoom;
import de.pbma.moa.createroomdemo.Preferences.MySelf;
import de.pbma.moa.createroomdemo.R;
import de.pbma.moa.createroomdemo.RoomRoom.RoomItem;
import de.pbma.moa.createroomdemo.RoomRoom.Repository;

public class Activity_20_RoomListHost extends AppCompatActivity {
    final static String TAG = Activity_20_RoomListHost.class.getCanonicalName();
    private ArrayList<RoomItem> roomList;
    private ListView lv;
    private Repository roomRepo;
    private ListAdapter_20_HostRoom adapter;
    Observer<List<RoomItem>> observer = new Observer<List<RoomItem>>() {
        @Override
        public void onChanged(List<RoomItem> changedTodos) {
            roomList.clear();
            roomList.addAll(changedTodos);
            adapter.notifyDataSetChanged();
        }
    };
    private AdapterView.OnItemClickListener oicl = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Long roomid = (Long) view.getTag();
            Intent intent = new Intent(Activity_20_RoomListHost.this,
                    Activity_22_RoomHostDetail.class);
            intent.putExtra(Activity_22_RoomHostDetail.ID, roomid);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "OnCreate");
        roomList = new ArrayList<>();
        setContentView(R.layout.page_20_roomlist);
        adapter = new ListAdapter_20_HostRoom(this, roomList);
        lv = findViewById(R.id.lv_20_room);
        lv.setAdapter(adapter);
        roomRepo = new Repository(this);
        MySelf me = new MySelf(this);
        roomRepo.getDbAllFromMeAsHost(
                me.getFirstName(),
                me.getName(),
                me.getEmail(),
                me.getPhone()).observe(this, observer);
        lv.setOnItemClickListener(oicl); //Erweiterung um einen onClickedListener
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_21_create_room, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_newRoom:
                Log.v(TAG, "onOptionsItemSelected() create new Room");
                Intent intent = new Intent(this, Activity_21_CreateNewRoom.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
