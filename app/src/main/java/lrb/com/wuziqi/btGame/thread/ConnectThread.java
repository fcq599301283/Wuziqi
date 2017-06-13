package lrb.com.wuziqi.btGame.thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

import lrb.com.wuziqi.btGame.BTUtils;
import lrb.com.wuziqi.btGame.LinkService;
import lrb.com.wuziqi.btGame.MyHandler;

/**
 * Created by FengChaoQun
 * on 2017/3/6
 */

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private LinkService linkService;

    public ConnectThread(BluetoothDevice device, LinkService linkService) {
        mmDevice = device;
        this.linkService = linkService;
        BluetoothSocket tmp = null;

        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            tmp = device.createRfcommSocketToServiceRecord(
                    LinkService.uuid);

        } catch (IOException e) {
            e.printStackTrace();
            linkService.sendMessage(MyHandler.LaunchConnectError);
        }
        mmSocket = tmp;

    }

    public void run() {
        setName("ConnectThread");

        // Always cancel discovery because it will slow down a connection
        BTUtils.bluetoothAdapter.cancelDiscovery();

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mmSocket.connect();
        } catch (IOException e) {
            // Close the socket
            try {
                mmSocket.close();
            } catch (IOException e2) {
                e.printStackTrace();
            }
            e.printStackTrace();
            linkService.sendMessage(MyHandler.STATE_CONNECT_FAIL);
            return;
        }

        // Reset the ConnectThread because we're done
        synchronized (this) {
            linkService.resetConnect();
        }

        // Start the connected thread
        linkService.connected(mmSocket, mmDevice, false);
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
