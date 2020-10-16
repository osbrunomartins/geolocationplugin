package com.outsystems.geolocationplugin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class GeolocationPlugin extends CordovaPlugin implements LocationListener {

    private static final String ACTION_GET_LOCATION = "Get";
    private static final String ACTION_START_CAPTURE = "Start";
    private static final String ACTION_STOP_CAPTURE = "Stop";
    private static final String ARGS_WAIT_BETWEEN = "wait_between";
    private static final int ARGS_WAIT_BETWEEN_VALUE = 1;
    private static final int ARGS_MIN_DISTANCE_VALUE = 3;

    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_LONGITUDE = "longitude";
    private static final String JSON_ALTITUDE = "altitude";
    private static final String JSON_STATUS = "status";
    private static final String JSON_STATUS_REQUEST = "request";
    private static final String JSON_STATUS_CAPTURING = "capturing";
    private static final String JSON_STATUS_STOPED_CAPTURING = "stoped";
    private static final String JSON_STATUS_WAITING = "waiting";

    private PluginMode mode = new NotCapturing();
    private int waitBetween = 1000;
    private int minDistance = 100;

    private static final String [] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };

    private LocationManager locationManager;
    private Activity cordovaActivity;
    private CallbackContext callbackContext;

    private boolean isCapturing = false;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        cordovaActivity = this.cordova.getActivity();
        this.callbackContext = callbackContext;
        locationManager = (LocationManager) cordovaActivity.getSystemService(cordovaActivity.LOCATION_SERVICE);

        if (action.equals(ACTION_GET_LOCATION)) {
            this.getLocation();
            return true;
        } else if (action.equals(ACTION_START_CAPTURE)) {
            if(isCapturing){
                Toast.makeText(cordovaActivity, "Capture already in progress...", Toast.LENGTH_LONG).show();
            }else {
                try {
                    waitBetween = Integer.parseInt(args.getString(ARGS_WAIT_BETWEEN_VALUE));
                    minDistance = Integer.parseInt(args.getString(ARGS_MIN_DISTANCE_VALUE));
                } catch (NumberFormatException e) {
                    callbackContext.error("Invalid arguments. Please use integers for the arguments 'wait_between' and 'min_distance'.");
                }
                this.startCapture(waitBetween, minDistance);
            }
            return true;
        } else if (action.equals(ACTION_STOP_CAPTURE)) {
            String message = args.getString(0);
            this.stopCapture(message, callbackContext);
            return true;
        }
        return false;
    }

    private void getLocation() {
        int permission = ActivityCompat.checkSelfPermission(cordovaActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED){
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,this, null);
        }else{
            //ActivityCompat.requestPermissions(cordovaActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            cordova.requestPermission(this, 0, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void startCapture(int waitBetween, int minDistance) {
        int permission = ActivityCompat.checkSelfPermission(cordovaActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED){
            mode = new Capturing();
            isCapturing = true;

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, waitBetween, minDistance, this);

            JSONObject json = getJsonObject(JSON_STATUS_WAITING);
            PluginResult result =  new PluginResult(PluginResult.Status.OK, json.toString());
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }else{
            //ActivityCompat.requestPermissions(cordovaActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            cordova.requestPermission(this, 0, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void stopCapture(String message, CallbackContext callbackContext) {
        if(!isCapturing){
            Toast.makeText(cordovaActivity, "Capture is already stoped.", Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.removeUpdates(this);
        isCapturing = false;
        JSONObject json = getJsonObject(JSON_STATUS_STOPED_CAPTURING);
        PluginResult result =  new PluginResult(PluginResult.Status.OK, json.toString());
        result.setKeepCallback(false);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void onLocationChanged(Location location) {
        mode.processLocationUpdate(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.cordova.getActivity());
        builder.setMessage( "Location is turned off. Do you want to enable it in settings?" );
        builder.setPositiveButton( "Yes" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                cordovaActivity.startActivityForResult(intent, 1);
            }
        });
        builder.setNegativeButton( "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(cordovaActivity, "You need to turn on location in order to use this functionality.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        super.onRequestPermissionResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            mode.processPermissionResult();
        }else{
            callbackContext.error("Permission Denied.");
        }
    }

    private JSONObject getJsonObject(String status){
        try {
            JSONObject json = new JSONObject();
            json.put(JSON_STATUS, status);
            return json;
        }catch(JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getJsonObject(String status, double latitude, double longitude, double altitude){
        try {
            JSONObject json = new JSONObject();
            json.put(JSON_STATUS, status);
            json.put(JSON_LATITUDE, latitude + "");
            json.put(JSON_LONGITUDE, longitude + "");
            json.put(JSON_ALTITUDE, altitude + "");
            return json;
        }catch(JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    private abstract class PluginMode{
         void processLocationUpdate(Location location){
             double latitude = location.getLatitude();
             double longitude = location.getLongitude();
             double altitude = location.getAltitude();
             JSONObject json = getJsonObject(getMode(),latitude,longitude,altitude);
             PluginResult result =  new PluginResult(PluginResult.Status.OK, json.toString());
             result.setKeepCallback( keepCallBack() );
             callbackContext.sendPluginResult(result);
        }

        abstract void processPermissionResult();

        abstract String getMode();

        abstract boolean keepCallBack();
    }

    private class NotCapturing extends PluginMode{
        @Override
        void processPermissionResult() {
            getLocation();
        }

        @Override
        String getMode() {
            return JSON_STATUS_REQUEST;
        }

        @Override
        boolean keepCallBack() {
            return false;
        }
    }

    private class Capturing extends PluginMode{
        @Override
        void processPermissionResult() {
            startCapture(waitBetween, minDistance);
        }

        @Override
        String getMode() {
            return JSON_STATUS_CAPTURING;
        }

        @Override
        boolean keepCallBack() {
            return true;
        }
    }

}
