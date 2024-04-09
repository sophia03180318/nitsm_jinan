package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Zhaozheng
 * @description TODO 事件信息类
 * @className EventInfoEntity
 * @date 2023/11/28 13:15
 * @since 2.1.0.0
 */
@Data
public class SnmpEventInfoEntity extends CommonEntity implements Serializable {
    //获取到的IP地址信息
    private Map<String, String> map;
}
