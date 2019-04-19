package com.utils;

/**
 * Created by qianghe on 2019/4/17.
 */
//将十六进制表示的 01 10 转化成对应的double型数据
public class TransferValue {
    public static double getDoubleValue(String value1,String value2){
        int i1 = Integer.parseInt(value1,16);
        int i2 = Integer.parseInt(value2,16);
        double d = ((i1 << 8) | i2)/100.0;
        return d;
    }

    public static String getDoubleValue(String value){
        String s = "";
        StringBuffer sb = new StringBuffer();
        if(value.length() == 8){
            s = value;
        }else{
            while (value.length() < 8){

            }
        }
        return s;
    }

}
