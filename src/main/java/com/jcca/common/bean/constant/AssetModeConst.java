package com.jcca.common.bean.constant;

/**
 * @ClassName AssetModeConst
 * @Description 资产类型常量
 * @Date 2020/6/2 11:42
 * @Author hanwone
 */
public interface AssetModeConst {

    // 183:服务器,1831:终端,201:交换机,42:路由器,1833:工控机,1832:小型机,318:磁盘阵列,1834:自律机,
    // 7001:存储设备,7002:安全边界,7003:网闸,7004:kvm,7005:PDU,7006:配线架,7007:光纤架,7008:授时仪,7009:双机切换单元,
    // 7010:防病毒服务器,7011:分析单元,7012:检测单元,7013:DDF,7014:KVM切换器,7015:空调,7016:UPS电源,7017:虚拟类型
    /**
     * 防火墙
     */
    Integer FIRE_WALL = 12;
    /**
     * 路由器
     */
    Integer ROUTER = 42;
    /**
     * 存储设备
     */
    Integer STORE = 61;
    /**
     * 小型机
     */
    Integer SMALL_SERVER = 1832;
    /**
     * 服务器
     */
    Integer SERVER = 183;
    /**
     * 交换机
     */
    Integer SWITCH = 201;
    /**
     * 终端
     */
    Integer TERMINAL = 1831;
    /**
     * 工控机
     */
    Integer IPC = 1833;
    /**
     * 自律机
     */
    Integer SELF = 1834;
    /**
     * 数据库
     */
    Integer ORACLE_DB = 263;

    /**
     * 磁盘阵列
     */
    Integer RAID = 318;
    /**
     * 空调
     */
    Integer HVAC = 7015;
    /**
     * UPS电源
     */
    Integer UPS = 7016;
    /**
     * 虚拟资产
     */
    Integer VIR = 7017;

    String B24 = "2498-B24";
}
