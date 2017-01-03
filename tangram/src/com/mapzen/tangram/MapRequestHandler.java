package com.mapzen.tangram;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import okio.BufferedSource;

import static android.content.ContentValues.TAG;

/**
 * Class created by magma3 on 10/26/2016.
 */

public class MapRequestHandler {

    private Context context;
    private TPLMapDecryptManager tplMapDecryptManager;
    private HttpHandler httpHandler;
    String offlineStoragePath;

    public MapRequestHandler(Context context, String key) {
        this.context = context;

        // Set a default HTTPHandler
        httpHandler = new HttpHandler();
        //
        tplMapDecryptManager = new TPLMapDecryptManager(key);
    }

    public void onMapRequest(String url, final MapRequestCallback mapRequestCallback) {

        if(httpHandler == null) {
            mapRequestCallback.onMapRequestFailure(false);
            return;
        }

        // Dont send request if it does not meet criteria
        if (!TPLHttpRequestManager.shouldRequestToServer(url)) {
            Log.i(TAG, "URL: Request not sent " + url);
            mapRequestCallback.onMapRequestFailure(false);
            return;
        }

        Log.i(TAG, "URL: serverpath " + url);

        httpHandler.onRequest(url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i(TAG, "URL: onFailure");
                mapRequestCallback.onMapRequestFailure(false);
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mapRequestCallback.onMapRequestFailure(true);
                    throw new IOException("Unexpected response code: " + response);
                }
                Log.i(TAG, "URL: response: " + response);

                BufferedSource source = response.body().source();
                byte[] bytes = source.readByteArray();
                mapRequestCallback.onMapRequestSuccess(bytes);
            }
        });
        // Check if offline mode or not
        /*if(isInternetConnectionAvailable(context)) {

            if(httpHandler == null) {
                mapRequestCallback.onMapRequestFailure(false);
                return;
            }

            // Dont send request if it does not meet criteria
            if (!TPLHttpRequestManager.shouldRequestToServer(url)) {
                Log.i(TAG, "URL: Request not sent " + url);
                mapRequestCallback.onMapRequestFailure(false);
                return;
            }

            Log.i(TAG, "URL: serverpath " + url);

            httpHandler.onRequest(url, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    mapRequestCallback.onMapRequestFailure(true);
                }

                @Override
                public void onResponse(final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        mapRequestCallback.onMapRequestFailure(true);
                        throw new IOException("Unexpected response code: " + response);
                    }
                    Log.i(TAG, "URL: serverpath response: " + response);

                    BufferedSource source = response.body().source();
                    byte[] bytes = source.readByteArray();
                    mapRequestCallback.onMapRequestSuccess(bytes);
                }
            });
        }
        else
        {
            // Get tile from local storage
            // update url to get tile from local storage
            String localStoragePath = getLocalStorageTilePath(url);
            if(!new File(localStoragePath).exists()) {
                mapRequestCallback.onMapRequestFailure(false);
                return;
            }

            Log.i(TAG, "URL: localStoragePath" + localStoragePath);
            byte[] bytes = getOfflineTileInBytes(localStoragePath);
            decryptTileTask(bytes, mapRequestCallback);
        }*/

    }

    private void decryptTileTask(byte[] bytes, final MapRequestCallback mapRequestCallback) {
        tplMapDecryptManager.decryptMapTile(bytes, new TPLMapDecryptManager.MapDecryptCallback() {
            @Override
            public void onFailure() {
                mapRequestCallback.onMapRequestFailure(false);
            }

            @Override
            public void onResponse(byte[] decryptedMapTileInBytes) {
                mapRequestCallback.onMapRequestSuccess(decryptedMapTileInBytes);
            }
        });
    }

    public void onCancelMapRequest(String url) {
        if(httpHandler == null)
            return;
        httpHandler.onCancel(url);
    }

    private String getLocalStorageTilePath(String url) {

        String type = "";
        if(url.contains("composite"))
            type = "composite";
        else if(url.contains("pois"))
            type = "pois";
        else if(url.contains("buildings"))
            type = "buildings";

        String tilePath = url.split(type + "/")[1];
        if(tilePath.contains(".json"))
            tilePath = tilePath.replace(".json", ".mvt");

        return offlineStoragePath + type + "/" + tilePath;
    }

    private byte[] getOfflineTileInBytes(String path) {

        File file = new File(path);
        FileInputStream fis = null;
        byte[] bytes = new byte[(int)file.length()];

        try
        {
            fis = new FileInputStream(file);
            BufferedInputStream buf = new BufferedInputStream(fis);
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    public static boolean isInternetConnectionAvailable(Context context) {
        boolean flag;
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        flag = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return flag;
    }

    /**
     * Set the {@link HttpHandler} for retrieving remote map resources; a default-constructed
     * HttpHandler is suitable for most cases, but methods can be extended to modify resource URLs
     * @param handler the HttpHandler to use
     */
    public void setHttpHandler(HttpHandler handler) {
        this.httpHandler = handler;
    }

    public void setOfflineMode(String offlineStoragePath) {
        this.offlineStoragePath = offlineStoragePath;
    }

    public interface MapRequestCallback {
        void onMapRequestFailure(boolean shouldCalledNativeFailure);
        void onMapRequestSuccess(byte[] bytes);
    }
}
