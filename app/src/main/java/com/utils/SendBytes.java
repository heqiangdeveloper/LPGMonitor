package com.utils;

/**
 * Created by qianghe on 2019/4/15.
 */
public class SendBytes {
    private static byte[] bytes;
    private static byte[] bytes2;
    private static int[] crc16;
    private static int c1;
    private static int c2;
    public static byte[] getSendBytes(int flag){
        Config.FLAG_SEND = flag;
        bytes = new byte[]{
                Config.FRAME_HEADER_SEND,
                Config.COMMAND_WORD_SEND,
                Config.DATA_LENGTH_SEND,
                (byte)Config.FLAG_SEND
        };

        //获取数据校验的数据
        crc16 = CRC16.getCrc16(bytes);//10进制
        c1 = crc16[0];//42  0x2a
        c2 = crc16[1];//36

        //最终发送的数据
        bytes2 = new byte[]{
                Config.FRAME_HEADER_SEND,
                Config.COMMAND_WORD_SEND,
                Config.DATA_LENGTH_SEND,
                (byte)Config.FLAG_SEND,
                (byte) c1,
                (byte) c2,
                Config.FRAME_TAIL_SEND
        };
        return bytes2;
    }
}
