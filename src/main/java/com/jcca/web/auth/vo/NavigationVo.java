package com.jcca.web.auth.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @ClassName NavigationVo
 * @Description 导航栏
 * @Date 2020/5/22 11:38
 * @Author hanwone
 */
@Data
@EqualsAndHashCode
public class NavigationVo {
    /**
     * 点击ID
     */
    @NotNull(message = "点击物件ID不能为空")
    private String id;
    /**
     * 导航名称
     */
    private String name;
    /**
     * 点击到的物件类型，0：首页，1:机柜，2：资产，3:历史
     * NavigationEnum
     */
    @NotNull(message = "点击按钮类型不能为空")
    private int type;

    private int assetMode;
}
