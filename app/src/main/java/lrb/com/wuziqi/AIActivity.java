package lrb.com.wuziqi;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.flyco.animation.ZoomEnter.ZoomInEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.OnBoomListener;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import lrb.com.wuziqi.utils.AIUtils;

/**
 * Created by FengChaoQun
 * on 2017/5/6
 * 人机对战界面
 *
 */

public class AIActivity extends Activity implements ChessBoard.OnPlay, ChessBoard.onGameOver {
    private ChessBoard chessBoard;
    private AIUtils aiUtils;
    private TextView title;
    private HamButton.Builder valueBuilder;
    private boolean showValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_game);
        initView();
    }

    private void initView() {
        chessBoard = (ChessBoard) findViewById(R.id.chessBoard);
        chessBoard.setMAX_LINE(15);
        chessBoard.setWhiteTurn(true);
        chessBoard.setMyTurn(true);
        chessBoard.setNeedWait(true);
        chessBoard.setOnPlay(this);
        chessBoard.setGameOver(this);
        aiUtils = new AIUtils(15);

        chessBoard.setDrawValues(showValue);

        title = (TextView) findViewById(R.id.turn);
        title.setText("人机对战");

        valueBuilder = getNormalBuilder("显示权值信息", R.color.vi_blue);

        BoomMenuButton rightBmb = (BoomMenuButton) findViewById(R.id.action_bar_right_bmb);
        rightBmb.setButtonEnum(ButtonEnum.Ham);
        rightBmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_3);
        rightBmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_3);
        rightBmb.addBuilder(getNormalBuilder("悔棋", R.color.green2));
        rightBmb.addBuilder(valueBuilder);
        rightBmb.addBuilder(getNormalBuilder("重新开始", R.color.colorAccent));

        rightBmb.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {
                switch (index) {
                    case 0:
                        regret();
                        break;
                    case 1:
                        clickOnShowValue();
                        break;
                    case 2:
                        restart();
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

    private void regret() {
        chessBoard.regret2();
        aiUtils.regret();
        chessBoard.setValue(aiUtils.getValue());
        chessBoard.invalidate();
    }

    private void clickOnShowValue() {
        showValue = !showValue;
        chessBoard.setDrawValues(showValue);
        chessBoard.invalidate();
        valueBuilder.normalText(showValue ? "关闭权值信息" : "显示权值信息");
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

    private void restart() {
        chessBoard.setWhiteTurn(true);
        chessBoard.setMyTurn(true);
        chessBoard.setReady(true);
        chessBoard.restart();
        aiUtils = new AIUtils(15);
    }

    @Override
    public void onPlay(Point point) {
        if (!chessBoard.isGamePlaying()) {
            return;
        }
        Point aiPoint = aiUtils.getAIPoint(point);
        chessBoard.setValue(aiUtils.getValue());
        chessBoard.addPoint(aiPoint);
        Log.d("AIActivity", "aiPoint:" + aiPoint);
    }

    @Override
    public void onWhiteWin() {
        showWinDialog("你赢了!");
    }

    @Override
    public void onBlackWin() {
        showWinDialog("你输了!");
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
                restart();
                dialog.dismiss();
            }
        });
    }
}
