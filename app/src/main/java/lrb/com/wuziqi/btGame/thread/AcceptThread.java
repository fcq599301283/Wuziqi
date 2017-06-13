package lrb.com.wuziqi.btGame.thread;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

import lrb.com.wuziqi.btGame.BTUtils;
import lrb.com.wuziqi.btGame.LinkService;
import lrb.com.wuziqi.btGame.MyHandler;

/**
 * Created by FengChaoQun
 * on 2017/3/6
 */

public class AcceptThread extends Thread {
    public static final String TAG = "AcceptThread";
    // The local server socket
    private final BluetoothServerSocket mmServerSocket;
    private LinkService linkService;
    private boolean isCancel;

    public AcceptThread(LinkService linkService) {
        this.linkService = linkService;

        BluetoothServerSocket tmp = null;

        // Create a new listening server socket
        try {

            tmp = BTUtils.bluetoothAdapter.listenUsingRfcommWithServiceRecord(TAG,
                    LinkService.uuid);

        } catch (Exception e) {
            e.printStackTrace();
            linkService.sendMessage(MyHandler.LaunchAcceptError);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        setName("AcceptThread");

        BluetoothSocket socket = null;

        // Listen to the server socket if we're not connected
        while (MyHandler.getInstance().getCurrentState() != MyHandler.STATE_CONNECTED) {
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mmServerSocket.accept();
            } catch (Exception e) {
                e.printStackTrace();
                if (!isCancel) {
                    linkService.sendMessage(MyHandler.RunAcceptError);
                }
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                synchronized (this) {
                    switch (MyHandler.getInstance().getCurrentState()) {
                        case MyHandler.STATE_LISTEN:
                        case MyHandler.STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            linkService.connected(socket, socket.getRemoteDevice(),true);
                            break;
                        case MyHandler.STATE_NONE:
                        case MyHandler.STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                    }
                }
            }
        }

    }

    public void cancel() {
        try {
            isCancel = true;
            mmServerSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
