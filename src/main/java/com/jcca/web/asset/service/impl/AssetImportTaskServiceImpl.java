package com.jcca.web.asset.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.dao.AssetImportTaskMapper;
import com.jcca.web.asset.entity.AssetImportTask;
import com.jcca.web.asset.service.AssetImportTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ Author：sophia
 * @ Date：Created in 11:25 2021/7/8
 * @ Description:
 */
@Service
public class AssetImportTaskServiceImpl extends ServiceImpl<AssetImportTaskMapper, AssetImportTask> implements AssetImportTaskService {
    @Resource
    private AssetImportTaskMapper assetImportTaskMapper;


    @Override
    public String getLastOneId() {
        return assetImportTaskMapper.getLastOneId();
    }

    @Override
    public int getStatusById(String id) {
        return assetImportTaskMapper.getStatusById(id);

    }

    @Override
    public void setLastStatus(int status) {
        assetImportTaskMapper.setLastStatus(status);
    }


}