package com.jcca.web2.vo;

import com.jcca.web2.entity.AlarmWhitelist;
import lombok.Data;

/**
 * @description: 响应数据
 * @author: Lvyp
 * @create: 2023/11/30 16:08
 */
@Data
public class WhitePageQueryVo extends AlarmWhitelist {

    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 事件类型名称
     */
    private String eventTypeName;

}
