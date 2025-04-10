package com.jcca.web2.dto;

import lombok.Data;

/**
 * @author: hhw
 * @description: AlarmUnhandledDto主要是用来
 * @date: 2025-04-10  11:09
 * @since: 2.1.5.0
 */
@Data
public class AlarmUnhandledDto {

    private String assetName;
    private String assetIp;
    private String occurTime;
    private String content;
    private String description;
    private String opinion;
}
