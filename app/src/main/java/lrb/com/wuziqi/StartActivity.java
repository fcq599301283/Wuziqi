package lrb.com.wuziqi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import lrb.com.wuziqi.btGame.BTGameSetting;

/**
 * Created by FengChaoQun
 * on 2017/5/4
 * 开始界面
 */

public class StartActivity extends Activity {

    private Button local, net, manMachine;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_satrt);

        local = (Button) findViewById(R.id.local);
        net = (Button) findViewById(R.id.net);
        manMachine = (Button) findViewById(R.id.manMachine);

        local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, LocalActivity.class));
            }
        });

        net.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, BTGameSetting.class));
            }
        });

        manMachine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, AIActivity.class));
            }
        });

    }
}
