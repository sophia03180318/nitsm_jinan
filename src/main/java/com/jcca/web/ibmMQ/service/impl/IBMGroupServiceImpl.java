package com.jcca.web.ibmMQ.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.ibmMQ.dao.IBMGroupMapper;
import com.jcca.web.ibmMQ.entity.IBMGroup;
import com.jcca.web.ibmMQ.service.IBMGroupService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 11:49 2021/11/25
 * @ Description:
 */
@Service
public class IBMGroupServiceImpl extends ServiceImpl<IBMGroupMapper, IBMGroup> implements IBMGroupService {
    @Resource
    IBMGroupMapper ibmGroupMapper;

    @Override
    public boolean selectByName(String name, String connectId) {//是否有重名
        List<IBMGroup> ibmGroups = ibmGroupMapper.selectByName(name, connectId);
        if (ibmGroups.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String selectIdByName(String name, String connectId) {
        List<IBMGroup> ibmGroups = ibmGroupMapper.selectByName(name, connectId);
        if (ibmGroups.size() > 0) {
            return ibmGroups.get(0).getId();
        }
        return "";
    }
}
