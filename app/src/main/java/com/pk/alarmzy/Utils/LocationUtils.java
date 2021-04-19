package com.pk.alarmzy.Utils;

import android.app.Activity;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.pk.alarmzy.R;
import com.pk.alarmzy.Utils.Constants.PermissionRequestCodes;
import com.pk.alarmzy.Utils.Constants.PreferenceKeys;
import com.pk.alarmzy.misc.MyApplication;

public class LocationUtils {

    private static final String TAG = "LocationUtils";
    private final Activity mActivity;
    private final FusedLocationProviderClient fusedLocationClient;
    private final PermissionUtils permissionUtils;

    public LocationUtils(Activity activity) {
        mActivity = activity;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MyApplication.getContext());
        permissionUtils = new PermissionUtils(mActivity);
    }


    /*------------------------------ Public Location Methods -------------------------------------*/

    /**
     * Gets location using GMS location service
     * Location access and GMS location accuracy permission is handled from here
     * No permission check is required prior to calling this method
     * <p>
     * No other method can be called from outside this class to get location
     */
    public void getLocation() {
        /* Check if location permission is granted
         * Note: API 22 and lower automatically grant location permission
         */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (permissionUtils.isLocationPermissionGranted())
                getLocationFromGMS();
            else
                permissionUtils.requestLocationPermission();
        } else
            getLocationFromGMS();
    }

    /**
     * Checks if location(latitude) is already present in Shared Preferences
     * If true, locating process is stopped
     *
     * @return true if location is not null
     */
    public boolean isLocationSaved() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        final String savedLocation = sharedPref.getString(PreferenceKeys.KEY_LAT, "null");
        if (savedLocation != null && !savedLocation.equals("null")) {
            Log.v(TAG, "location already saved");
            return true;
        } else {
            Log.w(TAG, "location NOT saved, will fetch now");
            return false;
        }
    }

    /*----------------------------- Hidden Location Methods --------------------------------------*/

    /**
     * Gets cached location using GMS FusedLocationProviderClient
     * and stores it via {@link #saveLocation(Location)}
     *
     * <p> If cached location is not available,
     * calls {@link #getCurrentLocation()} which gets current location
     */
    private void getCachedLocation() {
        Snackbar.make(mActivity.findViewById(android.R.id.content),
                mActivity.getString(R.string.fetching_weather_at_your_location),
                Snackbar.LENGTH_INDEFINITE).show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(mActivity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    Log.v(TAG, " Nothing cached, getting current location...");
                    getCurrentLocation();
                } else {
                    Log.v(TAG, "Got cached location: " + location.getLongitude());
                    saveLocation(location);
                }

            }
        });
    }

    /**
     * Gets current location using GMS FusedLocationProviderClient
     * and stores it via {@link #saveLocation(Location)}
     */
    private void getCurrentLocation() {
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_LOW_POWER, null).addOnSuccessListener(mActivity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    Snackbar.make(mActivity.findViewById(android.R.id.content),
                            mActivity.getString(R.string.failed_to_get_location),
                            Snackbar.LENGTH_LONG).show();
                } else {
                    Log.v(TAG, "Got Current location: " + location.getLongitude());
                    saveLocation(location);
                }
            }

        });
    }

    /*-------------------------------  Location data Methods -------------------------------------*/

    /**
     * Stores location latitude and longitude in Shared Preferences
     *
     * @param location Location object with valid latitude, longitude and other bearings
     */
    private void saveLocation(Location location) {


        // Save location
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        sharedPref.edit().putString(PreferenceKeys.KEY_LAT, String.valueOf(location.getLatitude()))
                .putString(PreferenceKeys.KEY_LONGI, String.valueOf(location.getLongitude())).apply();

        Snackbar.make(mActivity.findViewById(android.R.id.content),
                mActivity.getString(R.string.location_updated),
                Snackbar.LENGTH_SHORT).show();
    }


    /*------------------------------ GMS location methods ----------------------------------------*/

    /**
     * Sets LocationRequest parameters and passes to {@link #buildLocationRequest(LocationRequest)}
     */
    private void getLocationFromGMS() {
        LocationRequest locationRequest = LocationRequest.create();
        // Define default interval as we only need location once
        locationRequest.setInterval(10000);

        /* We need city level accuracy, i.e: coarse accuracy
         * PRIORITY_LOW_POWER gives accuracy of 10Km
         */
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        buildLocationRequest(locationRequest);
    }

    /**
     * Checks if GMS location accuracy is enabled
     * If yes, finally gets location using {@link #getCachedLocation()}
     * otherwise calls {@link #showGMSDialog(Exception)} to launch GMS enable dialog
     *
     * @param locationRequest LocationRequest object with set parameters for location quality
     */
    public void buildLocationRequest(LocationRequest locationRequest) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(MyApplication.getContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(mActivity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // finally get location
                getCachedLocation();
            }
        });

        task.addOnFailureListener(mActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Called when google location accuracy is disabled
                    Snackbar.make(mActivity.findViewById(android.R.id.content),
                            mActivity.getString(R.string.please_enable_Google_location_services),
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(mActivity.getString(R.string.enable), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showGMSDialog(e);
                                }
                            }).show();
                }
            }
        });

    }

    /**
     * Shows GMS location accuracy enable dialog
     *
     * @param e ResolvableApiException
     */
    private void showGMSDialog(Exception e) {
        try {
            /* Show the dialog to enable it by calling startResolutionForResult(),
             * and check the result in onActivityResult() in MainActivity
             */
            ResolvableApiException resolvable = (ResolvableApiException) e;
            resolvable.startResolutionForResult(mActivity,
                    PermissionRequestCodes.GMS_REQUEST_CODE);
        } catch (IntentSender.SendIntentException ignored) {
        }
    }
}
