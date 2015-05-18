package muc_15_01_14.lab2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Sebastian on 15.05.2015.
 */
public class ConnectThread extends AsyncTask<BluetoothSocket,Void,Boolean> {


    private BluetoothSocket mSocket = null;
    private BluetoothDevice mServerDevice;
    private BluetoothAdapter mBtAdapter;
    private boolean executing = false;
    private MainActivity context = null;


    public ConnectThread(MainActivity context, BluetoothAdapter mBtAdapter){
        this.mBtAdapter = mBtAdapter;
        this.context = context;
    }

    public BluetoothSocket getmSocket(BluetoothDevice mServerDevice) {
        this.mServerDevice = mServerDevice;
        try {
            mSocket = mServerDevice.createRfcommSocketToServiceRecord(UUID.fromString(MainActivity.UUID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mSocket;
    }
    public boolean isExecuting() {
        return executing;
    }

    @Override
    protected Boolean doInBackground(BluetoothSocket[] params) {
        executing = true;
        mBtAdapter.cancelDiscovery();
        try {
            if(params[0] != null) {
                params[0].connect();
                return params[0].isConnected();
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean b) {
        if(b){
            clientConnected();
            Log.i("BLUETOOTH", "connection established!");
        } else{
            Log.e("BLUETOOTH","connection error!");
        }
        executing = false;

    }
    public void clientConnected(){
        MainActivity.mSocket = mSocket;
        Intent intent = new Intent(context.getApplicationContext(), GameOverviewActivity.class);
        intent.putExtra(MainActivity.MASTER_KEY,false);
        intent.putExtra(MainActivity.MASTER_USER_KEY,"Challenger");
        String name = ((EditText)context.findViewById(R.id.etxt_username)).getText().toString();
        if(name.trim().equals("")){
            name = "You";
        }
        intent.putExtra(MainActivity.CLIENT_USER_KEY,name);
        context.startActivity(intent);
    }
}