package com.tplmaps3d.geometry;

import com.tplmaps3d.LngLat;

import java.util.List;
import java.util.Map;

/**
 * {@code Polyline} is a sequence of LngLat points and its properties.
 *
 * Users do not need to use this class.
 */
public class Polyline extends Geometry {

    public Polyline(List<LngLat> polyline, Map<String, String> properties) {
        this.coordinates = new double[polyline.size() * 2];
        int i = 0;
        for (LngLat point : polyline) {
            coordinates[i++] = point.longitude;
            coordinates[i++] = point.latitude;
        }
        if (properties != null) {
            this.properties = getStringMapAsArray(properties);
        }
    }
    
}
