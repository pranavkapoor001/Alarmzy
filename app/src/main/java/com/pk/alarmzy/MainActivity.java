package com.pk.alarmzy;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pk.alarmzy.Utils.Constants.PermissionRequestCodes;
import com.pk.alarmzy.Utils.Constants.PreferenceKeys;
import com.pk.alarmzy.Utils.LocationUtils;
import com.pk.alarmzy.Utils.PermissionUtils;
import com.pk.alarmzy.Utils.TimeFormatUtils;
import com.pk.alarmzy.alarm.db.AlarmEntity;
import com.pk.alarmzy.alarm.db.AlarmViewModel;
import com.pk.alarmzy.alarm.helper.AlarmHelper;
import com.pk.alarmzy.alarm.helper.NotificationHelper;
import com.pk.alarmzy.alarm.recycler.AlarmRecViewAdapter;
import com.pk.alarmzy.databinding.ActivityMainBinding;
import com.pk.alarmzy.settings.SettingsActivity;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private ImageView noAlarmsImage;
    private TextView noAlarmsText;

    // vars
    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private AlarmRecViewAdapter mAdapter;
    private AlarmHelper alarmHelper;
    private LocationUtils locationUtils;
    private PermissionUtils permissionUtils;


    //----------------------------- Lifecycle methods --------------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
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

        // Check & request location permission if weather is enabled
        checkLocationPermission();
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
                Snackbar.make(viewHolder.itemView, getString(R.string.alarm_removed), Snackbar.LENGTH_LONG)
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

                Snackbar.make(findViewById(android.R.id.content), getString(R.string.alarm_set_for) + " "
                        + TimeFormatUtils.getFormattedNextAlarmTime(c.getTimeInMillis()), Snackbar.LENGTH_LONG)
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

    private void checkLocationPermission() {
        // Check if weather info is enabled by user, return if not
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean enableWeather = sharedPref.
                getBoolean(PreferenceKeys.KEY_WEATHER_ENABLED, true);
        if (!enableWeather)
            return;

        // Initialize helpers
        locationUtils = new LocationUtils(this);
        permissionUtils = new PermissionUtils(this);

        // No need to update location
        if (locationUtils.isLocationSaved())
            return;

        // Start permission check process then get location
        locationUtils.getLocation();

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


    /*---------------------- Permission & activity result callbacks ------------------------------*/

    // Called when user manually grants/denies permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Show SnackBar if permission is denied
        if (requestCode == PermissionRequestCodes.LOCATION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Log.w(TAG, "onRequestPermissionsResult: Denied");
            permissionUtils.showPermissionRationale();
        } else {
            Log.v(TAG, "onRequestPermissionsResult: Granted");
            locationUtils.getLocation();
        }
    }

    // Called after system app settings or GMS location dialog is launched to request permission
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionUtils.handleOnActivityResult(requestCode, resultCode);
    }
}
