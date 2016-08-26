package com.mapzen.tangram;

/**
 * Class created by magma3 on 8/26/2016.
 */
public class TPLHttpRequestManager {

    private static final int MAP_BUILDINGS_MIN_ZOOM = 14;
    private static final int MAP_BUILDINGS_MAX_ZOOM = 18;
    private static final int MAP_POIS_MIN_ZOOM = 10;
    private static final int MAP_POIS_MAX_ZOOM = 19;

    /**
     * Method returns that client should request against the url or not.
     * Reason is to reduce request load on server by seeking the requestType and its limitations
     * based on zoom levels.
     */
    public static boolean shouldRequestToServer(String shortUrl) {
        RequestType requestType = getRequestType(shortUrl);
        if (requestType == RequestType.NONE)
            return false;

        if(requestType == RequestType.MAP_SATELLITE || requestType == RequestType.MAP_COMPOSITE)
            return true;

        int zoom = getZoomLevelForRequest(requestType, shortUrl);
        return (requestType == RequestType.MAP_BUILDINGS && zoom >= MAP_BUILDINGS_MIN_ZOOM
                && zoom <= MAP_BUILDINGS_MAX_ZOOM)  // For Buildings
                || (requestType == RequestType.MAP_POIS && zoom >= MAP_POIS_MIN_ZOOM
                && zoom <= MAP_POIS_MAX_ZOOM); //For POIs
    }

    public static final String URL_IDENTFR_COMPOSITE = "composite/";
    public static final String URL_IDENTFR_MAPBOX_SATELLITE = "mapbox.satellite/";

    private static RequestType getRequestType(String shortUrl) {
        if(shortUrl.contains(URL_IDENTFR_MAPBOX_SATELLITE))
            return RequestType.MAP_SATELLITE;

        if (shortUrl.contains(URL_IDENTFR_COMPOSITE))
            return RequestType.MAP_COMPOSITE;
        else if (shortUrl.contains(URL_IDENTFR_BUILDINGS))
            return RequestType.MAP_BUILDINGS;
        else if (shortUrl.contains(URL_IDENTFR_POIS))
            return RequestType.MAP_POIS;
        else
            return RequestType.NONE;
    }

    public static final String URL_IDENTFR_BUILDINGS = "buildings/";
    public static final String URL_IDENTFR_POIS = "pois/";

    private static int getZoomLevelForRequest(RequestType requestType, String shortUrl) {
        int intZoom = -1;

        if (requestType == RequestType.NONE || requestType == RequestType.MAP_SATELLITE
                || requestType == RequestType.MAP_COMPOSITE) {
            return intZoom;
        }

        String strIdentifier = null;
        if (requestType == RequestType.MAP_BUILDINGS)
            strIdentifier = URL_IDENTFR_BUILDINGS;
        else if (requestType == RequestType.MAP_POIS)
            strIdentifier = URL_IDENTFR_POIS;

        if (strIdentifier != null) {
            int lastIndex = shortUrl.lastIndexOf(strIdentifier) + strIdentifier.length();
            String strZoom = shortUrl.substring(lastIndex, lastIndex + 2);

            if(strZoom.contains("/")) {
                intZoom = Integer.valueOf(strZoom.replace("/", ""));
                return intZoom;
            } else {
                return Integer.valueOf(strZoom);
            }
        } else {
            return intZoom;
        }
    }

    public enum RequestType {
        NONE, MAP_COMPOSITE, MAP_BUILDINGS, MAP_POIS, MAP_SATELLITE
    }
}
