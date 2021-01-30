package com.pk.alarmzy;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pk.alarmzy.alarm.db.AlarmEntity;
import com.pk.alarmzy.alarm.db.AlarmViewModel;
import com.pk.alarmzy.alarm.helper.AlarmHelper;
import com.pk.alarmzy.alarm.helper.NotificationHelper;
import com.pk.alarmzy.alarm.recycler.AlarmRecViewAdapter;
import com.pk.alarmzy.databinding.ActivityMainBinding;
import com.pk.alarmzy.misc.Utils;
import com.pk.alarmzy.settings.SettingsActivity;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private ImageView noAlarmsImage;
    private TextView noAlarmsText;
    private ActivityMainBinding binding;

    // vars
    private RecyclerView mRecyclerView;
    private AlarmRecViewAdapter mAdapter;
    private AlarmHelper alarmHelper;


    //----------------------------- Lifecycle methods --------------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get views
        noAlarmsImage = binding.imageWhenEmpty;
        noAlarmsText = binding.textWhenEmpty;
        mRecyclerView = binding.itemAlarmRecyclerView;

        // Get helper
        alarmHelper = new AlarmHelper();

        // Initialize Recycler view and view model
        initRecyclerView();
        initViewModel();

        // Add on click listener to Fab
        FloatingActionButton fab = findViewById(R.id.fab_btn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickTime();
            }
        });

        // Create Notification Channel using NotificationHelper
        // (Pass -1 for alarmId, only creating Notification channel here)
        NotificationHelper notificationHelper = new NotificationHelper(MainActivity.this, -1);
        notificationHelper.createNotificationChannel();

        // Setup recycler view item touch helper
        itemTouchHelper();

        // Set default values on first launch
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }


    //----------------------------- Initialize methods -------------------------------------------//

    private void initRecyclerView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new AlarmRecViewAdapter();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initViewModel() {
        final AlarmViewModel alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        alarmViewModel.getAllAlarms().observe(this, new Observer<List<AlarmEntity>>() {
            @Override
            public void onChanged(List<AlarmEntity> alarmEntities) {
                mAdapter.submitList(alarmEntities);

                if (alarmEntities.size() == 0) {
                    noAlarmsImage.setVisibility(View.VISIBLE);
                    noAlarmsText.setVisibility(View.VISIBLE);
                } else {
                    noAlarmsImage.setVisibility(View.GONE);
                    noAlarmsText.setVisibility(View.GONE);
                }
            }
        });
    }


    //------------------------------- Misc methods -----------------------------------------------//

    private void itemTouchHelper() {

        // Helper method for Drag to del room item
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // This also cancels the alarm
                int adapterPos = viewHolder.getAdapterPosition();
                AlarmEntity currentEntity = mAdapter.getAlarmRecView(adapterPos);
                alarmHelper.cancelAlarm(currentEntity, true, true, -1);
                Snackbar.make(viewHolder.itemView, "Alarm Removed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        helper.attachToRecyclerView(mRecyclerView);
    }

    // Show dialog and set Time: Store selected time in Calendar object
    private void pickTime() {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                c.set(Calendar.SECOND, 0);

                alarmHelper.createAlarm(c);

                Snackbar.make(findViewById(android.R.id.content), "Alarm Set for "
                        + Utils.getFormattedNextAlarmTime(c.getTimeInMillis()), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        };
        Calendar currentTime = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                timeSetListener,
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }


    //----------------------------- MenuOptions methods ------------------------------------------//

    // Inflate menu layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}