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
import com.utils.CRC16;

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
    private byte[] bytes =
            new byte[]{0x3C,0x30,0x1C,0x07,0x02,0x05,0x3E,0x7F};
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
                //发送数据
                sendData();

                //卸液准备
                if(action_Tv1.getText().equals(getResources().getString(R.string.unloading_ready_label))){
                        action_Tv1.setText(getResources().getString(R.string.unloading_label));
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
            serialttyS1 = new SerialPort(new File("/dev/ttyS2"),115200,0);
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

            if(ttyS1InputStream != null){
                ttyS1InputStream.close();
            }
        }catch (Exception e){

        }
    }
    //发送数据
    public void sendData(){
        /* 串口发送字节 */
        if(ttyS1OutputStream != null){
            byte part1 = 0x3C;
            byte part2 = 0x30;
            byte part3 = 0x01;
            byte part4 = 0x07;
            byte part5 = 0x3C;
            byte[] bytes = new byte[]{part1,part2,part3,part4,part5};

            int[] crc16 = CRC16.getCrc16(bytes);//10进制
            int c1 = crc16[0];//42  0x2a
            int c2 = crc16[1];//36

            byte part8 = 0x3E;
            byte[] bytes2 = new byte[]{part1,part2,part3,part4,part5,(byte)c1,(byte)c2,part8};

            try{
                ttyS1OutputStream.write(bytes2);
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
                try{
                    while (ttyS1InputStream != null && (size = ttyS1InputStream.read(buffer)) > 0) {
                        receiveData = new String(buffer,0,size);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d("receivedata","receive data is: " + sb.toString());
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
