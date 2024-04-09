package com.jcca.web.xunjian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.xunjian.controller.bean.BeginXunJianReq;
import com.jcca.web.xunjian.entity.XunjianRecord;
import com.jcca.web.xunjian.entity.XunjianRecordV2;
import com.jcca.web.xunjian.entity.bean.XunjianLogBean;
import com.jcca.web.xunjian.entity.bean.XunjianServerDetailBean;
import com.jcca.web.xunjian.service.bean.XunjianTab;

import java.util.Collection;
import java.util.List;

/**
 * @author hanwone
 * @date 2021-02-24 16:12:09
 **/
public interface XunjianRecordV2Service extends IService<XunjianRecordV2> {

    /**
     * 开始巡检
     * @param req
     */
    String startXunJian(BeginXunJianReq req);

    /**
     * 查询当前用户的巡检记录
     * V2
     * @param username
     */
    List<XunjianLogBean> queryXunjianLog(String username);

    /**
     * 查询巡检结果明细信息
     * V2
     * @param xunjianRecordId
     * @param assetId
     */
    XunjianServerDetailBean queryXunjianDetail(String xunjianRecordId, String assetId);

    /**
     * 获取巡检简述报表
     * @param xunjianRecordId
     * @return
     */
    Collection<XunjianTab> getLogTab(String xunjianRecordId);

    /**
     * 删除巡检
     * @param xunjianRecordId
     */
    void removeXunjianLog(String xunjianRecordId);
}
