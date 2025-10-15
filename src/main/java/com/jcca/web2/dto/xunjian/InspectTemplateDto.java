package com.jcca.web2.dto.xunjian;


import lombok.Data;

import java.util.List;

/**
 * @author lifp
 * @version 1.0
 * @description: 智能巡检-接受请求参数
 * @date 2025-10-09 星期四 10:50:41
 */
@Data
public class InspectTemplateDto {
    String id;

    // 模板code
    String templateCode;

    // 模板名称
    String templateName;

    // 选择得设备数据
    List<String> assetData;
}
