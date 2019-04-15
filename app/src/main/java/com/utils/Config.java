package com.utils;

/**
 * Created by qianghe on 2019/4/15.
 */
public class Config {
    //串口文件路径
    public static String pathname = "/dev/ttyS2";
    //波特率
    public static int baudrate = 115200;

    //主机发送命令
    public static byte FRAME_HEADER_SEND = 0x3C;//帧头
    public static byte COMMAND_WORD_SEND = 0x30;//命令字
    public static byte DATA_LENGTH_SEND = 0x01;//数据长度
    public static int FLAG_SEND;//控制标志
    public static byte FRAME_TAIL_SEND = 0x3E;//帧尾

    //从机接收命令
    public static byte FRAME_HEADER_FEEDBACK;//帧头
    public static byte COMMAND_WORD_FEEDBACK;//命令字
    public static byte DATA_LENGTH_FEEDBACK;//数据长度
    public static byte PUMP_FRONT_FEEDBACK;//泵前压力
    public static byte LEVEL_FEEDBACK;//液位
    public static byte FLOWMETER_TEMPERATURE_FEEDBACK;//流量计温度
    public static byte FLOWMETER_UNLOADING_QUANTITY_FEEDBACK;//流量计卸液量
    public static byte FLOWMETER_RATE_FEEDBACK;//流量计瞬时流量
    public static byte STATUS_FEEDBACK;//从机状态
    public static byte FRAME_TAIL_FEEDBACK;//帧尾
}
