package muc_15_01_14.lab2;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import de.dfki.ccaal.gestures.Distribution;
import de.dfki.ccaal.gestures.IGestureRecognitionListener;
import de.dfki.ccaal.gestures.IGestureRecognitionService;


public class MainActivity extends ActionBarActivity {

    de.dfki.ccaal.gestures.IGestureRecognitionService mRecService;
    BluetoothAdapter mBtAdapter;
    private ArrayList mArrayAdapter;
    private static int BLUETOOTH_ENABLED = 14;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            popupDialog(this, "Fehler", "Kein Bluetooth verfügbar");
            this.finish();
        }


        // enable bluetooth if disabled
        if (!mBtAdapter.isEnabled()) {
            Log.i("BLUETOOTH","BTAdapter was not enabled");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_ENABLED);
        } else{
            Log.i("BLUETOOTH","BTAdapter is enabled");
        }

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        if(mBtAdapter.isDiscovering()){
            Log.i("BLUETOOTH","is Discovering");
        } else {
            Log.i("BLUETOOTH","is not Discovering");
        }

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
        //implicit intent ( < Android 5.0 )
        //Intent gestureBindIntent = new Intent("de.dfki.ccaal.gestures.GESTURE_RECOGNIZER");

        //explicit intent ( > Android 5.0)
        Intent gestureBindIntent = new Intent(this,IGestureRecognitionService.class);
        bindService(gestureBindIntent,mGestureConn,Context.BIND_AUTO_CREATE);
    }

    //unbind service
    @Override
    protected void onPause() {
        super.onPause();
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



    public void find_Devices(View view){
        mArrayAdapter = new ArrayList();

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        if(pairedDevices.size()>0){
            for(BluetoothDevice device : pairedDevices){

                System.out.println(device.getAddress());System.out.println(device.getName());
                System.out.println(device.getUuids());

            }

        }

        /*BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        mBtAdapter.startDiscovery();*/



        StringBuilder sb = new StringBuilder();

        Log.i("Info", "Bluetoothgeräte Anzahl: " + String.valueOf(mArrayAdapter.size()));


        for(int i =0;i<mArrayAdapter.size();i++){
            System.out.println(mArrayAdapter.get(i));
            sb.append(mArrayAdapter.get(i));
        }
        ((TextView)findViewById(R.id.text_devices)).setText(sb.toString());


    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult","called");
        if (requestCode == BLUETOOTH_ENABLED) {
            if (resultCode == RESULT_CANCELED) {
                popupDialog(this, "Bluetooth", "Bluetooth muss aktiviert werden um die Anwendung zu nützen");
                this.finish();
            }
            if(resultCode == RESULT_OK){
                Log.i("BLUETOOTH","RESULT_OK");
            }
        }
    }
}
