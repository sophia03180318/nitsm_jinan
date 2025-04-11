package com.jcca.common.webssh.constant;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Description: 常量池
 * @Author: NoCortY
 * @Date: 2020/3/8
 */
public class ConstantPool {
    /**
     * 发送指令：连接
     */
    public static final String WEBSSH_OPERATE_CONNECT = "connect";
    /**
     * 发送指令：命令
     */
    public static final String WEBSSH_OPERATE_COMMAND = "command";
    /**
     * 发送指令：心跳
     */
    public static final String WEBSSH_OPERATE_HEARTBEAT = "heartbeat";


    public static final Pattern CTRL_PATTERN = Pattern.compile("\\p{C}");


    /**
     * 十进制值
     * 字符名称
     * 含义描述
     * 0x00
     * NUL
     * 空字符（Null）
     * 0x01
     * SOH
     * 标题开始（Start of Heading）
     * 0x02
     * STX
     * 文本开始（Start of Text）
     * 0x03
     * ETX
     * 文本结束（End of Text）
     * 0x04
     * EOT
     * 传输结束（End of Transmission）
     * 0x05
     * ENQ
     * 询问（Enquiry）
     * 0x06
     * ACK
     * 确认（Acknowledge）
     * 0x07
     * BEL
     * 响铃（Bell）
     * 0x08
     * BS
     * 退格（Backspace）
     * 0x09
     * HT
     * 水平制表符（Horizontal Tab）
     * 0x0A
     * LF
     * 换行（Line Feed）
     * 0x0B
     * VT
     * 垂直制表符（Vertical Tab）
     * 0x0C
     * FF
     * 换页（Form Feed）
     * 0x0D
     * CR
     * 回车（Carriage Return）
     * 0x0E
     * SO
     * 移入（Shift Out）
     * 0x0F
     * SI
     * 移出（Shift In）
     * 0x10
     * DLE
     * 数据链路转义（Data Link Escape）
     * 0x11
     * DC1
     * 设备控制 1（Device Control 1）
     * 0x12
     * DC2
     * 设备控制 2（Device Control 2）
     * 0x13
     * DC3
     * 设备控制 3（Device Control 3）
     * 0x14
     * DC4
     * 设备控制 4（Device Control 4）
     * 0x15
     * NAK
     * 否定确认（Negative Acknowledge）
     * 0x16
     * SYN
     * 同步空闲（Synchronous Idle）
     * 0x17
     * ETB
     * 传输块结束（End of Transmission Block）
     * 0x18
     * CAN
     * 取消（Cancel）
     * 0x19
     * EM
     * 媒体结束（End of Medium）
     * 0x1A
     * SUB
     * 替换（Substitute）
     * 0x1B
     * ESC
     * 转义（Escape）
     * 0x1C
     * FS
     * 文件分隔符（File Separator）
     * 0x1D
     * GS
     * 组分隔符（Group Separator）
     * 0x1E
     * RS
     * 记录分隔符（Record Separator）
     * 0x1F
     * US
     * 单元分隔符（Unit Separator）
     * 0x7F
     * DEL
     * 删除（Delete）
     */
    public static final Map<Integer, String> CTRL_NAMES = new HashMap<Integer, String>() {{
        put(0x00, "NUL");
        put(0x01, "SOH");
        put(0x02, "STX");
        put(0x03, "ETX");
        put(0x04, "EOT");
        put(0x05, "ENQ");
        put(0x06, "ACK");
        put(0x07, "BEL");
        put(0x08, "BS");
        put(0x09, "HT");
        put(0x0A, "LF");
        put(0x0B, "VT");
        put(0x0C, "FF");
        put(0x0D, "CR");
        put(0x0E, "SO");
        put(0x0F, "SI");
        put(0x10, "DLE");
        put(0x11, "DC1");
        put(0x12, "DC2");
        put(0x13, "DC3");
        put(0x14, "DC4");
        put(0x15, "NAK");
        put(0x16, "SYN");
        put(0x17, "ETB");
        put(0x18, "CAN");
        put(0x19, "EM");
        put(0x1A, "SUB");
        put(0x1B, "ESC");
        put(0x1C, "FS");
        put(0x1D, "GS");
        put(0x1E, "RS");
        put(0x1F, "US");
        put(0x7F, "DEL");
    }};
}
