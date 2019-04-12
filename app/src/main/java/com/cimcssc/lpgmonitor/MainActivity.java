package com.cimcssc.lpgmonitor;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.serial.SerialPort;
import com.utils.CRC16;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author october
 */
public class MainActivity extends BaseActivity {
    RelativeLayout wait_on_Rl;

    private ImageView imageView;
    private TextView TextView9;

    //byte类型最大只能存放127（对应ox7F）
    private byte[] bytes =
            new byte[]{0x3C,0x30,0x1C,0x07,0x02,0x05,0x3E,0x7F};
    private SerialPort serialttyS1;
    private InputStream ttyS1InputStream;
    private OutputStream ttyS1OutputStream;
    String readDatas = null;
    StringBuffer sb = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wait_on_Rl = (RelativeLayout) findViewById(R.id.wait_on_rl);
        TextView9 = (TextView) findViewById(R.id.textView9);
        wait_on_Rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送数据
                sendData();
            }
        });
        //初始化串口
        init_serial();

        //接收数据
        receiveData();
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
                        sb.append(new String(buffer,0,size));
                        runOnUiThread(new Runnable() {
                            public void run() {
                                TextView9.setText(sb.toString());
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
