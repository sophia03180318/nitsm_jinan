package com.jcca.component.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @ClassName ReceiveThresholdDto
 * @Description 接收采集器阈值告警数据
 * @Date 2020/6/5 16:35
 * @Author hanwone
 */
@Data
public class ReceiveCollectDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采集信息种类， CPU,磁盘,内存,丢包率,误码率
     * ReceiveCollectConst
     */
    @NotEmpty(message = "采集信息种类不可空")
    private String category;
    /**
     * 采集具体数据
     */
    @NotEmpty(message = "采集数据不可空")
    private String content;
}
