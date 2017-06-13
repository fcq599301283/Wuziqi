package lrb.com.wuziqi.btGame;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.flyco.animation.ZoomEnter.ZoomInEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import lrb.com.wuziqi.R;
import lrb.com.wuziqi.data.Info;
import lrb.com.wuziqi.data.Invite;
import lrb.com.wuziqi.utils.LoadingDialog;
import lrb.com.wuziqi.utils.ScreenUtils;
import lrb.com.wuziqi.utils.ToastUtils;
import lrb.com.wuziqi.utils.runtimePermission.AndPermission;
import lrb.com.wuziqi.utils.runtimePermission.CheckPermission;
import lrb.com.wuziqi.utils.runtimePermission.PermissionNo;
import lrb.com.wuziqi.utils.runtimePermission.PermissionYes;
import lrb.com.wuziqi.utils.runtimePermission.Rationale;
import lrb.com.wuziqi.utils.runtimePermission.RationaleListener;

/**
 * Created by FengChaoQun
 * on 2017/5/5
 * 蓝牙对战邀请界面
 */

public class BTGameSetting extends Activity {

    public static final int REQUEST_LOCATION_PERMISSION = 100;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private Button search;
    private RadioButton whiteFirst, blackFirst, normalChessBoard, smallChessBoard, whitePice, blackPiece;

    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private ListView listView;
    private btAdapter adapter;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_setting);
        initView();
        registerReceiver(true);
        checkPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerReceiver(false);
    }

    private void initView() {
        loadingDialog = new LoadingDialog(this);

        search = (Button) findViewById(R.id.search);
        whiteFirst = (RadioButton) findViewById(R.id.whiteFirst);
        blackFirst = (RadioButton) findViewById(R.id.blackFirst);
        normalChessBoard = (RadioButton) findViewById(R.id.normal);
        smallChessBoard = (RadioButton) findViewById(R.id.small);
        whitePice = (RadioButton) findViewById(R.id.whitePiece);
        blackPiece = (RadioButton) findViewById(R.id.blackPiece);

        whiteFirst.setChecked(true);
        normalChessBoard.setChecked(true);
        whitePice.setChecked(true);

        listView = (ListView) findViewById(R.id.listView);
        adapter = new btAdapter(getActivity(), android.R.layout.simple_list_item_1, bluetoothDevices);
        listView.setAdapter(adapter);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String size = normalChessBoard.isChecked() ? "15*15" : "12*12";
                String firstHandler = whiteFirst.isChecked() ? "白子先手" : "黑子先手";
                String pieceColor = whitePice.isChecked() ? "白子" : "黑子";
                showInviteDialog(String.format(getString(R.string.gameInfo), bluetoothDevices.get(position).getName(), size, firstHandler, pieceColor),
                        bluetoothDevices.get(position));
            }
        });

    }

    public void showLoadingDialog(String msg) {
        loadingDialog.setLoadText(msg);
        loadingDialog.show();
        WindowManager.LayoutParams lp = loadingDialog.getWindow().getAttributes();
        lp.width = ScreenUtils.dip2px(120); //设置宽度
        lp.height = ScreenUtils.dip2px(120); //设置宽度
        loadingDialog.getWindow().setAttributes(lp);
    }

    private void getRoundDevice() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(discoverableIntent);
        } else {
            if (bluetoothAdapter.isDiscovering()) {
                ToastUtils.showShort(getActivity(), "正在搜索附近设备");
                return;
            }
            bluetoothAdapter.startDiscovery();
            search.setText("正在搜索附近设备");
            LinkService.getInstance().start();
        }
    }

    private void checkPermission() {
        if (AndPermission.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (CheckPermission.isGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                getRoundDevice();
            } else {
                showPermissionRejectedDialog();
            }
        } else {
            requestBasicPermission();
        }
    }

    private void requestBasicPermission() {
        AndPermission.with(getActivity())
                .requestCode(REQUEST_LOCATION_PERMISSION)
                .permission(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(getActivity(), rationale).show();
                    }
                })
                .send();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    //申请权限成功 检查权限
    @PermissionYes(REQUEST_LOCATION_PERMISSION)
    private void getBasicGrant(List<String> grantedPermissions) {
        if (!CheckPermission.isGranted(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                || !CheckPermission.isGranted(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionRejectedDialog();
        } else {
            getRoundDevice();
        }
    }

    //申请权限失败
    @PermissionNo(REQUEST_LOCATION_PERMISSION)
    private void getBasicDenine(List<String> deniedPermissions) {
        Toast.makeText(this, "申请权限被拒绝", Toast.LENGTH_SHORT).show();
        // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
        if (AndPermission.hasAlwaysDeniedPermission(this, deniedPermissions)) {
            showPermissionRejectedDialog();
        } else {
            requestBasicPermission();
        }
    }

    private void showPermissionRejectedDialog() {
        AndPermission.defaultSettingDialog(this, REQUEST_LOCATION_PERMISSION)
                .setTitle("申请权限失败")
                .setMessage("需要位置权限才能搜索附近的蓝牙,请在设置页面的权限管理中授权，否则该功能无法使用.")
                .setPositiveButton("确定")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private Activity getActivity() {
        return this;
    }

    private void registerReceiver(boolean register) {

        MyHandler.getInstance().register(onStateChange, register);

        if (register) {
            // Register for broadcasts when a device is discovered
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getActivity().registerReceiver(mReceiver, filter);

            // Register for broadcasts when discovery has finished
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            getActivity().registerReceiver(mReceiver, filter);
        } else {
            // Unregister broadcast listeners
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!bluetoothDevices.contains(device)) {
                    bluetoothDevices.add(device);
                    adapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                search.setText("搜索附近设备");
            }
        }
    };

    private void showInviteDialog(String text, final BluetoothDevice bluetoothDevice) {
        final NormalDialog dialog = new NormalDialog(this);
        dialog.isTitleShow(true)//
                .title("对战信息")
                .bgColor(Color.parseColor("#383838"))//
                .cornerRadius(5)//
                .content(text)
                .btnText("取消", "邀战")
                .contentGravity(Gravity.CENTER)//
                .contentTextColor(Color.WHITE)//
                .btnTextSize(15.5f, 15.5f)//
                .btnTextColor(Color.WHITE, Color.WHITE, Color.WHITE)//
                .btnPressColor(Color.parseColor("#2B2B2B"))//
                .widthScale(0.85f)//
                .showAnim(new ZoomInEnter())//
                .show();
        dialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
            }
        }, new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
                LinkService.getInstance().connect(bluetoothDevice);
            }
        });
    }

    private void showInvitedDialog(final Info info) {
        final Invite invite = new Gson().fromJson(info.getInfo(), Invite.class);
        String text = String.format(getString(R.string.gameInfo), invite.getFromName(), String.valueOf(invite.getBoardSize()),
                invite.isWhiteFirst() ? "白子先手" : "黑子先手",
                invite.isObjectPieceWhite() ? "黑子" : "白子");
        final NormalDialog dialog = new NormalDialog(this);
        dialog.isTitleShow(true)//
                .title("挑战信息")
                .bgColor(Color.parseColor("#383838"))//
                .cornerRadius(5)//
                .content(text)
                .btnText("取消", "应战")
                .contentGravity(Gravity.CENTER)//
                .contentTextColor(Color.WHITE)//
                .btnTextSize(15.5f, 15.5f)//
                .btnTextColor(Color.WHITE, Color.WHITE, Color.WHITE)//
                .btnPressColor(Color.parseColor("#2B2B2B"))//
                .widthScale(0.85f)//
                .showAnim(new ZoomInEnter())//
                .show();
        dialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
                LinkService.getInstance().agree(false);
            }
        }, new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
                LinkService.getInstance().agree(true);
                Intent intent = new Intent(getActivity(), BTGameActivity.class);
                intent.putExtra(BTGameActivity.SIZE, invite.getBoardSize());
                intent.putExtra(BTGameActivity.FIRST_HAND, invite.isWhiteFirst());
                intent.putExtra(BTGameActivity.IS_YOU_FIRST, invite.isWhiteFirst() == invite.isObjectPieceWhite());
                startActivity(intent);
            }
        });
    }

    public void hideLoadingDialog() {
        loadingDialog.dismiss();
    }

    private MyHandler.OnStateChange onStateChange = new MyHandler.OnStateChange() {
        @Override
        public void onChange(Message msg) {
            switch (msg.what) {
                case MyHandler.STATE_CONNECTING:
                    showLoadingDialog("连接中");
                    break;
                case MyHandler.LaunchConnectError:
                    showNormalDialog("启动连接失败");
                    break;
                case MyHandler.LaunchConnectedError:
                    showNormalDialog("建立连接失败");
                    break;
                case MyHandler.STATE_CONNECTED:
                    ToastUtils.showShort(getActivity(), "连接成功");
                    hideLoadingDialog();
                    if (!LinkService.getInstance().isCline()) {
                        sendInviteInfo();
                    }
                    break;
                case MyHandler.STATE_CONNECT_FAIL:
                    hideLoadingDialog();
                    ToastUtils.showShort(getActivity(), "连接失败");
                    break;
                case MyHandler.connectLose:
                    ToastUtils.showShort(getActivity(), "失去连接");
                    break;
                case MyHandler.LaunchAcceptError:
                    showNormalDialog("启动接受连接组件失败");
                    break;
                case MyHandler.RECEIVE_MESSAGE:
                    receiveMessage(msg);
                    break;
            }
        }
    };

    private void receiveMessage(Message msg) {
        byte[] readBuf = (byte[]) msg.obj;
        // construct a string from the valid bytes in the buffer
        String text = new String(readBuf, 0, msg.arg1);
        Log.d("receiveMessage", text);
        Gson gson = new Gson();
        Info info = gson.fromJson(text, Info.class);
        switch (info.getType()) {
            case Info.TYPE_INVITE:
                showInvitedDialog(info);
                break;
            case Info.TYPE_AGREE:
                ToastUtils.showShort(getActivity(), "对方应战");
                Intent intent = new Intent(getActivity(), BTGameActivity.class);
                intent.putExtra(BTGameActivity.SIZE, normalChessBoard.isChecked() ? 15 : 12);
                intent.putExtra(BTGameActivity.FIRST_HAND, whiteFirst.isChecked());
                intent.putExtra(BTGameActivity.IS_YOU_FIRST, whiteFirst.isChecked() != whitePice.isChecked());
                startActivity(intent);
                break;
            case Info.TYPE_REFUSE:
                ToastUtils.showShort(getActivity(), "对方拒绝");
                break;
        }
    }

    public void showNormalDialog(String msg) {
        final NormalDialog dialog = new NormalDialog(this);
        dialog.isTitleShow(false)//
                .bgColor(Color.parseColor("#383838"))//
                .cornerRadius(5)//
                .content(msg)//
                .btnNum(1)
                .btnText("确定")
                .contentGravity(Gravity.CENTER)//
                .contentTextColor(Color.parseColor("#ffffff"))//
                .dividerColor(Color.parseColor("#222222"))//
                .btnTextSize(15.5f, 15.5f)//
                .btnTextColor(Color.parseColor("#ffffff"), Color.parseColor("#ffffff"))//
                .btnPressColor(Color.parseColor("#2B2B2B"))//
                .widthScale(0.85f)//
                .show();

        dialog.setOnBtnClickL(
                new OnBtnClickL() {
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                });
    }

    private void sendInviteInfo() {
        Gson gson = new Gson();
        Invite invite = new Invite();
        invite.setFromName(BTUtils.bluetoothAdapter.getName());
        invite.setBoardSize(normalChessBoard.isChecked() ? 15 : 12);
        invite.setWhiteFirst(whiteFirst.isChecked());
        invite.setObjectPieceWhite(whitePice.isChecked());

        Info info = new Info();
        info.setInfo(gson.toJson(invite));
        info.setType(Info.TYPE_INVITE);

        Log.d("sendInviteInfo", gson.toJson(info));

        LinkService.getInstance().write(gson.toJson(info).getBytes());
    }

}
