package lrb.com.wuziqi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

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

/**
 *modified by FengChaoQun on 2017/5/8 14:47
 * description:优化代码
 * 本地双人对战界面
 */
public class LocalActivity extends AppCompatActivity {

    private ChessBoard chessBoard;
    private int SETTING_CODE = 100;
    private SettingUtils settingUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        chessBoard = (ChessBoard) findViewById(R.id.chessBoard);

        initView();

    }

    private void initView() {
        BoomMenuButton rightBmb = (BoomMenuButton) findViewById(R.id.action_bar_right_bmb);
        rightBmb.setButtonEnum(ButtonEnum.Ham);
        rightBmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_4);
        rightBmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_4);
        rightBmb.addBuilder(getNormalBuilder("设置", R.color.vi_blue));
        rightBmb.addBuilder(getNormalBuilder("悔棋", R.color.g0));
        rightBmb.addBuilder(getNormalBuilder("重新开始", R.color.green2));
        rightBmb.addBuilder(getNormalBuilder("退出", R.color.colorAccent));

        settingUtils = new SettingUtils(this);
        chessBoard.setWhiteTurn(settingUtils.isWhiteFirst());
        chessBoard.setMAX_LINE(settingUtils.getChessBoardSize());

        rightBmb.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {
                switch (index) {
                    case 0:
                        if (chessBoard.isGamePlaying()) {
                            showSettingDialog();
                        } else {
                            gotoSettingActivity();
                        }
                        break;
                    case 1:
                        chessBoard.regret();
                        break;
                    case 2:
                        chessBoard.restart();
                        break;
                    case 3:
                        finish();
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

        chessBoard.setGameOver(new ChessBoard.onGameOver() {
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

    private void showWinDialog(String text) {
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
                chessBoard.restart();
                dialog.dismiss();
            }
        });
    }

    private void showSettingDialog() {
        final NormalDialog dialog = new NormalDialog(this);
        dialog.isTitleShow(false)//
                .bgColor(Color.parseColor("#383838"))//
                .cornerRadius(5)//
                .content("对局正在进行，你确定要离开比赛?")
                .btnText("继续比赛", "去设置")
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
                gotoSettingActivity();
            }
        });
    }

    private void gotoSettingActivity() {
        startActivityForResult(new Intent(LocalActivity.this, SettingActivity.class), SETTING_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTING_CODE) {
            chessBoard.setWhiteTurn(settingUtils.isWhiteFirst());
            chessBoard.setMAX_LINE(settingUtils.getChessBoardSize());
            chessBoard.restart();
        }
    }
}
