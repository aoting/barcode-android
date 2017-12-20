package com.xat.barcodestore;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xat.barcodestore.model.Barcode;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog homeProgressDialog;
    private LoadedBroadcastReceiver loadedBroadcastReceiver;
    private TextView mTextMessage;

    private LoadHistoryService barcodeService;
    private boolean historyServiceBound;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected  void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

        }
        Intent intent = new Intent(this, LoadHistoryService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeProgressDialog = new ProgressDialog(this);
        homeProgressDialog.setTitle("Loading");
        homeProgressDialog.setMessage("Wait while loading information...");
        homeProgressDialog.show();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LoadedBroadcastReceiver.LOADED_ACTION);
        loadedBroadcastReceiver = new LoadedBroadcastReceiver();
        registerReceiver(loadedBroadcastReceiver, filter);

        setContentView(R.layout.activity_main);



        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(loadedBroadcastReceiver);
        unbindService(mConnection);
    }

    public void createBarcode(View view) {
        String name = "abc";
        String value = "efg";
        barcodeService.insertBarcode(name, value);
    }

    public void showBarcode(String name) {
        Toast.makeText(this.getApplicationContext(), name, Toast.LENGTH_SHORT).show();

        new BarcodeDialog().show(getFragmentManager(), "BarcodeDialog");
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            final LoadHistoryService.LocalBinder binder = (LoadHistoryService.LocalBinder) service;
            barcodeService = binder.getService();
            historyServiceBound = true;

            final BarcodeArrayAdapter barcodeArrayAdapter = new BarcodeArrayAdapter(getApplicationContext(), barcodeService.getBarcodes().toArray(new Barcode[0]));
            final ListView listview = findViewById(R.id.barcodeList);
            listview.setAdapter(barcodeArrayAdapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
                    final String name = ((TextView)itemView.findViewById(R.id.name)).getText().toString();
                    showBarcode(name);
                }
            });

            homeProgressDialog.cancel();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            historyServiceBound = false;
        }
    };

    public class LoadedBroadcastReceiver extends BroadcastReceiver {
        public static final String LOADED_ACTION = "Loaded";

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Log.i("History loaded", "onReceive: histories has been loaded");

            if (historyServiceBound) {
                List<Barcode> historyList = barcodeService.getBarcodes();

                mTextMessage = (TextView) findViewById(R.id.message);
                for (Barcode history : historyList) {
                    mTextMessage.setText(history.getName());
                }
            }

            homeProgressDialog.cancel();
        }

    }

}
