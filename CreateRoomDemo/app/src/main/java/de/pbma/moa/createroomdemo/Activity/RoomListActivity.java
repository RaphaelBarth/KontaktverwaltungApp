package de.pbma.moa.createroomdemo.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

import de.pbma.moa.createroomdemo.R;
import de.pbma.moa.createroomdemo.RoomListAdapter;
import de.pbma.moa.createroomdemo.RoomRoom.RoomItem;
import de.pbma.moa.createroomdemo.RoomRoom.RoomRepository;

public class RoomListActivity extends AppCompatActivity {
    final static String TAG = RoomListActivity.class.getCanonicalName();

    private ArrayList<RoomItem> roomList;
    private ListView lv;
    private RoomRepository roomRepo;
    private RoomListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "OnCreate");
        roomList = new ArrayList<>();
        setContentView(R.layout.roomlist_page);
        adapter = new RoomListAdapter(this, roomList);
        lv = findViewById(R.id.lv_highscore);
        lv.setAdapter(adapter);
        roomRepo = new RoomRepository(this);
        roomRepo.getDbAll().observe(this, observer);
    }
    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.room_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_newRoom:
                Log.v(TAG,"onOptionsItemSelected() create new Room");
               Intent intent = new Intent(this,CreateNewRoomActivity.class );
               startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    Observer<List<RoomItem>> observer = new Observer<List<RoomItem>>() {
        @Override
        public void onChanged(List<RoomItem> changedTodos) {
            roomList.clear();
            roomList.addAll(changedTodos);
            adapter.notifyDataSetChanged();
        }
    };
}