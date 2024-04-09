package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.CabinetTask;

/**
 * @ Author：sophia
 * @ Date：Created in 9:58 2021/8/12
 * @ Description:
 */
public interface CabinetTaskService extends IService<CabinetTask> {

    /*获取最新任务ID*/
    String getLastOneId();

    /*获取指定任务的状态值*/
    int getStatusById(String id);

    /*改变最后一次任务的状态值*/
    void setLastStatus(int status);

}
