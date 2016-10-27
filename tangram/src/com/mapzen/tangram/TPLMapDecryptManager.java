package com.mapzen.tangram;

import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class created by magma3 on 8/26/2016.
 */
public class TPLMapDecryptManager {

    MapDecryptCallback mapDecryptCallback;
    String keyStr;

    public TPLMapDecryptManager(String keyStr) {
        this.keyStr = keyStr;
    }

    public void decryptMapTile(final byte[] bytes, final MapDecryptCallback mapDecryptCallback) {

        this.mapDecryptCallback = mapDecryptCallback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] decryptedMapTileInBytes = decryptMapTile(bytes);
                mapDecryptCallback.onResponse(decryptedMapTileInBytes);
            }
        }).start();
    }

    private byte[] decryptMapTile(byte[] bytes) {

        byte[] key = keyStr.getBytes();
        byte[] iv, encryptedContentBytes;

        iv = Arrays.copyOfRange(bytes, 0, 16);
        encryptedContentBytes = Arrays.copyOfRange(bytes, 16, bytes.length);

        Cipher cipher;
        byte[] decryptedData = null;
        try
        {
            // Start decryption
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv));
            decryptedData = cipher.doFinal(encryptedContentBytes);

        } catch (Exception e) {
            e.printStackTrace();
            mapDecryptCallback.onFailure();
        }
        return decryptedData;
    }

    public interface MapDecryptCallback {
        void onFailure();
        void onResponse(byte[] bytes);
    }
}
