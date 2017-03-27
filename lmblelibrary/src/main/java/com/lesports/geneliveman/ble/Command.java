package com.lesports.geneliveman.ble;

/**
 * Created by raysun on 15/11/9.
 */
class Command {
    // 命令前缀
    public static final String CMD_PREFIX = "FF";
    // 命令后缀
    public static final String CMD_SUFFIX = "00";
    // 命令版本
    public static final String CMD_VERSION = "01";

    // 重启Wifi命令
    public static final String CMD_REBOOT = "0B";
    // 设置sta ssid
    public static final String CMD_SET_STA_SSID = "08";
    // 设置sta password
    public static final String CMD_SET_STA_PASSWORD = "09";
    // 设置ap ssid
    public static final String CMD_SET_AP_SSID = "0D";
    // 设置ap password
    public static final String CMD_SET_AP_PASSWORD = "0E";
    // 获取IP
    public static final String CMD_GET_IP = "0A";
    // 设置模式
    public static final String CMD_SET_MODE = "07";

    // 获取模式
    public static final String CMD_AP_OR_STA = "10";
    // 获取ap模式下的ssid
    public static final String CMD_GET_AP_SSID = "11";
    // 获取ap模式下的password
    public static final String CMD_GET_AP_PASSWORD = "12";
    // 获取sta模式下的ssid
    public static final String CMD_GET_STA_SSID = "13";
    // 获取sta模式下的password
    public static final String CMD_GET_STA_PASSWORD = "14";
    // 获取当前固件的版本
    public static final String CMD_GET_DEVICE_VERSION = "15";

    // #define COMMAND_AP_OR_STA           @"FF01051000"
    // #define COMMAND_GET_AP_SSID         @"FF01051100"
    // #define COMMAND_GET_AP_PASS         @"FF01051200"
    // #define COMMAND_GET_STA_SSID        @"FF01051300"
    // #define COMMAND_GET_STA_PASS        @"FF01051400"
    // #define COMMAND_GET_DEVICE_VERSION  @"FF01051500"

    //    开始录像
    //    0xFF01050100
    //    停止录像
    //    0xFF01050200
    //    拍照
    //    0xFF01050300
    //    开启RTMP码流
    //    0xFF01050400
    //    停止RTMP码流
    //    0xFF01050500
    //    设置RTMP推送地址
    //    0xFF01050600


}
