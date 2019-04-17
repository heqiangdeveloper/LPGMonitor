package com.cimcssc.lpgmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by qianghe on 2019/4/17.
 */
public class LoginActivity extends BaseActivity {
    @BindView(R.id.user_et)
    EditText user_Et;
    @BindView(R.id.password_cb)
    CheckBox password_Cb;
    @BindView(R.id.password_et)
    EditText password_Et;
    @BindView(R.id.login_bt)
    Button login_Bt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        password_Cb.setOnCheckedChangeListener(new PasswordChange());
        addWatcher(user_Et);
        addWatcher(password_Et);
    }

    class PasswordChange implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                //密码由不可见变为可见
                password_Et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }else{
                //密码由可见变为不可见
                password_Et.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            password_Et.setSelection(password_Et.getText().toString().length());
        }
    }

    public void addWatcher(TextView tv){
        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(user_Et.getText().toString().trim().length() != 0 &&
                        password_Et.getText().toString().trim().length() != 0){
                    login_Bt.setClickable(true);
                    login_Bt.setBackground(getResources().getDrawable(R.drawable.shape_login_button_on));
                }else{
                    login_Bt.setClickable(false);
                    login_Bt.setBackground(getResources().getDrawable(R.drawable.shape_login_button_off));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @OnClick({R.id.login_bt})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.login_bt:
                if(login_Bt.isClickable()){
                    String name = user_Et.getText().toString().trim();
                    String password = password_Et.getText().toString().trim();
                    if(name.equals("admin") && password.equals("1234")){
                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        finish();
                    }else{
                        Toast.makeText(LoginActivity.this,"用户名或密码不正确，请重新输入！",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}
