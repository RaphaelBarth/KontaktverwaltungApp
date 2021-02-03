package de.pbma.moa.createroomdemo.activitys;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;

import com.google.zxing.WriterException;

import org.joda.time.DateTime;

import java.io.File;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import de.pbma.moa.createroomdemo.BuildConfig;
import de.pbma.moa.createroomdemo.PdfClass;
import de.pbma.moa.createroomdemo.QrCodeManger;
import de.pbma.moa.createroomdemo.R;
import de.pbma.moa.createroomdemo.database.Repository;
import de.pbma.moa.createroomdemo.database.RoomItem;
import de.pbma.moa.createroomdemo.service.MQTTService;


/**
 * Hier kann der Host die aktuellen Daten über den Raum sehen. Er kann den Raum schließen,
 * das TimeOut ändern und die Teilnehmer sehen.
 */
public class Activity_22_RoomHostDetail extends AppCompatActivity {
    final static String TAG = Activity_22_RoomHostDetail.class.getCanonicalName();
    final static String ID = "RoomID";
    long roomid;
    private TextView tvtimeout, tvstatus, tvroomname, tvStartTime, tvEndTime, tvLocation;
    private Button btnopen;
    private Button btntimeout;
    private RoomItem item;
    private Repository repo;
    private final ArrayList<RoomItem> toSend = new ArrayList<>();
    private TimeoutRefresherThread timeoutRefresherThread;
    private AtomicLong endtimeAtomic, startTimeAtomic;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreated_Teilnehmer_Uebersicht");
        this.setTitle("Details des erstellten Events");
        setContentView(R.layout.page_22_room_host_detail_activity);
        endtimeAtomic = new AtomicLong(0);
        startTimeAtomic = new AtomicLong(0);
        bindUI();

        //Holt die Daten aus der Bank
        repo = new Repository(this);
        roomid = getIntent().getExtras().getLong(ID, -1);
        if (roomid != -1) {
            LiveData<RoomItem> liveData = repo.getRoomByID(roomid);
            //observer auf raum hängen
            liveData.observe(this, roomItem -> {
                Activity_22_RoomHostDetail.this.item = roomItem;
                updateRoom(roomItem);
                //Speichern für Nebenläufigkeit
                //So profitieren alle von Livedata
                endtimeAtomic.set(roomItem.endTime);
                startTimeAtomic.set(roomItem.startTime);
                //der Timeoutrefresherthread wird nur gestartet wenn
                //Der Raum offen ist oder wenn er geröffnet werden soll.
                if (item.status == RoomItem.ROOMISOPEN) {
                    timeoutRefresherThread.initialStart();
                    btntimeout.setEnabled(true);
                    btnopen.setEnabled(true);
                } else if (item.status == RoomItem.ROOMWILLOPEN) {
                    timeoutRefresherThread.initialStart();
                    btntimeout.setEnabled(true);
                    btnopen.setEnabled(false);
                } else {
                    //Wenn der Raum nicht offen ist soll der Thread gestoppt
                    //werden. Aber nur wenn er läuft.
                    timeoutRefresherThread.stop();

                    //Tasten für Raum schließen disable
                    btnopen.setEnabled(false);
                    btntimeout.setEnabled(false);
                }
            });
        }
    }

    /**
     * MQTT Service wird gelöst und {@link TimeoutRefresherThread} wird gestoppt.
     */
    @Override
    protected void onPause() {
        super.onPause();
        timeoutRefresherThread.stop();
        unbindMQTTService();
    }

    /**
     * MQTT Service wird gebunden und {@link TimeoutRefresherThread} wird gestartet.
     */
    @Override
    protected void onResume() {
        super.onResume();
        timeoutRefresherThread = new TimeoutRefresherThread(this, tvtimeout,
                endtimeAtomic, startTimeAtomic);
        timeoutRefresherThread.initialStart();
        bindMQTTService();
    }

    /**
     * Bindet UI Elemente und setzt ocls
     */
    private void bindUI() {
        tvroomname = findViewById(R.id.tv_22_roomname_value);
        tvstatus = findViewById(R.id.tv_22_status_value);
        tvtimeout = findViewById(R.id.tv_22_status_timeout);
        tvStartTime = findViewById(R.id.tv_22_starttime);
        tvEndTime = findViewById(R.id.tv_22_endtime);
        tvLocation = findViewById(R.id.tv_22_location);
        btnopen = findViewById(R.id.btn_22_closeroom);
        btntimeout = findViewById(R.id.btn_22_changetimeout);
        Button btnpartic = findViewById(R.id.btn_22_partiicipantlist);
        btnpartic.setOnClickListener(this::onViewParticipants);
        btnopen.setOnClickListener(this::onCloseRoom);
        btntimeout.setOnClickListener(this::onChangeTimeout);

    }

    /**
     * Wird vom Observer gerufen wenn sich die Daten zum Raum geändert haben (z.B. Timeout).
     * Dann aktualisiert diese Funktion die UI Elemente.
     */
    @SuppressLint("SetTextI18n")
    private void updateRoom(RoomItem item) {
        if (item != null) {
            tvroomname.setText(item.roomName);
            if (item.status == RoomItem.ROOMISOPEN) {
                tvstatus.setText("schließt in");
            } else if (item.status == RoomItem.ROOMWILLOPEN) {
                tvstatus.setText("öffnet in");
            } else {
                tvstatus.setText("geschlossen");
            }
            DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.GERMAN);
            tvStartTime.setText("Von: " + df.format(item.startTime));
            tvEndTime.setText("Bis: " + df.format(item.endTime));
            tvLocation.setText(item.place + "\n" + item.address);
        }
    }

    /**
     * Wird gerufen wenn der Raum vom Host manuell geschlossen wird.
     */
    private void onCloseRoom(View view) {
        //Taste macht nichts mehr wenn der Raum geschlossen wurde
        timeoutRefresherThread.stop();
        //Trage aktuelle Zeit für die Endzeit ein
        item.endTime = System.currentTimeMillis();
        item.status = RoomItem.ROOMISCLOSE;
        repo.updateRoomItem(item);
        repo.kickOutParticipants(item, System.currentTimeMillis());
        if (mqttService == null)
            toSend.add(item);
        else
            mqttService.sendRoom(item, true);
    }

    /**
     * Wird gerufen wenn das Timeout geändert wird. Die Auswahl wird durch einen DatePickerDialog und TimePickerDialog
     * durchgeführt.
     */
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;

    private void onChangeTimeout(View view) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(item.endTime);

        timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
            Log.v(TAG, "ChangeEndTimeDateClicked " + hourOfDay + " " + minute);
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            if (calendar.getTimeInMillis() <= item.startTime) {
                Toast.makeText(Activity_22_RoomHostDetail.this, R.string.fehlerhafte_endzeit, Toast.LENGTH_LONG).show();
            } else {
                item.endTime = calendar.getTimeInMillis();
                repo.updateRoomItem(item);
                if (mqttService == null)
                    toSend.add(item);
                else
                    mqttService.sendRoom(item, true);
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

        datePickerDialog = new DatePickerDialog(this, (view12, year, monthOfYear, dayOfMonth) -> {
            Log.v(TAG, "ChangeEndTimeDateClicked " + dayOfMonth + " " + monthOfYear + " " +
                    year);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.MONTH, monthOfYear);
            //Call timepickerdialog
            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));


        //call datepickerdialog
        datePickerDialog.show();

    }


    /**
     * Startet die {@link Activity_23_HostViewParticipant} um die Teilnehmer eines Raumes zu sehen.
     */
    private void onViewParticipants(View view) {
        Intent intent = new Intent(Activity_22_RoomHostDetail.this,
                Activity_23_HostViewParticipant.class);
        intent.putExtra(Activity_23_HostViewParticipant.INTENT_ROOM_ID, item.id);
        startActivity(intent);
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_22_room_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Wird gerufen wenn ein Menüitem ausgewählt wurde. Wird "teilen" ausgewählt wird
     * {@link #shareRoom(RoomItem)} gestartet, wird "QR-Code" gewählt wird
     * {@link #callAlertDialog_QR()} ausgeführt, wird "RoomTag auswählt wird
     * {@link #callAlertDialog_URI()} aufgerufen.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuitem) {
        int menuitemId = menuitem.getItemId();
        if (menuitemId == R.id.menu_partic_share) {
            shareRoom(this.item);
            return true;
        } else if (menuitemId == R.id.menu_partic_qr) {
            callAlertDialog_QR();
            return true;
        } else if (menuitemId == R.id.menu_partic_uri) {
            callAlertDialog_URI();
            return true;
        } else {
            return super.onOptionsItemSelected(menuitem);
        }
    }

    private DisplayMetrics GetDisplaySize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    /**
     * Erstellt einen QR Code mit dem Inhalt msg
     *
     * @param msg String der in den QR Code geschrieben wird.
     */
    private Bitmap getQR(String msg, int hight, int width) {
        //Generate QR-code as bitmap
        Bitmap qrCode = null;
        QrCodeManger qrCodeManager = new QrCodeManger(this);
        try {
            qrCode = qrCodeManager.createQrCode(msg, width, hight);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return qrCode;
    }

    /**
     * Ein PDF mit den Raumeigenschaften wird erstellt und über einen Share Dialog geteilt.
     * Auch der QR Code steht in diesem PDF.
     */
    private void shareRoom(RoomItem item) {

        //generate PDF with qrCode an room infos -> saved in external file system
        PdfClass pdf = new PdfClass(Activity_22_RoomHostDetail.this);
        File file = pdf.createPdfRoomInfos(item, getQR(item.getRoomTag(),
                PdfClass.A4_WIDTH / 2, PdfClass.A4_HEIGHT / 2));

        Log.v(TAG, "showPDF(" + file.getName() + ")");
        if (!file.exists()) {
            Toast.makeText(this, "Something wrong \n " + "file: " + file.getPath(),
                    Toast.LENGTH_LONG).show();
            return;
        }
        //start share Intent
        try {
            Uri uri = FileProvider.getUriForFile(Activity_22_RoomHostDetail.this,
                    BuildConfig.APPLICATION_ID + ".provider", file);
            Intent intent = ShareCompat.IntentBuilder.from(Activity_22_RoomHostDetail.this)
                    .setType(URLConnection.guessContentTypeFromName(file.getName()))
                    .setStream(uri)
                    .setChooserTitle("Choose bar")
                    .createChooserIntent()
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(Activity_22_RoomHostDetail.this,
                    "Something wrong \n " + "file: " + file.getPath(), Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Öffnet einen AlertDialog auf dem der QR Code zum Einscannen steht.
     */
    public void callAlertDialog_QR() {
        LayoutInflater qrDialogInflater = LayoutInflater.from(this);
        View view = qrDialogInflater.inflate(R.layout.pop_up_22_qr, null);
        TextView tvQrUri = view.findViewById(R.id.tv_qr_show_uri);
        ImageView ivQr = view.findViewById(R.id.qr_code_show);
        new Thread(() -> {
            int size = (int) (GetDisplaySize().widthPixels * 0.5);
            ivQr.setImageBitmap(getQR(this.item.getRoomTag(), size, size));
            tvQrUri.setText(item.getRoomTag());
            Activity_22_RoomHostDetail.this.runOnUiThread(() -> new AlertDialog.Builder(this).setView(view).create().show());
        }).start();


    }

    /**
     * Öffnet einen AlertDialog auf dem der der RoomTag steht.
     */
    public void callAlertDialog_URI() {
        LayoutInflater uriDialogInflater = LayoutInflater.from(this);
        View view = uriDialogInflater.inflate(R.layout.pop_up_22_uri, null);

        TextView tvUri = view.findViewById(R.id.tv_show_uri);
        tvUri.setText(item.getRoomTag());

        AlertDialog alertDialogUri = new AlertDialog.Builder(this).setView(view).create();
        alertDialogUri.show();
    }

    //MQTT
    private boolean mqttServiceBound;
    private MQTTService mqttService;

    /**
     * Versucht den MQTT Service zu binden.
     */
    private void bindMQTTService() {
        Log.v(TAG, "bindMQTTService");
        Intent intent = new Intent(this, MQTTService.class);
        intent.setAction(MQTTService.ACTION_PRESS);
        mqttServiceBound = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        if (!mqttServiceBound) {
            Log.w(TAG, "could not try to bind service, will not be bound");
        }
    }

    /**
     * Versucht den MQTT Service zu lösen.
     */
    private void unbindMQTTService() {
        Log.v(TAG, "unbindMQTTService");
        if (mqttServiceBound) {
            mqttServiceBound = false;
            unbindService(serviceConnection);
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "onServiceConnected");
            mqttService = ((MQTTService.LocalBinder) service).getMQTTService();
            //Jeder Raum der sich geöffnet oder geschlossen hat soll auch so in die Datenbank
            //eingetragen werden
            for (RoomItem room : toSend) {
                boolean status = room.status == RoomItem.ROOMISCLOSE;
                mqttService.sendRoom(room, status);
            }
            toSend.clear();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // unintentionally disconnected
            Log.v(TAG, "onServiceDisconnected");
            unbindMQTTService(); // cleanup
        }
    };

}
