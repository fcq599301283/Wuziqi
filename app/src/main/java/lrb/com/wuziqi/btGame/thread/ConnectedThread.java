package lrb.com.wuziqi.btGame.thread;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lrb.com.wuziqi.btGame.LinkService;
import lrb.com.wuziqi.btGame.MyHandler;

/**
 * Created by FengChaoQun
 * on 2017/3/6
 */

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private LinkService linkService;
    private boolean isCline;

    public ConnectedThread(BluetoothSocket socket, LinkService linkService, boolean isCline) {
        mmSocket = socket;
        this.linkService = linkService;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        this.isCline = isCline;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            linkService.sendMessage(MyHandler.LaunchConnectedError);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                linkService.sendMessage(MyHandler.RECEIVE_MESSAGE, bytes, buffer);

            } catch (Exception e) {
                e.printStackTrace();
                linkService.sendMessage(MyHandler.connectLose);
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    public void write(byte[] buffer) {
        try {
            mmOutStream.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            linkService.sendMessage(MyHandler.connectLose);
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCline() {
        return isCline;
    }
}
