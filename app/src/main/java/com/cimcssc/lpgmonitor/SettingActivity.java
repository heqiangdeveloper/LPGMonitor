package com.cimcssc.lpgmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author october
 */
public class SettingActivity extends BaseActivity {
    @BindView(R.id.gateway_setting)
    TextView gateway_setting_Tv;
    @BindView(R.id.unloading_setting)
    TextView unloading_setting_Tv;
    @BindView(R.id.downtime_setting)
    TextView downtime_setting_Tv;
    @BindView(R.id.password_safety)
    TextView password_safety_Tv;
    @BindView(R.id.back_iv)
    ImageView back_Iv;

    private String type = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.back_iv,R.id.gateway_setting,R.id.unloading_setting,R.id.downtime_setting,
            R.id.password_safety})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.back_iv:
                finish();
                break;
            case R.id.gateway_setting:
                type = "gateway";
                break;
            case R.id.unloading_setting:
                type = "unloading";
                break;
            case R.id.downtime_setting:
                type = "downtime";
                break;
            case R.id.password_safety:
                type = "password";
                break;
        }
        if(v.getId() != R.id.back_iv){
            Intent i = new Intent(SettingActivity.this,SettingSubActivity.class);
            i.putExtra("type",type);
            startActivity(i);
        }
    }
}
