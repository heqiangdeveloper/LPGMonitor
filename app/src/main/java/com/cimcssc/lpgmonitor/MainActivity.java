package com.cimcssc.lpgmonitor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.utils.Config;
import com.utils.SendBytes;
import com.utils.TransferValue;

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
    @BindView(R.id.settings_iv)
    ImageView settings_Iv;
    @BindView(R.id.pump_behind_pressure_tv)
    TextView pump_behind_pressure_Tv;
    @BindView(R.id.pump_front_pressure_tv)
    TextView pump_front_pressure_Tv;
    @BindView(R.id.level_tv)
    TextView level_Tv;

    //byte类型最大只能存放127（对应ox7F）
    //private byte[] bytes =
    //        new byte[]{0x3C,0x30,0x1C,0x07,0x02,0x05,0x3E,0x7F};
    private SerialPort serialttyS1;
    private SerialPort serialttyUSB3;
    private InputStream ttyS1InputStream;
    private OutputStream ttyS1OutputStream;
    private InputStream ttyUSB3InputStream;
    private OutputStream ttyUSB3OutputStream;
    String readDatas = null;
    StringBuffer sb = new StringBuffer();

    private Context mContext = MainActivity.this;
    private String receiveData = "";
    private boolean isCRCValid = false;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //初始化串口
        init_serial(Config.pathname,Config.baudrate);

        //初始化usb串口
        init_usb_serial(Config.usb_pathname,Config.usb_baudrate);

        //接收数据
        receiveData();

        //初始化MediaPlayer
        initMediaPlayer();

        //01 1C 00 09 01 05 01 00 01 01 00 10 00 09 01 3B
        byte[] myBytes = new byte[]{
                0x01, 0x1C, 0x00, 0x09, 0x01, 0x05, 0x01, 0x00, 0x01, 0x01,
                0x00, 0x10, 0x00, 0x09, 0x01, 0x3B
        };
        int[] is = CRC16.getCrc16(myBytes);
    }

    @OnClick({R.id.action_tv1,R.id.action_tv2,R.id.settings_iv,R.id.textView9})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.textView9:
                /*if(mediaPlayer != null && mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }*/

                //USBPrinter printer = USBPrinter.getInstance();
                //printer.initPrinter(MainActivity.this);
                //printer.print("Hello World!");

                sendUSBData();

                break;
            case R.id.settings_iv:
                startActivity(new Intent(MainActivity.this,SettingActivity.class));
                break;
            case R.id.action_tv1:
                //发送数据 0x01 1（十进制）
                //0x02 2（十进制）
                //0x03 3（十进制）

                sendData(1);//十进制

                //卸液准备
                if(action_Tv1.getText().equals(getResources().getString(R.string.unloading_ready_label))){
                        //action_Tv1.setText(getResources().getString(R.string.unloading_label));
                    //发送  卸液准备


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
    private void init_serial(String path,int baudrate){
        try {
            Log.d("mylog","send a  data...");
            serialttyS1 = new SerialPort(new File(path),baudrate,0);
            ttyS1InputStream = serialttyS1.getInputStream();
            ttyS1OutputStream = serialttyS1.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* 打开usb串口 */
    private void init_usb_serial(String path,int baudrate){
        try {
            Log.d("mylog","send a  data...");
            serialttyUSB3 = new SerialPort(new File(path),baudrate,0);
            ttyUSB3InputStream = serialttyUSB3.getInputStream();
            ttyUSB3OutputStream = serialttyUSB3.getOutputStream();
        } catch (Exception e) {
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

    //倒计时
    private void startCountDownTime(final long time) {
        final CountDownTimer timer = new CountDownTimer(time * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                /*if(isSpeaked){//客户端已经讲完话、
                    timeTv.setText("");
                    this.onFinish();
                }else {
                    timeTv.setText((millisUntilFinished / 1000) + "s");
                }*/
            }

            @Override
            public void onFinish() {
                //timeTv.setText("");
            }
        };
        timer.start();
    }

    //发送打印数据
    public void sendUSBData(){
        /* 串口发送字节 */
        if(ttyUSB3OutputStream != null){
            String s = "hello world!";

            //byte[] sendCommandBytes = new byte[]{0x0A,0x30,0x01,0x02};
            try{
                ttyUSB3OutputStream.write(s.getBytes());
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
                int[] ints;
                try{
                    while (ttyS1InputStream != null && (size = ttyS1InputStream.read(buffer)) > 0) {
                        sb.append(new String(buffer,0,size));

                        s = sb.toString().trim();
                        if(s.startsWith("3C") && s.endsWith("3E")){
                            receiveData = sb.toString().trim();
                            Log.d("receivedata","receive data is: " + receiveData);
                            if(!receiveData.contains(" ")){
                                receiveData = transferString(receiveData);
                            }
                            sb = new StringBuffer();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    String[] strs = receiveData.split(" ");

                                    Config.FRAME_HEADER_FEEDBACK = Integer.parseInt(strs[0],16);
                                    Config.COMMAND_WORD_FEEDBACK = Integer.parseInt(strs[1],16);
                                    Config.DATA_LENGTH_FEEDBACK = Integer.parseInt(strs[2],16);
                                    //泵前压力
                                    Config.PUMP_FRONT_FEEDBACK =
                                            TransferValue.getDoubleValue(strs[3],strs[4]);
                                    //泵后压力
                                    Config.PUMP_BEHIND_FEEDBACK =
                                            TransferValue.getDoubleValue(strs[5],strs[6]);
                                    //液位
                                    Config.LEVEL_FEEDBACK = TransferValue.getDoubleValue(strs[7], strs[8]);
                                    //流量计温度
                                    Config.FLOWMETER_TEMPERATURE_FEEDBACK =
                                            TransferValue.getDoubleValue(strs[9],strs[10]);
                                    //流量计卸液量
                                    Config.FLOWMETER_UNLOADING_QUANTITY_FEEDBACK =
                                            TransferValue.getDoubleValue(strs[11],strs[12]);
                                    //流量计瞬时流量
                                    Config.FLOWMETER_RATE_FEEDBACK =
                                            TransferValue.getDoubleValue(strs[13],strs[14]);

                                    //从机状态
                                    Config.STATUS_FEEDBACK = Integer.parseInt(strs[15],16);

                                    //设备状态
                                    Config.DEVICE_STATUS_FEEDBACK1 = Integer.parseInt(strs[16],16);
                                    Config.DEVICE_STATUS_FEEDBACK2 = Integer.parseInt(strs[17],16);
                                    Config.DEVICE_STATUS_FEEDBACK3 = Integer.parseInt(strs[18],16);

                                    //数据校验
                                    Config.CRC_FEEDBACK1 = Integer.parseInt(strs[19],16);
                                    Config.CRC_FEEDBACK2 = Integer.parseInt(strs[20],16);

                                    //帧尾
                                    Config.LEVEL_FEEDBACK = Integer.parseInt(strs[21],16);

                                    //开始数据校验
                                    byte[] b = new byte[]{
                                            (byte)Integer.parseInt(strs[3],16),
                                            (byte)Integer.parseInt(strs[4],16),

                                            (byte)Integer.parseInt(strs[5],16),
                                            (byte)Integer.parseInt(strs[6],16),

                                            (byte)Integer.parseInt(strs[7],16),
                                            (byte)Integer.parseInt(strs[8],16),

                                            (byte)Integer.parseInt(strs[9],16),
                                            (byte)Integer.parseInt(strs[10],16),

                                            (byte)Integer.parseInt(strs[11],16),
                                            (byte)Integer.parseInt(strs[12],16),

                                            (byte)Integer.parseInt(strs[13],16),
                                            (byte)Integer.parseInt(strs[14],16),

                                            (byte)Integer.parseInt(strs[15],16),

                                            (byte)Integer.parseInt(strs[16],16),
                                            (byte)Integer.parseInt(strs[17],16),
                                            (byte)Integer.parseInt(strs[18],16)
                                    };
                                    int[] ints = CRC16.getCrc16(b);
                                    //判断校验码是否正确
                                    if(ints[0] == Config.CRC_FEEDBACK1 && ints[1] == Config.CRC_FEEDBACK2){
                                        isCRCValid = true;
                                    }else{
                                        isCRCValid = false;
                                    }

                                    if(isCRCValid){
                                        pump_behind_pressure_Tv.setText(Config.PUMP_BEHIND_FEEDBACK + "");
                                        pump_front_pressure_Tv.setText(Config.PUMP_FRONT_FEEDBACK + "");
                                        level_Tv.setText(Config.LEVEL_FEEDBACK + "");

                                        //设备状态
                                        //
                                        String status1 =
                                                Integer.toBinaryString(Config.DEVICE_STATUS_FEEDBACK1);
                                        String status2 =
                                                Integer.toBinaryString(Config.DEVICE_STATUS_FEEDBACK2);
                                        String status3 =
                                                Integer.toBinaryString(Config.DEVICE_STATUS_FEEDBACK3);


                                        //报警
                                        if(mediaPlayer != null){
                                            //mediaPlayer.start();
                                        }

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
                                    }else{
                                        //do nothing
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

    //将String s每隔2个数字，就加一个空格
    public String transferString(String s){
        StringBuffer result = new StringBuffer();
        int count = 0;
        for(int k = 0; k <= s.length() - 1; k++){
            result.append(s.charAt(k));
            count++;
            if(count % 2 == 0){
                count = 0;
                result.append(" ");
            }
        }

        return result.toString();
    }

    public void initMediaPlayer(){
        try{
            mediaPlayer = MediaPlayer.create(MainActivity.this,R.raw.alarm);
            //mediaPlayer.start();
        }catch (Exception e){

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close_serial();
    }
}
