package com.jcca.web2.dto;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcca.web2.entity.PortTemp;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @description: 端口面板
 * @author: sophia
 * @create: 2024/01/08 13:38
 **/
@Data
public class PortTempDto {
    private String id;

    //类型类型
    private String modelId;

    //光口数量
    private Integer port1 = 0;

    //电口数量
    private Integer port2 = 0;

    //模板名称
    private String name;

    //模板内容
    private String msg;

    public static PortTempDto getPortTemp(PortTemp portTemp) {
        PortTempDto portTempDto = new PortTempDto();
        if (ObjectUtil.isNotNull(portTemp.getId())) {
            portTempDto.setId(portTemp.getId());
        }
        portTempDto.setName(portTemp.getName());
        portTempDto.setModelId(portTemp.getModelId());
        if (ObjectUtil.isNotNull(portTemp.getMsg())) {
            portTempDto.setMsg(portTemp.getMsg());
        }
        try {
            if (!StringUtils.isEmpty(portTemp.getPortCount())) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(portTemp.getPortCount());
                portTempDto.setPort1(jsonNode.get("port1").asInt());
                portTempDto.setPort2(jsonNode.get("port2").asInt());
            }
        } catch (JsonProcessingException e) {
            portTempDto.setPort1(0);
            portTempDto.setPort2(0);
        }
        return portTempDto;
    }
}