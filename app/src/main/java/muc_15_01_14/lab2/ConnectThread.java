package muc_15_01_14.lab2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Sebastian on 15.05.2015.
 */
public class ConnectThread extends Thread {
    private BluetoothSocket mSocket;
    private BluetoothDevice mServerDevice;
    private BluetoothAdapter mBtAdapter;
    public ConnectThread(BluetoothAdapter mBtAdapter,BluetoothDevice serverDevice) throws IOException {
        mServerDevice = serverDevice;
        this.mBtAdapter = mBtAdapter;
        // Get a BluetoothSocket to connect with the server device
        // APP_UUID is the app's UUID string, also used by the server code
        mSocket = serverDevice.createRfcommSocketToServiceRecord(UUID.fromString("4080ad8d-8ba2-4846-8803-a3206a8975be"));
    }
    public void run() {
        // Cancel discovery because it will slow down the connection
        mBtAdapter.cancelDiscovery();
        // Connect the device through the socket (SDP lookup for UUID)
        // This will block until it succeeds or fails
        try {
            mSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Do work to manage the connection (in a separate thread)
        //manageConnectedSocket(mSocket);
    }
}