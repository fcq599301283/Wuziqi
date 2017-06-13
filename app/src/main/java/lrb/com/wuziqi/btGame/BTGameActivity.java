package lrb.com.wuziqi.btGame;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.flyco.animation.ZoomEnter.ZoomInEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.google.gson.Gson;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.OnBoomListener;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import lrb.com.wuziqi.ChessBoard;
import lrb.com.wuziqi.R;
import lrb.com.wuziqi.data.ChessBoardPoint;
import lrb.com.wuziqi.data.Info;
import lrb.com.wuziqi.utils.ToastUtils;

/**
 * Created by FengChaoQun
 * on 2017/5/5
 * 蓝牙对战界面
 */

public class BTGameActivity extends Activity implements ChessBoard.OnPlay, ChessBoard.onGameOver {

    public static final String SIZE = "SIZE";
    public static final String FIRST_HAND = "FIRST_HAND";
    public static final String IS_YOU_FIRST = "IS_YOU_FIRST";

    private ChessBoard chessBoard;
    private TextView turn;

    private int size;
    private boolean isWhiteFirst;
    private boolean isYourFirst;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_game);
        initView();
    }

    private void initView() {
        chessBoard = (ChessBoard) findViewById(R.id.chessBoard);
        turn = (TextView) findViewById(R.id.turn);

        size = getIntent().getIntExtra(SIZE, 15);
        isWhiteFirst = getIntent().getBooleanExtra(FIRST_HAND, true);
        isYourFirst = getIntent().getBooleanExtra(IS_YOU_FIRST, true);

        initChessBoard();

        MyHandler.getInstance().register(onStateChange, true);

        BoomMenuButton rightBmb = (BoomMenuButton) findViewById(R.id.action_bar_right_bmb);
        rightBmb.setButtonEnum(ButtonEnum.Ham);
        rightBmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_2);
        rightBmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_2);
        rightBmb.addBuilder(getNormalBuilder("重新开始", R.color.green2));
        rightBmb.addBuilder(getNormalBuilder("退出", R.color.colorAccent));

        rightBmb.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {
                switch (index) {
                    case 0:
                        reStart();
                        break;
                    case 1:
                        showExitDialog();
                        break;
                }
            }

            @Override
            public void onBackgroundClick() {

            }

            @Override
            public void onBoomWillHide() {

            }

            @Override
            public void onBoomDidHide() {

            }

            @Override
            public void onBoomWillShow() {

            }

            @Override
            public void onBoomDidShow() {

            }
        });
    }

    private HamButton.Builder getNormalBuilder(String buttonText, int buttonColor) {
        return new HamButton.Builder()
                .normalColorRes(buttonColor)
                .highlightedColor(Color.DKGRAY)
                .textSize(20)
                .textGravity(Gravity.CENTER_VERTICAL)
                .normalText(buttonText)
                .pieceColor(Color.WHITE);
    }

    private void reStart() {
        if (chessBoard.isGamePlaying()) {
            ToastUtils.showShort(getActivity(), "正在对局，不能重新开始");
        } else {
            initChessBoard();
            chessBoard.restart();
            Info i = new Info();
            i.setType(Info.READY);
            Gson gson = new Gson();
            LinkService.getInstance().write(gson.toJson(i).getBytes());
        }
    }

    private void initChessBoard() {
        chessBoard.setMAX_LINE(size);
        chessBoard.setWhiteTurn(isWhiteFirst);
        chessBoard.setMyTurn(isYourFirst);
        chessBoard.setNeedWait(true);
        chessBoard.setOnPlay(this);
        chessBoard.setGameOver(this);

        if (isYourFirst) {
            turn.setText("你的回合");
        } else {
            turn.setText("对手回合");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyHandler.getInstance().register(onStateChange, false);
    }

    private MyHandler.OnStateChange onStateChange = new MyHandler.OnStateChange() {
        @Override
        public void onChange(Message msg) {
            switch (msg.what) {
                case MyHandler.connectLose:
                    try {
                        NoticeDialog("对方离开了");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

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
            case Info.PLAY:
                ChessBoardPoint chessBoardPoint = gson.fromJson(info.getInfo(), ChessBoardPoint.class);
                chessBoard.addPoint(new Point(chessBoardPoint.getX(), chessBoardPoint.getY()));
                turn.setText("你的回合");
                break;
            case Info.READY:
                chessBoard.setReady(true);
                break;
        }
    }

    public Activity getActivity() {
        return this;
    }

    @Override
    public void onPlay(Point point) {
        Gson gson = new Gson();
        Info info = new Info();
        info.setType(Info.PLAY);
        ChessBoardPoint c = new ChessBoardPoint(point.x, point.y);
        info.setInfo(gson.toJson(c));
        LinkService.getInstance().write(gson.toJson(info).getBytes());
        turn.setText("对手回合");
    }

    @Override
    public void onWhiteWin() {
        showWinDialog("白子胜利!");
    }

    @Override
    public void onBlackWin() {
        showWinDialog("黑子胜利!");
    }

    @Override
    public void onTie() {
        showWinDialog("平局!");
    }

    private void showWinDialog(String text) {

        chessBoard.setReady(false);

        final NormalDialog dialog = new NormalDialog(this);
        dialog.isTitleShow(false)//
                .bgColor(Color.parseColor("#383838"))//
                .cornerRadius(5)//
                .content(text)
                .btnText("查看棋盘", "再来一局")
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
                reStart();
                dialog.dismiss();
            }
        });
    }

    private void showExitDialog() {
        final NormalDialog dialog = new NormalDialog(this);
        dialog.isTitleShow(false)//
                .bgColor(Color.parseColor("#383838"))//
                .cornerRadius(5)//
                .content("你确定离开这局比赛?")
                .btnText("走了", "继续下棋")
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
                LinkService.getInstance().start();
                finish();
            }
        }, new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
            }
        });
    }

    private void NoticeDialog(String notice) {
        final NormalDialog dialog = new NormalDialog(this);
        dialog.isTitleShow(false)//
                .bgColor(Color.parseColor("#383838"))//
                .cornerRadius(5)//
                .content(notice)
                .btnText("确定")
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
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            showExitDialog();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
