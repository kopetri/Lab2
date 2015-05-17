package muc_15_01_14.lab2;

/**
 * Created by Sebastian on 17.05.2015.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;

public class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    private BluetoothSocket socket = null;
    private MainActivity mainActivity = null;



    private BluetoothAdapter mBtAdapter = null;
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
                mainActivity.manageConnectedSocket(socket);
                break;
            }
        }
    }

    public BluetoothAdapter getmBtAdapter() {
        return mBtAdapter;
    }

    public void setmBtAdapter(BluetoothAdapter mBtAdapter) {
        this.mBtAdapter = mBtAdapter;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
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