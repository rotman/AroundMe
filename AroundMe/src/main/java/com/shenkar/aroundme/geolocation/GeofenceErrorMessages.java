package com.shenkar.aroundme.geolocation;

import android.content.Context;
import com.google.android.gms.location.GeofenceStatusCodes;

/**
 * handle an error message
 */
public class GeofenceErrorMessages {

    public static String getErrorString(Context context, int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "geoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "too many results";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "too many pending intents";
            default:
                return "unknown geofence error";
        }
    }
}
