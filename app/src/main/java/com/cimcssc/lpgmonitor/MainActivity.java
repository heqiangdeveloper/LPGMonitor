package com.cimcssc.lpgmonitor;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.serial.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author october
 */
public class MainActivity extends BaseActivity {
    private byte[] bytes = new byte[]{0b01010110,0b10,0b11};
    private SerialPort serialttyS1;
    private InputStream ttyS1InputStream;
    private OutputStream ttyS1OutputStream;
    private ImageView imageView;
    private TextView TextView9;
    String readDatas = null;
    StringBuffer sb = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.textView10);
        TextView9 = (TextView) findViewById(R.id.textView9);
        imageView.setOnClickListener(new View.OnClickListener() {
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
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

            }
        }).start();*/

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
            try{
                ttyS1OutputStream.write(bytes);
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

    public void receiveData2() {
        while (ttyS1InputStream != null) {
            try {
                int size;
                byte[] buffer = new byte[1024];
                size = ttyS1InputStream.read(buffer);
                if(size > 0){
                    sb.append(new String(buffer,0,size));
                    Log.e("myreceive","receive data is: " + sb.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close_serial();
    }
}
