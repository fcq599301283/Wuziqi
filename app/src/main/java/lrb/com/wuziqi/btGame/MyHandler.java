package lrb.com.wuziqi.btGame;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FengChaoQun
 * on 2017/3/6
 */

public class MyHandler extends Handler {

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_CONNECT_FAIL = 4;
    public static final int RECEIVE_MESSAGE = 5;    //receive new message

    public static final int LaunchAcceptError = 10;
    public static final int RunAcceptError = 11;
    public static final int LaunchConnectedError = 12;
    public static final int RunConnectedError = 13;
    public static final int connectLose = 14;
    public static final int LaunchConnectError = 15;

    public static final int connectNormal = 100;

    public static final String ok = "ok";
    public static final byte[] okByte = ok.getBytes();

    private List<OnStateChange> OnStateChanges = new ArrayList<>();
    private int currentState;


    public static MyHandler getInstance() {
        return MyHandler.SingletonHolder.myHandler;
    }

    private static class SingletonHolder {
        private static final MyHandler myHandler = new MyHandler();
    }

    private MyHandler() {
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        currentState = msg.what;
        Log.d("MyHandler", "state change:" + currentState + "--->" + msg.what);

        for (int i = 0; i < OnStateChanges.size(); i++) {
            OnStateChanges.get(i).onChange(msg);
        }

        //连接错误时  重启
        switch (currentState) {
            case STATE_CONNECT_FAIL:
            case LaunchAcceptError:
            case RunAcceptError:
            case LaunchConnectedError:
            case RunConnectedError:
            case connectLose:
            case LaunchConnectError:
                LinkService.getInstance().start();
                break;
        }
    }

    public void register(OnStateChange OnStateChange, boolean register) {
        if (OnStateChange == null) {
            return;
        }
        if (register) {
            OnStateChanges.add(OnStateChange);
        } else {
            OnStateChanges.remove(OnStateChange);
        }
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public interface OnStateChange {
        void onChange(Message msg);
    }
}
