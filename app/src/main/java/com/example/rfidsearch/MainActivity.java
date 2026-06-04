package com.example.rfidsearch;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zebra.rfid.api3.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Readers readers;
    private ArrayList<ReaderDevice> availableRFIDReaderList;
    private ReaderDevice readerDevice;
    private RFIDReader reader;
    private static String TAG = "DEMO";
    TextView statusText;
    TextView resultText;
    private EventHandler eventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        // UI
        statusText = (TextView) findViewById(R.id.statusText);
        resultText = (TextView) findViewById(R.id.resultText);

        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reader != null && reader.isConnected()) {
                    try {
                        resultText.setText("");
                        reader.Actions.Inventory.perform();
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }
                } else {
                    statusText.setText("Reader not connected");
                }
            }
        });

        // SDK
        if (readers == null) {
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        }

        // Connect to RFID reader in background thread
        new Thread(() -> {
            boolean connected = false;
            try {
                if (readers != null) {
                    if (readers.GetAvailableRFIDReaderList() != null) {
                        availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                        if (availableRFIDReaderList.size() != 0) {
                            readerDevice = (ReaderDevice) availableRFIDReaderList.get(0);
                            reader = readerDevice.getRFIDReader();
                            if (!reader.isConnected()) {
                                reader.connect();
                                ConfigureReader();
                                connected = true;
                            }
                        }
                    }
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
                Log.d(TAG, "OperationFailureException " + e.getVendorMessage());
            }
            final boolean finalConnected = connected;
            runOnUiThread(() -> {
                if (finalConnected) {
                    Toast.makeText(getApplicationContext(), "Reader Connected", Toast.LENGTH_LONG).show();
                    statusText.setText("Reader connected");
                } else {
                    statusText.setText("Reader not found");
                }
            });
        }).start();
    }

    // Configure reader settings
    private void ConfigureReader() {
        if (reader.isConnected()) {
            TriggerInfo triggerInfo = new TriggerInfo();
            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            try {
                // receive events from reader
                if (eventHandler == null)
                    eventHandler = new EventHandler();
                reader.Events.addEventsListener(eventHandler);
                // HH event
                reader.Events.setHandheldEvent(true);
                // tag event with tag data
                reader.Events.setTagReadEvent(true);
                // application will collect tag using getReadTags API
                reader.Events.setAttachTagDataWithReadEvent(false);
                // set trigger mode as rfid so scanner beam will not come
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                // set start and stop triggers
                reader.Config.setStartTrigger(triggerInfo.StartTrigger);
                reader.Config.setStopTrigger(triggerInfo.StopTrigger);
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (reader != null) {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
                Toast.makeText(getApplicationContext(), "Disconnecting reader", Toast.LENGTH_LONG).show();
                reader = null;
                readers.Dispose();
                readers = null;
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read/Status Notify handler
    // Implement the RfidEventsLister class to receive event notifications
    public class EventHandler implements RfidEventsListener {
        // Read Event Notification
        public void eventReadNotify(RfidReadEvents e) {
            TagData[] myTags = reader.Actions.getReadTags(100);
            if (myTags != null) {
                final StringBuilder sb = new StringBuilder();
                for (int index = 0; index < myTags.length; index++) {
                    sb.append(myTags[index].getTagID()).append("\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultText.append(sb.toString());
                    }
                });
            }
        }

        // Status Event Notification
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
        }
    }
}
