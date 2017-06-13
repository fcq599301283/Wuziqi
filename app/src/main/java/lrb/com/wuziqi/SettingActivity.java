package lrb.com.wuziqi;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.RadioButton;

/**
 * Created by FengChaoQun
 * on 2017/5/4
 * 设置界面
 */

public class SettingActivity extends Activity {

    private RadioButton whiteFirst, blackFirst, normalChessBoard, smallChessBoard;

    private SettingUtils settingUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    private void initView() {

        settingUtils = new SettingUtils(this);

        whiteFirst = (RadioButton) findViewById(R.id.whiteFirst);
        blackFirst = (RadioButton) findViewById(R.id.blackFirst);
        normalChessBoard = (RadioButton) findViewById(R.id.normal);
        smallChessBoard = (RadioButton) findViewById(R.id.small);

        if (settingUtils.isWhiteFirst()) {
            whiteFirst.setChecked(true);
        } else {
            blackFirst.setChecked(true);
        }

        if (settingUtils.getChessBoardSize() == 15) {
            normalChessBoard.setChecked(true);
        } else {
            smallChessBoard.setChecked(true);
        }

        whiteFirst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingUtils.setFirstHand(isChecked);
            }
        });

        blackFirst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingUtils.setFirstHand(!isChecked);
            }
        });

        normalChessBoard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settingUtils.setChessBoardSize(15);
                }
            }
        });

        smallChessBoard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settingUtils.setChessBoardSize(12);
                }
            }
        });

    }
}
