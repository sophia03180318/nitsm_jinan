package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectConnectBean;
import com.jcca.web.collect.entity.CollectConnect;
import com.jcca.web.collect.service.CollectConnectService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 连接处理
 *
 * @author
 */
@Component
public class DisposeConnectAdapterImpl implements CollectAdapter {
    @Resource
    private CollectConnectService collectConnectService;


    /**
     * 处理采集的连接
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        List<CollectConnectBean> connBeanList = JSONUtil.toList(data, CollectConnectBean.class);
        List<CollectConnect> connList = new ArrayList<>();
        for (CollectConnectBean item : connBeanList) {
            if (StrUtil.isEmpty(item.getAssetId()) || StrUtil.isEmpty(item.getCollectTime()) || item.getEstablishedNum() == null) {
                continue;
            }

            Date date = new Date();
            date.setTime(Long.valueOf(item.getCollectTime()));
            CollectConnect connect = EntityBeanUtil.copy(item, CollectConnect.class);
            connect.setCollectTime(date);
            connect.setId(MyIdUtil.getId());
            connect.setEstablishedNum(item.getEstablishedNum());
            connList.add(connect);
        }
        if (connList.isEmpty()) {
            return;
        }
        // 刷新缓存
        collectConnectService.updateRealTimeData(connList);
        // 更新全部
        collectConnectService.updateBatchByAssetId(connList);

    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.CONNECT;
    }


}
