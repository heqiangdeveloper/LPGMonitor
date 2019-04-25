package com.utils;

import java.io.OutputStream;
import java.util.TimerTask;

public class SelectTask extends TimerTask {
    private OutputStream ttyS1OutputStream;
    private int flag;
    public SelectTask(OutputStream ttyS1OutputStream,int flag) {
        this.ttyS1OutputStream = ttyS1OutputStream;
        this.flag = flag;
    }

    @Override
    public void run() {
        /* 串口发送字节 */
        if(ttyS1OutputStream != null){
            byte[] sendCommandBytes = SendBytes.getSendBytes(flag);
            try{
                ttyS1OutputStream.write(sendCommandBytes);
            }catch (Exception e){

            }
        }
    }
}
