package com.mapzen.tangram;

/**
 * Class created by magma3 on 8/26/2016.
 */
public class TPLHttpRequestManager {

    private static final int MAP_BUILDINGS_MIN_ZOOM = 14;
    private static final int MAP_BUILDINGS_MAX_ZOOM = 18;
    private static final int MAP_POIS_MIN_ZOOM = 10;
    private static final int MAP_POIS_MAX_ZOOM = 19;
    private static final int MAP_STREETVIEW_MIN_ZOOM = 10;
    private static final int MAP_STREETVIEW_MAX_ZOOM = 16;


    public static final String URL_IDENTFR_COMPOSITE = "composite/";
    public static final String URL_IDENTFR_MAPBOX_SATELLITE = "mapbox.satellite/";



    public static final String URL_IDENTFR_BUILDINGS = "buildings/";
    public static final String URL_IDENTFR_POIS = "pois/";
    public static final String URL_IDENTFR_STREETVIEW = "sv/";

    /**
     * Method returns that client should request against the url or not.
     * Reason is to reduce request load on server by seeking the requestType and its limitations
     * based on zoom levels.
     */
    public static boolean shouldRequestToServer(String shortUrl) {
        RequestType requestType = getRequestType(shortUrl);
        if (requestType == RequestType.NONE) {
            return false;
        } else if (requestType != RequestType.MAP_SATELLITE && requestType != RequestType.MAP_COMPOSITE) {
            int zoom = getZoomLevelForRequest(requestType, shortUrl);
            return requestType == RequestType.MAP_BUILDINGS && zoom >= MAP_BUILDINGS_MIN_ZOOM && zoom <= MAP_BUILDINGS_MAX_ZOOM || requestType == RequestType.MAP_POIS && zoom >= MAP_POIS_MIN_ZOOM && zoom <= MAP_POIS_MAX_ZOOM || requestType == RequestType.MAP_STREET_VIEW && zoom >= MAP_STREETVIEW_MIN_ZOOM && zoom <= MAP_STREETVIEW_MAX_ZOOM;
        } else {
            return true;
        }
    }

    public static enum RequestType {
        NONE, MAP_COMPOSITE, MAP_BUILDINGS, MAP_POIS, MAP_SATELLITE, MAP_STREET_VIEW;

        private RequestType() {
        }
    }

    private static RequestType getRequestType(String shortUrl) {
        return shortUrl.contains(URL_IDENTFR_MAPBOX_SATELLITE) ? RequestType.MAP_SATELLITE : (shortUrl.contains(URL_IDENTFR_COMPOSITE) ? RequestType.MAP_COMPOSITE : (shortUrl.contains("buildings/") ? RequestType.MAP_BUILDINGS : (shortUrl.contains("pois/") ? RequestType.MAP_POIS : (shortUrl.contains("sv/")) ? RequestType.MAP_STREET_VIEW : RequestType.NONE)));
    }

    private static int getZoomLevelForRequest(RequestType requestType, String shortUrl) {
        byte intZoom = -1;
        if (requestType != RequestType.NONE && requestType != RequestType.MAP_SATELLITE && requestType != RequestType.MAP_COMPOSITE) {
            String strIdentifier = null;
            if (requestType == RequestType.MAP_BUILDINGS) {
                strIdentifier = URL_IDENTFR_BUILDINGS;
            } else if (requestType == RequestType.MAP_POIS) {
                strIdentifier = URL_IDENTFR_POIS;
            } else if (requestType == RequestType.MAP_STREET_VIEW) {
                strIdentifier = URL_IDENTFR_STREETVIEW;
            }
            if (strIdentifier != null) {
                int lastIndex = shortUrl.lastIndexOf(strIdentifier) + strIdentifier.length();
                String strZoom = shortUrl.substring(lastIndex, lastIndex + 2);
                if (strZoom.contains("/")) {
                    int intZoom1 = Integer.valueOf(strZoom.replace("/", "")).intValue();
                    return intZoom1;
                } else {
                    return Integer.valueOf(strZoom).intValue();
                }
            } else {
                return intZoom;
            }
        } else {
            return intZoom;
        }
    }


}
