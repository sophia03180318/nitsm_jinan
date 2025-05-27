package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.dto.xunjian.XunjianJobDto;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.vo.ItemVo;

import java.util.List;

public interface XunjianScheduleService extends IService<XunjianSchedule> {

    void addSchedule(XunjianJobDto dto);

    void joinScheduleJob();

    void beginXunjian(XunjianJobDto dto);

    void pauseJob(String id);

    void recoverJob(String id);

    /**
     * 资产组织树
     *
     * @return List
     */
    List<ItemVo> getOrgModeAssetList();

    void removeSchedule(String id);

    void updateSchedule(XunjianJobDto dto);

    List<XunjianSchedule> findByJobId(String jobId);
}
