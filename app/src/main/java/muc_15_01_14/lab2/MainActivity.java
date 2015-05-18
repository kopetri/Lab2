package muc_15_01_14.lab2;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import de.dfki.ccaal.gestures.Distribution;
import de.dfki.ccaal.gestures.IGestureRecognitionListener;
import de.dfki.ccaal.gestures.IGestureRecognitionService;
import java.io.IOException;
import java.util.UUID;


public class MainActivity extends ActionBarActivity  {

    public static final String MASTER_KEY="ismaster";
    public static  final String MASTER_USER_KEY="mastername";
    public static  final String CLIENT_USER_KEY="clientname";
    public static final String UUID = "4080ad8d-8ba2-4846-8803-a3206a8975be";

    de.dfki.ccaal.gestures.IGestureRecognitionService mRecService;
    BluetoothAdapter mBtAdapter;
    private static int BLUETOOTH_ENABLED = 14;
    private final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    private AcceptThread acceptThread = null;
    private ConnectThread connectThread = null;

    public void setmSocket(BluetoothSocket mSocket) {
        this.mSocket = mSocket;
    }

    public static BluetoothSocket mSocket = null;

    //Variables for client representation on Gui
    private ListView availableDevicesList;
    private ArrayList<BluetoothDevice> availableDevicesStringArray = new ArrayList<BluetoothDevice>();
    private ArrayAdapter<BluetoothDevice> availableDevicesAdapter;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i("BLUETOOTH","device: " + device.getName()+" address: "+device.getAddress());
                availableDevicesStringArray.add(device);
                availableDevicesAdapter = new ArrayAdapter<BluetoothDevice>(getApplicationContext(), android.R.layout.simple_list_item_1, availableDevicesStringArray);
                availableDevicesList.setAdapter(availableDevicesAdapter);
            }
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Client list an on click listener for the clients
        availableDevicesList = (ListView) findViewById(R.id.list_availableDevices);
        availableDevicesList.setClickable(true);
        availableDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                // establish bluetooth connection
                BluetoothDevice device = (BluetoothDevice) arg0.getAdapter().getItem(position);
                Log.i("BLUETOOTH","device: "+device.getName());

                    if(!connectThread.isExecuting()) {
                        connectThread.execute(connectThread.getmSocket(device));
                    }

            }
        });
        Button startServer = (Button) findViewById(R.id.btn_startNewGame);
        startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!acceptThread.isExecuting()) {
                    acceptThread.execute(acceptThread.getMmServerSocket());
                }
            }
        });
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            popupDialog(this, "Fehler", "Kein Bluetooth verfügbar");
            this.finish();
        }

        // enable bluetooth if disabled
        if (!mBtAdapter.isEnabled()) {
            Log.i("BLUETOOTH", "BTAdapter was not enabled");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_ENABLED);
        } else {
            Log.i("BLUETOOTH", "BTAdapter is enabled");
        }

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        acceptThread = new AcceptThread(this,mBtAdapter,"MUCmimic",UUID);
        connectThread = new ConnectThread(this,mBtAdapter);
    }



    public void manageConnectedSocket(BluetoothSocket socket){
        setmSocket(socket);
        Intent intent = new Intent(getApplicationContext(), GameOverviewActivity.class);
        intent.putExtra(MainActivity.MASTER_KEY,true);
        intent.putExtra(MainActivity.MASTER_USER_KEY,"Master");
        String name = ((EditText)findViewById(R.id.etxt_username)).getText().toString();
        if(name.trim().equals("")){
            name = "You";
        }
        intent.putExtra(MainActivity.CLIENT_USER_KEY,name);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_info) {
            //
            popupDialog(this, "About", "Mobile & Ubiquitous Computing\nAssignment 2 - Mimicry Game\n\nTheresa Hirzle\nSebastian Hardwig\nDavid Lehr");
        }
        return super.onOptionsItemSelected(item);
    }

    //bind service
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        if(mBtAdapter!=null) {
            if(!mBtAdapter.isDiscovering()) {
                mBtAdapter.startDiscovery();
            }
        }
    }

    //unbind service
    @Override
    protected void onPause() {
        super.onPause();

        if(mBtAdapter!=null) {
            mBtAdapter.cancelDiscovery();
        }


        if(mReceiver != null){
            unregisterReceiver(mReceiver);
        }

        mRecService = null;
    }

    public static void popupDialog(Context context, String title, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(text)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult", "called");
        if (requestCode == BLUETOOTH_ENABLED) {
            if (resultCode == RESULT_CANCELED) {
                popupDialog(this, "Bluetooth", "Bluetooth muss aktiviert werden um die Anwendung zu nützen");
                this.finish();
            }
            if (resultCode == RESULT_OK) {
                Log.i("BLUETOOTH", "RESULT_OK");
            }
        }
    }

    // On click listener for start new game as Master
    public void clickStartNewGame(View view) {
        Intent intent = new Intent(getApplicationContext(), GameOverviewActivity.class);
        intent.putExtra(MASTER_KEY,true);
        String name = ((EditText)findViewById(R.id.etxt_username)).getText().toString();
        if(name.trim().equals("")){
            name = "You";
        }
        intent.putExtra(MASTER_USER_KEY,name);
        intent.putExtra(CLIENT_USER_KEY,"Challenger");
        startActivity(intent);
    }

    /*public void addAvailableDevices(List availableDevices) {
        for (int i = 0; i < availableDevices.size(); i++) {
            availableDevicesStringArray.add(availableDevices);
        }
        availableDevicesAdapter.notifyDataSetChanged();
    }*/
}