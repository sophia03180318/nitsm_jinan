package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.FileRelateMapper;
import com.jcca.web2.entity.FileRelate;
import com.jcca.web2.service.FileRelateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 维护手册2.1
 * @className FileRelateServiceImpl
 * @date 2025/3/27 16:39
 * @since 2.1.5.0
 */
@Service
public class FileRelateServiceImpl extends ServiceImpl<FileRelateMapper, FileRelate> implements FileRelateService {

    @Resource
    private FileRelateMapper fileRelateMapper;

    @Override
    public FileRelate getByFileId(String fileId) {
        return fileRelateMapper.getByFileId(fileId);
    }
}
