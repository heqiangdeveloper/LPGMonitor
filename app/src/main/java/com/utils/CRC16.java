package com.utils;

/**
 * Created by qianghe on 2019/4/12.
 */
//****************************************************************************
//函数名称:		CRC16

//功能说明:		CRC校验码生成程序

//输入：
//	puchMsg ： 	用于计算 CRC 的报文

//返回：
//	int[] 类型 CRC

// 备注  :
// ****************************************************************************/
public class CRC16 {
    public static int[] getCrc16(byte[] data){
        int flag;
        //16位寄存器，所有位置均为1
        int wcrc = 0xffff;
        for(int i = 0; i < data.length; i++){
            //16位寄存器的地位字节
            //取被校验串的一个字节与16位寄存器的低位字节进行“异或（xor）”运算
            wcrc = wcrc ^ data[i];
            for(int j = 0; j < 8; j++){
                flag = wcrc & 0x0001;
                //右移动1位
                wcrc = wcrc >> 1;
                if(flag == 1)
                    wcrc ^= 0xa001;
            }
        }
        //获取低8位
        int low = wcrc >> 8;//或wcrc / 256；
        //获取高8位
        int up = wcrc % 256;

        int[] crc = {up,low};
        return crc;
    }
}
