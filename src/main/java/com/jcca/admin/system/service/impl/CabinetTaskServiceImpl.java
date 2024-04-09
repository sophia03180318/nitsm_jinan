package com.jcca.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.CabinetTaskMapper;
import com.jcca.admin.system.entity.CabinetTask;
import com.jcca.admin.system.service.CabinetTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ Author：sophia
 * @ Date：Created in 9:59 2021/8/12
 * @ Description:
 */
@Service
public class CabinetTaskServiceImpl extends ServiceImpl<CabinetTaskMapper, CabinetTask> implements CabinetTaskService {
    @Resource
    private CabinetTaskMapper cabinetTaskMapper;


    @Override
    public String getLastOneId() {
        return cabinetTaskMapper.getLastOneId();
    }

    @Override
    public int getStatusById(String id) {
        return cabinetTaskMapper.getStatusById(id);

    }

    @Override
    public void setLastStatus(int status) {
        cabinetTaskMapper.setLastStatus(status);
    }


}
