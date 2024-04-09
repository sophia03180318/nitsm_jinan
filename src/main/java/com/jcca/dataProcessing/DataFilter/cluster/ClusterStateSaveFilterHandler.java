package com.jcca.dataProcessing.DataFilter.cluster;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectClusterEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectCluster;
import com.jcca.web.collect.service.CollectClusterService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 集群信息数据库保存
 * @className ClusterABStatusFilterHandler
 * @date 2023/10/20 17:24
 * @since 2.1.0.0
 */
@Component("clusterStateSaveFilterHandler")
public class ClusterStateSaveFilterHandler extends IFilterHandler<CollectClusterEntity> {

    @Resource
    private CollectClusterService clusterServ;

    @Override
    public boolean handler(CollectClusterEntity info) {

        CollectCluster collectCluster = EntityBeanUtil.copy(info, CollectCluster.class);
        Date date = new Date();
        date.setTime(Long.valueOf(collectCluster.getCollectTimeStr()));
        collectCluster.setCollectTime(date);
        collectCluster.setId(MyIdUtil.getId());
        QueryWrapper<CollectCluster> queryWrapper = new QueryWrapper<CollectCluster>();
        queryWrapper.eq("ASSET_ID", info.getAssetId());
        clusterServ.remove(queryWrapper);
        clusterServ.save(collectCluster);


        return true;

    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
