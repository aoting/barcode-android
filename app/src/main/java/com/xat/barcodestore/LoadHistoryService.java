package com.xat.barcodestore;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xat.barcodestore.model.Barcode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xat on 9/12/17.
 */

public class LoadHistoryService extends Service {

    private final String LOAD_HISTORY_SERVICE_TAG = "LOAD_HISTORY_SERVICE";

    private static final Type HISTORY_TYPE = new TypeToken<List<Barcode>>() {}.getType();
    private List<Barcode> barcodes;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public List<Barcode> getBarcodes() {
	    return barcodes;
    }

    @Override
    public IBinder onBind(Intent intent) {
        loadHistories();
        return mBinder;
    }

    private void loadHistories() {
        Log.i("LOAD_HISTORY_SERVICE", "loadHistories: start loading histories");
        Log.i(LOAD_HISTORY_SERVICE_TAG, "external storage state: " + Environment.getExternalStorageState());

        File root = new File(Environment.getExternalStorageDirectory(), "BarcodeStorage");
        if (!root.exists()) {
            boolean appFolderCreated = root.mkdir();
            Log.i(LOAD_HISTORY_SERVICE_TAG, "create app root: " + appFolderCreated);
            if (!appFolderCreated) {
                return;
            }
        }

        Log.i(LOAD_HISTORY_SERVICE_TAG, "Reading history file: history.json");
        File historyFile = new File(root, "history.json");
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                Log.i("History File Error", "Unable to create history file: history.json");
            }
        }


        try {
            Gson gson = new Gson();
            barcodes = gson.fromJson(new FileReader(historyFile), HISTORY_TYPE);

            if (barcodes == null) {
                barcodes = new ArrayList<>();
            }

            Log.i(LOAD_HISTORY_SERVICE_TAG, "loadHistories: ");
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.LoadedBroadcastReceiver.LOADED_ACTION);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(broadcastIntent);
        } catch (FileNotFoundException e) {
            Log.e("History File Error", "Unable to read/create history file", e);
        }
    }

    public void insertBarcode(String name, String barcodeValue) {

        Log.i(LOAD_HISTORY_SERVICE_TAG, "insertBarcode: " + name + " " + barcodeValue);
        Barcode newBarcode = new Barcode();
        newBarcode.setName(name);
        newBarcode.setValue(barcodeValue);

        barcodes.add(newBarcode);

        persistBarcodes();
    }

    private void persistBarcodes() {
        File root = new File(Environment.getExternalStorageDirectory(), "BarcodeStorage");
        if (!root.exists()) {
            boolean appFolderCreated = root.mkdir();
            Log.i(LOAD_HISTORY_SERVICE_TAG, "create app root: " + appFolderCreated);
            if (!appFolderCreated) {
                return;
            }
        }

        File historyFile = new File(root, "history.json");
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                Log.i("History File Error", "Unable to create history file: history.json");
            }
        }

        try (java.io.Writer writer = new FileWriter(historyFile)) {
            Gson gson = new Gson();
            gson.toJson(barcodes, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e(LOAD_HISTORY_SERVICE_TAG, "persistBarcodes: ", e);
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LoadHistoryService getService() {
            // Return this instance of LoadHistoryService so clients can call public methods
            return LoadHistoryService.this;
        }
    }
}
