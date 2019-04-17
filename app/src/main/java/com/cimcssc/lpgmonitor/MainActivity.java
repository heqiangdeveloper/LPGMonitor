package com.cimcssc.lpgmonitor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.serial.SerialPort;
import com.utils.Config;
import com.utils.SendBytes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author october
 */
public class MainActivity extends BaseActivity {
    @BindView(R.id.action_tv1)
    TextView action_Tv1;
    @BindView(R.id.action_tv2)
    TextView action_Tv2;
    @BindView(R.id.textView9)
    TextView TextView9;

    //byte类型最大只能存放127（对应ox7F）
    //private byte[] bytes =
    //        new byte[]{0x3C,0x30,0x1C,0x07,0x02,0x05,0x3E,0x7F};
    private SerialPort serialttyS1;
    private InputStream ttyS1InputStream;
    private OutputStream ttyS1OutputStream;
    String readDatas = null;
    StringBuffer sb = new StringBuffer();

    private Context mContext = MainActivity.this;
    private String receiveData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //初始化串口
        init_serial();

        //接收数据
        receiveData();
    }

    @OnClick({R.id.action_tv1,R.id.action_tv2})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.action_tv1:
                //发送数据 0x01 1（十进制）
                //0x02 2（十进制）
                //0x03 3（十进制）

                //卸液准备
                if(action_Tv1.getText().equals(getResources().getString(R.string.unloading_ready_label))){
                        //action_Tv1.setText(getResources().getString(R.string.unloading_label));
                    //发送  卸液准备
                    sendData(1);//十进制

                }
                //卸液
                else if(action_Tv1.getText().equals(getResources().getString(R.string.unloading_label))){

                }
                //卸液结束
                else if(action_Tv2.getText().equals(getResources().getString(R.string.ready_label))){

                }
                //故障
                else if(action_Tv2.getText().equals(getResources().getString(R.string.ready_label))){

                }
                break;
            case R.id.action_tv2:
                String title = "";
                //准备
                if(action_Tv2.getText().equals(getResources().getString(R.string.ready_label))){
                    title = getResources().getString(R.string.ready_dialog_title);
                }
                //待机
                else if(action_Tv2.getText().equals(getResources().getString(R.string.wait_on_label))){
                    title = getResources().getString(R.string.wait_on_dialog_title);
                }

                LayoutInflater inflater = LayoutInflater.from(mContext);
                View view1 = inflater.inflate(R.layout.ready_dailog_content,null);
                Button login_Bt = view1.findViewById(R.id.login_bt);
                ImageView back_Iv = (ImageView) view1.findViewById(R.id.back_iv);
                ImageView exit_Iv = (ImageView) view1.findViewById(R.id.exit_iv);
                TextView title_Tv = (TextView) view1.findViewById(R.id.title_tv);
                title_Tv.setText(title);
                final EditText password_Et = (EditText) view1.findViewById(R.id.password_et);
                final Dialog dialog = new AlertDialog.Builder(mContext)
                        //.setTitle("提示")
                        .setView(view1)
                        //.setMessage(mContext.getResources().getString(R.string
                        //.add_oil_dialog_title))
                        .setCancelable(false)
                        .create();
                dialog.show();

                login_Bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String password = password_Et.getText().toString().trim();
                        if(password.equals("")){
                            Toast.makeText(mContext,"请输入密码！",Toast.LENGTH_SHORT).show();
                        }else if(password.equals("1234")){
                            dialog.dismiss();
                            Toast.makeText(mContext,"登入成功！",Toast.LENGTH_SHORT).show();
                            if(action_Tv2.getText().equals(getResources().getString(R.string.ready_label))){
                                action_Tv2.setVisibility(View.GONE);
                                action_Tv1.setText(getResources().getString(R.string.unloading_ready_label));
                            }else{
                                action_Tv1.setVisibility(View.VISIBLE);
                                action_Tv2.setVisibility(View.VISIBLE);
                                action_Tv1.setText(getResources().getString(R.string.wait_on_label));
                                action_Tv1.setText(getResources().getString(R.string.ready_label));
                            }
                        }else{
                            password_Et.setText("");
                            Toast.makeText(mContext,"密码错误，请重新输入！",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                back_Iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                exit_Iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
        }
    }
    /* 打开串口 */
    private void init_serial(){
        try {
            Log.d("mylog","send a  data...");
            serialttyS1 = new SerialPort(new File(Config.pathname),Config.baudrate,0);
            ttyS1InputStream = serialttyS1.getInputStream();
            ttyS1OutputStream = serialttyS1.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //关闭串口
    private void close_serial(){
        try{
            if(ttyS1InputStream != null){
                ttyS1InputStream.close();
            }

            if(ttyS1OutputStream != null){
                ttyS1OutputStream.close();
            }
        }catch (Exception e){

        }
    }
    //发送数据
    public void sendData(int flag){
        /* 串口发送字节 */
        if(ttyS1OutputStream != null){
            byte[] sendCommandBytes = SendBytes.getSendBytes(flag);
            try{
                ttyS1OutputStream.write(sendCommandBytes);
            }catch (Exception e){

            }
        }
    }

    public void receiveData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                int size;
                byte[] buffer = new byte[1024];
                String s = "";
                try{
                    while (ttyS1InputStream != null && (size = ttyS1InputStream.read(buffer)) > 0) {
                        sb.append(new String(buffer,0,size));

                        s = sb.toString().trim();
                        if(s.startsWith("3C") && s.endsWith("3E")){
                            receiveData = sb.toString().trim();
                            sb = new StringBuffer();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Log.d("receivedata","receive data is: " + receiveData);

                                    String[] strs = receiveData.split(" ");

                                    Config.FRAME_HEADER_FEEDBACK = Integer.parseInt(strs[0],16);
                                    Config.COMMAND_WORD_FEEDBACK = Integer.parseInt(strs[1],16);
                                    Config.DATA_LENGTH_FEEDBACK = Integer.parseInt(strs[2],16);
                                    Config.PUMP_FRONT_FEEDBACK = Integer.parseInt(strs[3],16);
                                    Config.PUMP_BEHIND_FEEDBACK = Integer.parseInt(strs[4],16);
                                    Config.PUMP_BEHIND_FEEDBACK = Integer.parseInt(strs[5],16);
                                    Config.LEVEL_FEEDBACK = Integer.parseInt(strs[0],16);


                                    byte[] bytes = new byte[]{

                                    };
                                    //卸液
                                    if(receiveData.equals("2019")){
                                        action_Tv1.setText(getResources().getString(R.string.unloading_label));
                                    }
                                    //卸液结束
                                    else if(receiveData.equals("2020")){
                                        action_Tv1.setVisibility(View.VISIBLE);
                                        action_Tv2.setVisibility(View.VISIBLE);
                                        action_Tv2.setText(getResources().getString(R.string.unloading_end_label));
                                        action_Tv1.setText(getResources().getString(R.string.wait_on_label));
                                    }
                                }
                            });
                        }
                    }
                }catch (Exception e){

                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close_serial();
    }
}
