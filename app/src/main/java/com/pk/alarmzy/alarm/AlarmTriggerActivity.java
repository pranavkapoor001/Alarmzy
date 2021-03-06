package com.pk.alarmzy.alarm;

import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.ncorti.slidetoact.SlideToActView;
import com.pk.alarmzy.R;
import com.pk.alarmzy.alarm.db.AlarmEntity;
import com.pk.alarmzy.alarm.db.AlarmRepository;
import com.pk.alarmzy.alarm.helper.AlarmHelper;
import com.pk.alarmzy.alarm.helper.NotificationHelper;
import com.pk.alarmzy.alarm.services.AlarmService;
import com.pk.alarmzy.databinding.ActivityAlarmTriggerBinding;
import com.pk.alarmzy.misc.Constants;
import com.pk.alarmzy.misc.MyApplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmTriggerActivity extends AppCompatActivity {

    // UI Components
    private ActivityAlarmTriggerBinding binding;
    private TextView tvAlarmTime, tvAlarmTitle;

    // vars
    private static final String TAG = "AlarmTriggerActivity";
    private boolean isSnoozed = false;
    private Handler handler;
    private Runnable silenceRunnable;
    private AlarmEntity alarmEntity;
    private SharedPreferences sharedPref;
    private String actionBtnPref;


    //----------------------------- Lifecycle methods --------------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlarmTriggerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Wakeup screen
        turnOnScreen();

        // Register Power button (screen off) intent receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(PowerBtnReceiver, filter);

        // Get Settings shared preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Get views
        tvAlarmTime = binding.triggerAlarmTime;
        tvAlarmTitle = binding.triggerAlarmTitle;
        SlideToActView btnDismissAlarm = binding.btnDismissAlarm;
        SlideToActView btnSnoozeAlarm = binding.btnSnoozeAlarm;

        Intent intent = getIntent();

        /* This can produce npe
         * Check if key exists then fetch value
         */
        int alarmId = -1;
        if (intent.hasExtra("alarmIdKey"))
            alarmId = intent.getIntExtra("alarmIdKey", -1);

        Log.i(TAG, "onCreate: Got alarmIdKey: " + alarmId);

        // Get alarm type and build data for UI
        buildDisplayInfo(alarmId);

        // SlideToActView Listeners

        // Dismiss Alarm
        btnDismissAlarm.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NonNull SlideToActView slideToActView) {
                // Stop service and finish this activity
                stopAlarmService();
            }
        });

        // Snooze Alarm
        btnSnoozeAlarm.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NonNull SlideToActView slideToActView) {
                snoozeAlarm();
            }
        });

        // Check silenceTimeout
        silenceTimeout(alarmId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;

        unregisterReceiver(PowerBtnReceiver);
    }

    //----------------------------- Build UI Data ------------------------------------------------//

    /* This method gets the alarm Type (Parent, child, snooze)
     * and calls displayInfo() with alarmEntity object
     */
    private void buildDisplayInfo(int alarmId) {
        Application app = AlarmTriggerActivity.this.getApplication();
        final AlarmRepository ar = new AlarmRepository(app);

        // Temp var for inner class
        final int finalAlarmId = alarmId;
        MyApplication.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                alarmEntity = ar.getAlarm(finalAlarmId);
                /* If alarmId is not matched in db and daysOfRepeat is null
                 * Then it is a child alarm
                 */
                try {
                    Boolean[] daysOfRepeat = alarmEntity.getDaysOfRepeatArr();
                    // Disable toggle if alarm is not recurring type
                    if (!daysOfRepeat[Constants.IsRECURRING])
                        ar.updateAlarmStatus(finalAlarmId, false);

                    displayInfo(alarmEntity);

                } catch (NullPointerException e) {
                    Log.e(TAG, "run: Array is null, This is a child alarm");

                    try {
                        // Get today's day of week
                        Calendar todayCal = Calendar.getInstance();
                        int dayToday = todayCal.get(Calendar.DAY_OF_WEEK);


                        /* Since child alarms are scheduled
                         * with ID as parent id + dayToday(Calendar.DAY_OF_WEEK)
                         * sub dayToday from received id to get parent alarm id
                         * which is stored in db
                         */
                        int parentAlarmId = finalAlarmId - dayToday;

                        AlarmEntity parentEntity = ar.getAlarm(parentAlarmId);

                        /* This is a repeating alarm
                         * Now set this alarm for next week
                         */
                        AlarmHelper ah = new AlarmHelper();
                        ah.repeatingAlarm(parentEntity, dayToday);

                        displayInfo(parentEntity);
                    } catch (NullPointerException e1) {
                        Log.e(TAG, "run: This is a snoozed alarm");

                        displaySnoozedInfo();
                        isSnoozed = true;

                        // Cancel notification
                        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotifyManager.cancelAll();
                    }
                }
            }
        });
    }


    //----------------------------- Set UI Data ------------------------------------------------//

    // Display alarmTime and alarmTitle
    public void displayInfo(final AlarmEntity alarmEntity) {
        // Fetch from db (running on bg thread)
        final long alarmTimeInMillis = alarmEntity.getAlarmTime();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Get alarm time
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                        Locale.getDefault());
                String formattedTime = sdf.format(alarmTimeInMillis);
                tvAlarmTime.setText(formattedTime);

                /* Get alarm title
                 * If alarmTitle is "Alarm Title" (User didn't set any custom title)
                 * then show title as "Alarm"
                 */
                if (alarmEntity.getAlarmTitle().trim().equals(getString(R.string.alarm_title)))
                    tvAlarmTitle.setText(R.string.alarm);
                else
                    tvAlarmTitle.setText(alarmEntity.getAlarmTitle());
            }
        });
    }

    // Display alarm title and time of snoozed alarm
    private void displaySnoozedInfo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Simply show current time and "Snoozed Alarm" as title

                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                        Locale.getDefault());
                String formattedTime = sdf.format(System.currentTimeMillis());
                tvAlarmTime.setText(formattedTime);

                tvAlarmTitle.setText(R.string.snoozed_alarm);
            }
        });
    }


    //------------------------------- Get Silence Timeout ----------------------------------------//

    /* Check if silence timeout is greater than 0
     * and deliver missed alarm notification if timeout is exceeded
     */
    public void silenceTimeout(final int alarmId) {
        final String KEY_SILENCE_TIMEOUT = "silenceTimeout";

        // Get silence timeout
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String silenceTimeStr = sharedPref.getString(KEY_SILENCE_TIMEOUT, "0");

        /* Set default and add null check
         * to avoid warning and npe later
         */
        int silenceTimeoutInt = 0;
        if (silenceTimeStr != null)
            silenceTimeoutInt = Integer.parseInt(silenceTimeStr);

        /* If silenceTimeout is set to Never(0)
         * Return from here
         */
        if (silenceTimeoutInt == 0)
            return;

        /* If not dismissed under x minutes
         * Stop alarm
         * Post missed alarm notification
         */
        handler = new Handler(getMainLooper());
        silenceRunnable = new Runnable() {
            @Override
            public void run() {
                // Deliver notification using id
                NotificationHelper nh = new NotificationHelper(getApplicationContext(), alarmId);

                /* AlarmEntity is null for snoozed alarm
                 * Get actual alarm time by: CurrentTime - silenceTimeout
                 */
                if (isSnoozed) {
                    nh.deliverMissedNotification(
                            System.currentTimeMillis() - (Long.parseLong(silenceTimeStr) * 60000));
                } else
                    nh.deliverMissedNotification(alarmEntity.getAlarmTime());

                stopAlarmService();
            }
        };
        handler.postDelayed(silenceRunnable, silenceTimeoutInt * 60000); // x Minutes * millis
    }


    //--------------------------------- Misc Methods ---------------------------------------------//

    // Stop service and finish activity
    public void stopAlarmService() {
        Intent intent = new Intent(AlarmTriggerActivity.this, AlarmService.class);
        stopService(intent);

        /* Runnable has not yet executed
         * and alarm has been dismissed by user
         * no need to post work now
         */
        if (handler != null && silenceRunnable != null)
            handler.removeCallbacks(silenceRunnable);
        finish();
    }

    public void snoozeAlarm() {
        // Create new snooze alarm
        AlarmHelper ah = new AlarmHelper();
        ah.snoozeAlarm();

        stopAlarmService();
    }

    private void turnOnScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true);
            setShowWhenLocked(true);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        } else {
            final Window win = getWindow();
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
    }

    //-------------------------------- ActionBtn Methods -----------------------------------------//

    /* This method:
     * Receives Volume button press
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            // Get volume key pref
            actionBtnPref = sharedPref.getString("volume_btn_action", Constants.ACTION_DO_NOTHING);
            if (actionBtnPref != null)
                actionBtnHandler(actionBtnPref);
        }
        return super.onKeyDown(keyCode, event);
    }

    /* This method:
     * Receives Power button press (Screen off event)
     */
    private final BroadcastReceiver PowerBtnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

                    // Get power key pref
                    actionBtnPref = sharedPref.getString("power_btn_action", Constants.ACTION_DO_NOTHING);
                    if (actionBtnPref != null)
                        actionBtnHandler(actionBtnPref);
                }
            }
        }
    };

    private void actionBtnHandler(String action) {
        switch (action) {
            case Constants.ACTION_MUTE:
                // Mute is handled by MuteActionReceiver in AlarmService
                sendBroadcast(new Intent().setAction(Constants.ACTION_MUTE));
                break;
            case Constants.ACTION_DISMISS:
                stopAlarmService();
                break;
            case Constants.ACTION_SNOOZE:
                snoozeAlarm();
                break;
        }
    }
}
