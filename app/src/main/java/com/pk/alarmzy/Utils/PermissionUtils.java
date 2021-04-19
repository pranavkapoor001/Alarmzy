package com.pk.alarmzy.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.pk.alarmzy.R;
import com.pk.alarmzy.Utils.Constants.PermissionRequestCodes;

public class PermissionUtils {

    private static final String TAG = "PermissionUtils";
    private static final String LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final Activity mActivity;

    public PermissionUtils(Activity activity) {
        mActivity = activity;
    }

    /**
     * Checks if ACCESS_FINE_LOCATION is granted
     *
     * @return true if granted
     */
    public boolean isLocationPermissionGranted() {
        // Permission is granted, return success
        return ContextCompat.checkSelfPermission(mActivity, LOCATION_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Decides whether permission should be directly requested
     * or using informational UI, snack bar here
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestLocationPermission() {
        // Permission is denied, show permission rationale snack bar
        if (mActivity.shouldShowRequestPermissionRationale(LOCATION_PERMISSION)) {
            Log.v("PermissionUtils", "Show rationale");
            showPermissionRationale();
        } else {
            Log.v("PermissionUtils", "request directly");
            // Directly request permission
            mActivity.requestPermissions(
                    new String[]{LOCATION_PERMISSION}, PermissionRequestCodes.LOCATION_REQUEST_CODE);
        }
    }

    /**
     * Shows Snack Bar to request ACCESS_FINE_LOCATION which opens system's settings app
     */
    public void showPermissionRationale() {
        Snackbar.make(mActivity.findViewById(android.R.id.content),
                mActivity.getString(R.string.location_access_required_for_weather),
                Snackbar.LENGTH_INDEFINITE).setAction(mActivity.getString(R.string.allow), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.startActivityForResult(
                        new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:com.pk.alarmzy")),
                        PermissionRequestCodes.LOCATION_REQUEST_SYSTEM_SETTINGS_CODE);
            }
        }).show();
    }

    /**
     * Used when onActivityResult() is called by MainActivity or SettingsActivity
     * Handles system app settings page or GMS location activity result
     *
     * @param requestCode unique integer code used with startActivityForResult(),
     *                    received back in onActivityResult()
     * @param resultCode  failure or success code received in onActivityResult()
     */
    public void handleOnActivityResult(int requestCode, int resultCode) {
        LocationUtils locationUtils = new LocationUtils(mActivity);
        switch (requestCode) {
            case PermissionRequestCodes.LOCATION_REQUEST_SYSTEM_SETTINGS_CODE:
                locationUtils.getLocation();
                break;

            case PermissionRequestCodes.GMS_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK)
                    Log.v(TAG, "GMS location enabled from dialog");
                else
                    Log.w(TAG, "GMS location NOT enabled from dialog, retry");

                // Call getLocation() anyway, since everything is handled there
                locationUtils.getLocation();
                break;
        }
    }
}
