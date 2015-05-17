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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import de.dfki.ccaal.gestures.Distribution;
import de.dfki.ccaal.gestures.IGestureRecognitionListener;
import de.dfki.ccaal.gestures.IGestureRecognitionService;


public class MainActivity extends ActionBarActivity  {

    de.dfki.ccaal.gestures.IGestureRecognitionService mRecService;
    BluetoothAdapter mBtAdapter;
    private ArrayList mArrayAdapter;
    private static int BLUETOOTH_ENABLED = 14;
    private final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    private AcceptThread acceptThread = new AcceptThread("MUCmimic","4080ad8d-8ba2-4846-8803-a3206a8975be");
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mArrayAdapter.add(device.getName()+"\n"+device.getAddress());

                //Log.i("BLUETOOTH", "device " + device.getName() + " address " + device.getAddress());

            }
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArrayAdapter = new ArrayList();
        setContentView(R.layout.activity_main);
        Button startServer = (Button) findViewById(R.id.start_server_button);
        startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!acceptThread.isAlive()){
                    acceptThread.start();
                } else{
                    Log.i("BLUETOOTH","thread is alive");
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
            //Log.i("BLUETOOTH","BTAdapter was not enabled");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_ENABLED);
        }

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);


    }

    public void manageConnectedSocket(BluetoothSocket socket){
        Log.i("BLUETOOTH","socket returned - pairing done");
        popupDialog(this,"Paired","Pairing done!");
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //bind service
    @Override
    protected void onResume() {
        super.onResume();

        if(mBtAdapter!=null) {
            if(!mBtAdapter.isDiscovering()) {
                mBtAdapter.startDiscovery();
            }
        }
        //implicit intent ( < Android 5.0 )
        //Intent gestureBindIntent = new Intent("de.dfki.ccaal.gestures.GESTURE_RECOGNIZER");

        //explicit intent ( > Android 5.0)
        Intent gestureBindIntent = new Intent(this,IGestureRecognitionService.class);
        bindService(gestureBindIntent,mGestureConn,Context.BIND_AUTO_CREATE);
        registerReceiver(mReceiver,filter);
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
        try {
            if (mRecService != null) {
                mRecService.unregisterListener(IGestureRecognitionListener.
                        Stub.asInterface(mGestureListenerStub));
            }
        } catch (android.os.RemoteException e) {
            e.printStackTrace();
        }
        mRecService = null;
        unbindService(mGestureConn);
    }

    //create a service connection to the recognition service
    private ServiceConnection mGestureConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mRecService =
                    IGestureRecognitionService.Stub.asInterface(service);
            try {
                mRecService.registerListener(
                        IGestureRecognitionListener.Stub.asInterface(
                                mGestureListenerStub));
                mRecService.startClassificationMode("muc");
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    //create gestureListener
    IBinder mGestureListenerStub =
            new IGestureRecognitionListener.Stub() {
                @Override
                public void onGestureRecognized(Distribution distr) {
                    final String gesture = distr.getBestMatch();
                    final double distance = distr.getBestDistance();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView text = (TextView) findViewById(R.id.text_gesture);
                            text.setText(gesture + " " + String.valueOf(distance));
                        }
                    });

                }

                @Override
                public void onGestureLearned(String gestureName) throws RemoteException {

                }

                @Override
                public void onTrainingSetDeleted(String trainingSet) throws RemoteException {

                }
            };


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

    class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private BluetoothSocket socket = null;
        WorkerThread workerThread = new WorkerThread();

        public AcceptThread(String name, String uuid) {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;

            try {
                // MY_UUID is the app's UUID string, also used by the client code
                if(mBtAdapter!=null) {
                    tmp = mBtAdapter.listenUsingRfcommWithServiceRecord(name, UUID.fromString(uuid));
                }
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void setSocket(BluetoothSocket socket) {
            this.socket = socket;
        }

        public void run() {

            // Keep listening until exception occurs or a socket is returned
            while (true) {
                if(mmServerSocket!=null) {

                    workerThread.execute(mmServerSocket);
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }

        class WorkerThread extends AsyncTask<BluetoothServerSocket,Void,BluetoothSocket> {
            @Override
            protected BluetoothSocket doInBackground(BluetoothServerSocket[] params) {
                try {
                    return params[0].accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(BluetoothSocket bluetoothSocket) {
                super.onPostExecute(bluetoothSocket);
                setSocket(bluetoothSocket);
            }
        }
    }
}
