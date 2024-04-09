package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.AssetImportTask;

/**
 * @ Author：sophia
 * @ Date：Created in 11:28 2021/7/8
 * @ Description:
 */
public interface AssetImportTaskService extends IService<AssetImportTask> {


    /*获取最新任务ID*/
    String getLastOneId();

    /*获取指定任务的状态值*/
    int getStatusById(String id);

    /*改变最后一次任务的状态值*/
    void setLastStatus(int status);

}
