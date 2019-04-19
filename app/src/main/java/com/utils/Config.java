package com.utils;

/**
 * Created by qianghe on 2019/4/15.
 */
public class Config {
    //串口文件路径
    public static String pathname = "/dev/ttyS2";

    //usb串口文件路径
    public static String usb_pathname = "/dev/ttyUSB10";

    //波特率
    public static int baudrate = 115200;

    //波特率
    public static int usb_baudrate = 9600;

    //主机发送命令
    public static byte FRAME_HEADER_SEND = 0x3C;//帧头
    public static byte COMMAND_WORD_SEND = 0x30;//命令字
    public static byte DATA_LENGTH_SEND = 0x01;//数据长度
    public static int FLAG_SEND;//控制标志
    public static byte FRAME_TAIL_SEND = 0x3E;//帧尾

    //从机接收命令
    public static int FRAME_HEADER_FEEDBACK;//帧头
    public static int COMMAND_WORD_FEEDBACK;//命令字
    public static int DATA_LENGTH_FEEDBACK;//数据长度
    public static double PUMP_FRONT_FEEDBACK;//泵前压力
    public static double PUMP_BEHIND_FEEDBACK;//泵后压力
    public static double LEVEL_FEEDBACK;//液位
    public static double FLOWMETER_TEMPERATURE_FEEDBACK;//流量计温度
    public static double FLOWMETER_UNLOADING_QUANTITY_FEEDBACK;//流量计卸液量
    public static double FLOWMETER_RATE_FEEDBACK;//流量计瞬时流量
    public static int STATUS_FEEDBACK;//从机状态
    public static int DEVICE_STATUS_FEEDBACK1;//设备状态1
    public static int DEVICE_STATUS_FEEDBACK2;//设备状2
    public static int DEVICE_STATUS_FEEDBACK3;//设备状态3
    public static int CRC_FEEDBACK1;//数据校验1
    public static int CRC_FEEDBACK2;//数据校验2
    public static int FRAME_TAIL_FEEDBACK;//帧尾
}
