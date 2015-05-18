package muc_15_01_14.lab2;

/**
 * Created by Sebastian on 17.05.2015.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class AcceptThread extends AsyncTask<BluetoothServerSocket,Void,BluetoothSocket> {
    private final BluetoothServerSocket mmServerSocket;
    private BluetoothSocket socket = null;
    private MainActivity mainActivity = null;
    private BluetoothAdapter mBtAdapter = null;
    private boolean executing = false;

    public BluetoothServerSocket getMmServerSocket() {
        return mmServerSocket;
    }

    public AcceptThread(MainActivity mainActivity, BluetoothAdapter mBtAdapter, String name, String uuid) {
        BluetoothServerSocket tmp = null;
        this.mBtAdapter = mBtAdapter;
        this.mainActivity = mainActivity;

        try {
            tmp = mBtAdapter.listenUsingRfcommWithServiceRecord(name, UUID.fromString(uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmServerSocket = tmp;
    }

    @Override
    protected BluetoothSocket doInBackground(BluetoothServerSocket[] params) {
        executing = true;
        try {
            Log.i("BLUETOOTH", "start doInBackground from worker thread..." + params.length);
            if (params[0] != null){
                return params[0].accept();
            } else{
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isExecuting() {
        return executing;
    }

    @Override
    protected void onPostExecute(BluetoothSocket bluetoothSocket) {
        super.onPostExecute(bluetoothSocket);
        mainActivity.manageConnectedSocket(bluetoothSocket);
        executing = false;
    }
}