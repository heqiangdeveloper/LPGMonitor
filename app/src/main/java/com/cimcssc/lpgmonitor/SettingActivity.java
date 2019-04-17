package com.cimcssc.lpgmonitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author october
 */
public class SettingActivity extends BaseActivity {
    @BindView(R.id.back_iv)
    ImageView back_Iv;
    @BindView(R.id.save_settings_bt)
    Button save_settings_Bt;
    @BindView(R.id.stop_flow_et)
    EditText stop_flow_Et;
    @BindView(R.id.stop_pump_pressure_et)
    EditText stop_pump_pressure_Et;
    @BindView(R.id.cavitation_pressure_et)
    EditText cavitation_pressure_Et;
    @BindView(R.id.max_liquid_level_et)
    EditText max_liquid_level_Et;
    @BindView(R.id.max_tank_pressure_et)
    EditText max_tank_pressure_Et;
    @BindView(R.id.delay_et)
    EditText delay_Et;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parameter_setting);
        ButterKnife.bind(this);

        sp = getSharedPreferences("configs",MODE_PRIVATE);
        readConfigs();

        addWatcher(stop_flow_Et);
        addWatcher(stop_pump_pressure_Et);
        addWatcher(cavitation_pressure_Et);
        addWatcher(max_liquid_level_Et);
        addWatcher(max_tank_pressure_Et);
        addWatcher(delay_Et);
    }

    public void readConfigs(){
        stop_flow_Et.setText(sp.getString("stop_flow",""));
        stop_pump_pressure_Et.setText(sp.getString("stop_pump_pressure",""));
        cavitation_pressure_Et.setText(sp.getString("cavitation_pressure",""));
        max_tank_pressure_Et.setText(sp.getString("max_tank_pressure",""));
        max_liquid_level_Et.setText(sp.getString("max_liquid_level",""));
        delay_Et.setText(sp.getString("delay",""));
    }

    public void addWatcher(TextView tv){
        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(stop_flow_Et.getText().toString().trim().length() != 0 &&
                        stop_pump_pressure_Et.getText().toString().trim().length() != 0 &&
                        cavitation_pressure_Et.getText().toString().trim().length() != 0 &&
                        max_liquid_level_Et.getText().toString().trim().length() != 0 &&
                        max_tank_pressure_Et.getText().toString().trim().length() != 0 &&
                        delay_Et.getText().toString().trim().length() != 0){
                    save_settings_Bt.setClickable(true);
                    save_settings_Bt.setBackground(getResources().getDrawable(R.drawable.shape_login_button_on));
                }else{
                    save_settings_Bt.setClickable(false);
                    save_settings_Bt.setBackground(getResources().getDrawable(R.drawable.shape_login_button_off));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @OnClick({R.id.back_iv,R.id.save_settings_bt})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.back_iv:
                finish();
                break;
            case R.id.save_settings_bt:
                if(save_settings_Bt.isClickable()){
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("stop_flow",stop_flow_Et.getText().toString());
                    editor.putString("stop_pump_pressure",stop_pump_pressure_Et.getText().toString());
                    editor.putString("cavitation_pressure",cavitation_pressure_Et.getText().toString());
                    editor.putString("max_liquid_level",max_liquid_level_Et.getText().toString());
                    editor.putString("max_tank_pressure",max_tank_pressure_Et.getText().toString());
                    editor.putString("delay",delay_Et.getText().toString());
                    editor.commit();
                    Toast.makeText(SettingActivity.this,"参数设置保存成功！",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}
