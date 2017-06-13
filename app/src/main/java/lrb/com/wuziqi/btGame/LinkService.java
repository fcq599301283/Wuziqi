package lrb.com.wuziqi.btGame;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import java.util.UUID;

import lrb.com.wuziqi.data.Info;
import lrb.com.wuziqi.btGame.thread.AcceptThread;
import lrb.com.wuziqi.btGame.thread.ConnectThread;
import lrb.com.wuziqi.btGame.thread.ConnectedThread;

import static lrb.com.wuziqi.btGame.MyHandler.STATE_CONNECTED;

/**
 * Created by FengChaoQun
 * on 2017/3/6
 */

public class LinkService {
    public static final String TAG = "LinkService";
    public static final UUID uuid = UUID.fromString("1995-0925-2838-2017-0505");
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private BluetoothDevice connecttedDevice;

    public static LinkService getInstance() {
        return SingletonHolder.linkService;
    }

    private static class SingletonHolder {
        private static final LinkService linkService = new LinkService(MyHandler.getInstance());
    }

    private LinkService(Handler mHandler) {
        this.mHandler = mHandler;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void start() {
        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        sendMessage(MyHandler.STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (acceptThread == null) {
            acceptThread = new AcceptThread(this);
            acceptThread.start();
        }

    }

    /**
     * description:和其余蓝牙建立连接
     * 关闭已有的连接
     */

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice bluetoothDevice, boolean isCline) {

        closeBTConnected();

        connectedThread = new ConnectedThread(socket, this, isCline);
        connectedThread.start();
        connecttedDevice = bluetoothDevice;
        MyHandler.getInstance().obtainMessage(STATE_CONNECTED, socket.getRemoteDevice()).sendToTarget();
    }

    /**
     * description:关闭已有的连接 取消监听 将已连接设备置空
     */

    private synchronized void closeBTConnected() {
        //关闭已有的连接
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        //取消监听
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        connecttedDevice = null;
    }

    public synchronized void resetConnect() {
        connectThread = null;
    }

    public synchronized void connect(BluetoothDevice device) {

        // Cancel any thread attempting to make a connection
        if (MyHandler.getInstance().getCurrentState() == MyHandler.STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device, this);
        connectThread.start();
        sendMessage(MyHandler.STATE_CONNECTING);
    }

    public synchronized void sendMessage(int message) {
        mHandler.obtainMessage(message).sendToTarget();
    }

    public synchronized void sendMessage(int message, int length, byte[] text) {
        mHandler.obtainMessage(message, length, -1, text).sendToTarget();
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (connectedThread == null || !connectedThread.isAlive()) {
                Log.d(TAG, "is not connected");
                return;
            }
            r = connectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    public boolean isCline() {
        if (connectedThread != null) {
            return connectedThread.isCline();
        } else {
            return false;
        }
    }

    public void agree(boolean agree) {
        Info info = new Info();
        info.setType(agree ? Info.TYPE_AGREE : Info.TYPE_REFUSE);
        Gson gson = new Gson();
        write(gson.toJson(info).getBytes());
        Log.d("write", gson.toJson(info));
    }

}
