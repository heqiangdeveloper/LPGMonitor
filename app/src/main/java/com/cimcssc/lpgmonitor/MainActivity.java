package com.cimcssc.lpgmonitor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.serial.SerialPort;
import com.utils.CRC16;
import com.utils.Config;
import com.utils.SelectTask;
import com.utils.SendBytes;
import com.utils.TransferValue;
import com.utils.USBPrinter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author october
 */
public class MainActivity extends BaseActivity {
    @BindView(R.id.stop_alarm_rl)
    RelativeLayout stop_alarm_Rl;
    @BindView(R.id.print_rl)
    RelativeLayout print_Rl;
    @BindView(R.id.pressure_ll)
    LinearLayout pressure_Ll;
    @BindView(R.id.alarm_rl)
    RelativeLayout alarm_Rl;

    @BindView(R.id.action_tv1)
    TextView action_Tv1;
    @BindView(R.id.action_tv2)
    TextView action_Tv2;
    @BindView(R.id.settings_iv)
    ImageView settings_Iv;
    @BindView(R.id.pump_behind_pressure_tv)
    TextView pump_behind_pressure_Tv;
    @BindView(R.id.pump_front_pressure_tv)
    TextView pump_front_pressure_Tv;
    @BindView(R.id.level_tv)
    TextView level_Tv;
    @BindView(R.id.stop_alarm_bt)
    Button stop_alarm_Bt;
    @BindView(R.id.alarm_tv)
    TextView alarm_Tv;

    //byte类型最大只能存放127（对应ox7F）
    //private byte[] bytes =
    //        new byte[]{0x3C,0x30,0x1C,0x07,0x02,0x05,0x3E,0x7F};
    private SerialPort serialttyS1;
    private SerialPort serialttyUSB3;
    private InputStream ttyS1InputStream;
    private OutputStream ttyS1OutputStream;
    private InputStream ttyUSB3InputStream;
    private OutputStream ttyUSB3OutputStream;
    //String readDatas = null;
    StringBuffer sb = new StringBuffer();

    private Context mContext = MainActivity.this;
    private String receiveData = "";
    private boolean isCRCValid = false;
    private MediaPlayer mediaPlayer;
    private int number = 0;
    private StringBuffer warningBuffer = new StringBuffer();
    private boolean isAbnormal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();
        //初始化串口
        init_serial();
        //初始化usb串口
        init_usb_serial(Config.usb_pathname,Config.usb_baudrate);

        //接收数据
        receiveData();

        initMediaPlayer();

        byte[] myBytes = new byte[]{
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x01, 0x00, 0x01, 0x3B
        };
        int[] is = CRC16.getCrc16(myBytes);

    }

    public void initView(){
        print_Rl.setVisibility(View.VISIBLE);
        stop_alarm_Rl.setVisibility(View.GONE);

        pressure_Ll.setVisibility(View.VISIBLE);
        alarm_Rl.setVisibility(View.GONE);
    }

    @OnClick({R.id.action_tv1,R.id.action_tv2,R.id.settings_iv,R.id.print_tv,
            R.id.stop_alarm_bt})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.print_tv:
                /*if (mediaPlayer != null && mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }*/
                USBPrinter printer = USBPrinter.getInstance();
                USBPrinter.initPrinter(MainActivity.this);
                printer.print("hello world!!");

                break;
            case R.id.settings_iv:
                number = 1;
                Dialog("请输入密码",number);
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
                    sendData(07);//参照SendBytes类

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
                //准备
                //number = 2;
                if(action_Tv2.getText().equals(getResources().getString(R.string.ready_label))){
                    Config.current_action_flag = 2;
                    Dialog(getResources().getString(R.string.ready_dialog_title),Config.current_action_flag);
                }
                //待机
                else if(action_Tv2.getText().equals(getResources().getString(R.string.wait_on_label))){
                    Config.current_action_flag = 3;
                    Dialog(getResources().getString(R.string.wait_on_dialog_title), Config.current_action_flag);
                }

                break;

            case R.id.stop_alarm_bt:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
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
                            Log.d("receivedata","receive data is: " + receiveData);
                            if(!receiveData.contains(" ")){
                                receiveData = transferString(receiveData);
                            }
                            sb = new StringBuffer();

                            String[] strs = receiveData.split(" ");

                            Config.FRAME_HEADER_FEEDBACK = Integer.parseInt(strs[0],16);//将一个十六进制数转为十进制
                            Config.COMMAND_WORD_FEEDBACK = Integer.parseInt(strs[1],16);
                            Config.DATA_LENGTH_FEEDBACK = Integer.parseInt(strs[2],16);
                            //泵前压力
                            Config.PUMP_FRONT_FEEDBACK =
                                    TransferValue.getDoubleValue(strs[3],strs[4]);//
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
                            Config.DEVICE_STATUS_FEEDBACK1 = Integer.parseInt(strs[16], 16);
                            Config.DEVICE_STATUS_FEEDBACK2 = Integer.parseInt(strs[17], 16);
                            Config.DEVICE_STATUS_FEEDBACK3 = Integer.parseInt(strs[18], 16);

                            //设备状态
                            final String status1 =
                                    Integer.toBinaryString(Config.DEVICE_STATUS_FEEDBACK1);
                            String status2 =
                                    Integer.toBinaryString(Config.DEVICE_STATUS_FEEDBACK2);
                            String status3 =
                                    Integer.toBinaryString(Config.DEVICE_STATUS_FEEDBACK3);
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
                            if(ints[0] == Integer.parseInt(strs[19],16) &&
                                    ints[1] == Integer.parseInt(strs[20],16)){
                                isCRCValid = true;
                            }else{
                                isCRCValid = false;
                            }


                            runOnUiThread(new Runnable() {
                                public void run() {
                                    //数据校验通过
                                    if(isCRCValid){
                                        pump_behind_pressure_Tv.setText(Config.PUMP_BEHIND_FEEDBACK + "");
                                        pump_front_pressure_Tv.setText(Config.PUMP_FRONT_FEEDBACK + "");
                                        level_Tv.setText(Config.LEVEL_FEEDBACK + "");

                                        //检查从机是否进入 卸液准备
                                        Config.current_action_flag = 3;
                                        if(Config.current_action_flag == 3){
                                            warningBuffer = new StringBuffer();
                                            isAbnormal = false;
                                            //1.检查压力值，液位
                                            if(Config.PUMP_FRONT_FEEDBACK > 100){
                                                isAbnormal = true;
                                                warningBuffer.append("泵前压力值异常\n");
                                            }
                                            if(Config.PUMP_BEHIND_FEEDBACK > 100){
                                                isAbnormal = true;
                                                warningBuffer.append("泵后压力值异常\n");
                                            }
                                            if(Config.LEVEL_FEEDBACK > 100){
                                                isAbnormal = true;
                                                warningBuffer.append("液位值异常\n");
                                            }
                                            if(Config.FLOWMETER_TEMPERATURE_FEEDBACK > 100){
                                                isAbnormal = true;
                                                warningBuffer.append("流量计温度异常\n");
                                            }
                                            if(Config.FLOWMETER_UNLOADING_QUANTITY_FEEDBACK > 100){
                                                isAbnormal = true;
                                                warningBuffer.append("流量计卸液量异常\n");
                                            }
                                            if(Config.FLOWMETER_RATE_FEEDBACK > 100){
                                                isAbnormal = true;
                                                warningBuffer.append("流量计瞬时流量异常\n");
                                            }

                                            //2.从机状态
                                            if(Config.STATUS_FEEDBACK !=  1){
                                                isAbnormal = true;
                                                //warningBuffer.append("从机出现异常\n");
                                            }
                                            //3.状态位
                                            //0-7 位
                                            //第 0 位
                                            /*if((Config.DEVICE_STATUS_FEEDBACK1 & 1) == 0) {
                                                isAbnormal = true;
                                                warningBuffer.append("433无线模块工作状态异常\n");
                                            }

                                            //第 1 位
                                            if((Config.DEVICE_STATUS_FEEDBACK1 & 2) == 0) {
                                                isAbnormal = true;
                                                warningBuffer.append("碰撞传感器工作状态异常\n");
                                            }

                                            ////第 2 位
                                            if((Config.DEVICE_STATUS_FEEDBACK1 & 4) == 1) {
                                                isAbnormal = true;
                                                warningBuffer.append("LPG车发生碰撞事故，触发碰撞报警\n");
                                            }

                                            //第 3 位
                                            /*if((Config.DEVICE_STATUS_FEEDBACK1 & 8) == 0) {
                                                warningBuffer.append("液位传感器工作状态异常\n");
                                            }

                                            //第 4 位
                                            if((Config.DEVICE_STATUS_FEEDBACK1 & 16) == 0) {
                                                warningBuffer.append("泵后压力传感器工作状态异常\n");
                                            }

                                            //第 5 位
                                            if((Config.DEVICE_STATUS_FEEDBACK1 & 32) == 0) {
                                                warningBuffer.append("泵前压力传感器工作状态异常\n");
                                            }*/

                                            //第 6 位
                                           /* if((Config.DEVICE_STATUS_FEEDBACK1 & 64) == 1) {
                                                isAbnormal = true;
                                                warningBuffer.append("空气中燃气含量超标，触发探头2的报警\n");
                                            }*/

                                            //第 7 位
                                            /*if((Config.DEVICE_STATUS_FEEDBACK1 & 128) == 1) {
                                                isAbnormal = true;
                                                warningBuffer.append("空气中燃气含量超标，触发探头1的报警。\n");
                                            }

                                            //8-15位
                                            //第 8 位
                                            if((Config.DEVICE_STATUS_FEEDBACK2 & 1) == 0) {
                                                isAbnormal = true;
                                                warningBuffer.append("流量计与采集板通讯异常。\n");
                                            }*/

                                            //第 9 位
                                            /*if((Config.DEVICE_STATUS_FEEDBACK2 & 2) == 0) {
                                                warningBuffer.append("碰撞传感器工作状态异常\n");
                                            }

                                            ////第 10 位
                                            if((Config.DEVICE_STATUS_FEEDBACK2 & 4) == 1) {
                                                warningBuffer.append("LPG车发生碰撞事故，触发碰撞报警\n");
                                            }

                                            //第 11 位
                                            if((Config.DEVICE_STATUS_FEEDBACK2 & 8) == 0) {
                                                warningBuffer.append("液位传感器工作状态异常\n");
                                            }*/

                                            //第 12 位
                                            /*if((Config.DEVICE_STATUS_FEEDBACK2 & 16) == 0) {
                                                isAbnormal = true;
                                                warningBuffer.append("刹车未制动\n");
                                            }

                                            //第 13 位
                                            if((Config.DEVICE_STATUS_FEEDBACK2 & 32) == 1) {
                                                isAbnormal = true;
                                                warningBuffer.append("门意外开启报警被触发\n");
                                            }

                                            //第 14 位
                                            if((Config.DEVICE_STATUS_FEEDBACK2 & 64) == 1) {
                                                isAbnormal = true;
                                                warningBuffer.append("尾操作箱门打开\n");
                                            }

                                            //第 15 位
                                            if((Config.DEVICE_STATUS_FEEDBACK2 & 128) == 1) {
                                                isAbnormal = true;
                                                warningBuffer.append("充装枪已经离开枪座。\n");
                                            }*/
                                        }

                                        //如果有异常
                                        if(isAbnormal){
//                                            Intent i = new Intent(MainActivity.this,AlarmActivity.class);
//                                            i.putExtra("msg",warningBuffer.toString());
//                                            startActivity(i);

                                            alarm_Rl.setVisibility(View.VISIBLE);
                                            stop_alarm_Rl.setVisibility(View.VISIBLE);
                                            print_Rl.setVisibility(View.GONE);
                                            pressure_Ll.setVisibility(View.GONE);
                                            mediaPlayer.start();

                                            warningBuffer.append(warningBuffer);
                                            alarm_Tv.setText(warningBuffer.toString());
                                        }else {//无异常，准备就绪
                                            alarm_Rl.setVisibility(View.GONE);
                                            stop_alarm_Rl.setVisibility(View.GONE);
                                            print_Rl.setVisibility(View.VISIBLE);
                                            pressure_Ll.setVisibility(View.VISIBLE);

                                            action_Tv1.setText(getResources().getString(R.string.unloading_label));
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
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alarm);
        }catch (Exception e){

        }
    }

    public void Dialog(String title, final int number){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view1 = inflater.inflate(R.layout.ready_dailog_content,null);
        Button login_Bt = view1.findViewById(R.id.login_bt);
        ImageView back_Iv = (ImageView) view1.findViewById(R.id.back_iv);
        ImageView exit_Iv = (ImageView) view1.findViewById(R.id.exit_iv);
        TextView title_Tv = (TextView) view1.findViewById(R.id.title_tv);
        title_Tv.setText(title);
        final EditText password_Et = (EditText) view1.findViewById(R.id.password_et);
        password_Et.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
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

                    //开启 轮询 从机的状态
                    SelectTask mySelectTask = new SelectTask(ttyS1OutputStream,7);
                    //创建定时器对象
                    Timer t= new Timer();
                    //在0秒后执行MyTask类中的run方法,后面每20秒跑一次
                    t.schedule(mySelectTask, 0,1000 * 10);

                    if (Config.current_action_flag == 1){
                        startActivity(new Intent(MainActivity.this,SettingActivity.class));
                    }
                    if (Config.current_action_flag == 2){
                        if(action_Tv2.getText().equals(getResources().getString(R.string.ready_label))){
                            action_Tv2.setVisibility(View.GONE);
                            action_Tv1.setText(getResources().getString(R.string.unloading_ready_label));

                        }else{
                            action_Tv1.setVisibility(View.VISIBLE);
                            action_Tv2.setVisibility(View.VISIBLE);
                            action_Tv1.setText(getResources().getString(R.string.wait_on_label));
                            action_Tv1.setText(getResources().getString(R.string.ready_label));
                        }
                    }
                    if (Config.current_action_flag == 3){
                        //查询从机是否准备好
                        sendData(01);
                    }
                }else{
                    password_Et.setText("");
                    Toast.makeText(mContext,"密码错误，请重新输入！",Toast.LENGTH_SHORT).show();
                }
                /*switch (view.getId()){
                    case R.id.settings_iv:
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                }*/
            }
        });
        exit_Iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close_serial();
    }
}
