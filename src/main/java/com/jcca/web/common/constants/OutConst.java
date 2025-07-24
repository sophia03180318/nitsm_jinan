package com.jcca.web.common.constants;

/**
 * @ClassName OutConst
 * @Description 外部接口常量
 * @Date 2020/7/31 9:17
 * @Author hanwone
 */
public interface OutConst {
    /**
     * 新增资产
     */
    Integer ADD_ASSET = 0;
    /**
     * 删除资产
     */
    Integer DEL_ASSET = 1;
    /**
     * 修改资产监控相关字段
     */
    Integer MODIFY_ASSET = 2;
    /**
     * 修改资产所有字段
     */
    Integer ALL_ASSET_UPDATE = 3;
    /**
     * 中心采集器IP在redis中的key
     */
    String COLLECT_MASTER_URL = "COLLECT_MASTER_URL";
    /**
     * 获取资产所有进程
     */
    String ALL_PROCESS_URI = "/business/getProcessInfosById";
    /**
     * 添加资产时测试采集指标
     */
    String ASSET_TEST_URI = "/business/testTarget";
    String ASSET_TEST_URI_V2 = "/business/testTargetV2";
    /**
     * 资产变动通知采集器
     */
    String NOTIFY_ONCHANGE_ASSET = "/business/assetChange";
    /**
     * 进程变动
     */
    String NOTIFY_ONCHANGE_PROCESS = "/business/processChange";
    /**
     * 指标变动
     */
    String NOTIFY_ONCHANGE_TARGET = "/business/targetChange";
    /**
     * 测试target
     */
    String TEST_TARGET = "/business/itsmTestTarget";
}
