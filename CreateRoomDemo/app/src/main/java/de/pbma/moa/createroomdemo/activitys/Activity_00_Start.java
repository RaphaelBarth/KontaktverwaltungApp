package de.pbma.moa.createroomdemo.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import java.util.List;

import de.pbma.moa.createroomdemo.database.Repository;
import de.pbma.moa.createroomdemo.database.RoomItem;
import de.pbma.moa.createroomdemo.preferences.MySelf;
import de.pbma.moa.createroomdemo.preferences.PreferenceActivity;
import de.pbma.moa.createroomdemo.R;
import de.pbma.moa.createroomdemo.service.RoomLivecycleService;

public class Activity_00_Start extends AppCompatActivity {
    final static String TAG = Activity_00_Start.class.getCanonicalName();
    private Button btnHost, btnParticipant;

    //Todo: Service für TimeOutRunoutCapture

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_00_start);

        btnHost = findViewById(R.id.btn_00_start_host);
        btnParticipant = findViewById(R.id.btn_00_start_participant);

        btnHost.setOnClickListener(Activity_00_Start.this::iAmHost);
        btnParticipant.setOnClickListener(Activity_00_Start.this::iAmParticipant);

        Intent intent = new Intent(this, RoomLivecycleService.class);
        startService(intent);

        //remove DB entries older two weeks
        deleteOldEntries();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, RoomLivecycleService.class);
        stopService(intent);
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_00_start, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_start_pref:
                Log.v(TAG, "onOptionsItemSelected() Settings");
                intent = new Intent(Activity_00_Start.this, PreferenceActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_start_history:
                Log.v(TAG, "onOptionsItemSelected() History");
                if (!checkMySelfe())
                    return true;
                intent = new Intent(Activity_00_Start.this, Activity_10_RoomListVisited.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void iAmHost(View view) {
        Log.v(TAG, "iAmHost() clicked");
        if (!checkMySelfe())
            return;
        Intent intent = new Intent(Activity_00_Start.this, Activity_20_RoomListHost.class);
        startActivity(intent);
    }

    private void iAmParticipant(View view) {
        Log.v(TAG, "iAmParticipant() clicked");
        if (!checkMySelfe())
            return;
        Intent intent = new Intent(Activity_00_Start.this, Activity_11_EnterViaQrNfc.class);
        startActivity(intent);
    }

    private boolean checkMySelfe(){
        Log.v(TAG,"Check mySelfe()");
        MySelf me = new MySelf(Activity_00_Start.this);
        if (!me.isValide()) {
            Toast.makeText(this, "Eigenangaben sind nicht vollständig", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Activity_00_Start.this, PreferenceActivity.class);
            startActivity(intent);
            Log.v(TAG,"Check mySelfe(): false");
            return false;
        }
        Log.v(TAG,"Check mySelfe(): true");
        return true;
    }

    private void deleteOldEntries(){
        Repository repository = new Repository(this);
        long currentTime = System.currentTimeMillis();
        repository.getAllRoomsOlderTwoWeeks(currentTime).observe(Activity_00_Start.this, new Observer<List<RoomItem>>() {
            @Override
            public void onChanged(List<RoomItem> roomItems) {
                for (RoomItem item : roomItems){
                    repository.deleteParticipantsOfRoom(item.id);
                }
            repository.deleteRoomsOlderTwoWeeks(currentTime);
            }
        });

    }

}