package com.jcca.web2.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.PortTempMapper;
import com.jcca.web2.dto.PortModelTemp;
import com.jcca.web2.dto.PortTempDto;
import com.jcca.web2.entity.PortTemp;
import com.jcca.web2.service.PortTempService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;

/**
 * @description:
 * @author: sophia
 * @create: 2024/01/09 14:04
 **/
@Service
public class PortTempServiceImpl extends ServiceImpl<PortTempMapper, PortTemp> implements PortTempService {
    @Resource
    PortTempMapper tempMapper;

    @Value("${project.upload.file-path}")
    private String path;


    @Override
    public PortTempDto getPortTempMsg(String id) {
        PortTemp portTemp = this.getById(id);
        if (ObjectUtil.isNull(portTemp)) {
            return new PortTempDto();
        }
        PortTempDto portTempDto = PortTempDto.getPortTemp(portTemp);
        String filePath = path + "/portTemp/";
        Reader reader = null;
        try {
            File file = new File(filePath + portTemp.getModelId() + "&&" + portTemp.getName());
            if (!file.exists()) {
                return portTempDto;
            }

            reader = new InputStreamReader(new FileInputStream(file));
            char[] tempchars = new char[50];
            int charread = 0;
            StringBuilder builder = new StringBuilder();
            while ((charread = reader.read(tempchars)) != -1) {
                if ((charread == tempchars.length)) {
                    builder.append(tempchars);
                } else {
                    for (int i = 0; i < charread; i++) {
                        builder.append(tempchars[i]);
                    }
                }
            }
            reader.close();
            portTempDto.setMsg(builder.toString());
            return portTempDto;
        } catch (IOException e) {
            if (ObjectUtil.isNotNull(reader)) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    return portTempDto;
                }
            }
            return portTempDto;
        }
    }

    @Override
    public List<PortModelTemp> getNetworkDevices() {
        return tempMapper.getNetworkDevices();
    }

    @Override
    public List<PortTemp> getTemplateByAssetId(String assetId) {
        List<PortTemp> list = tempMapper.getTemplateByAssetId(assetId);
        return list;
    }

}