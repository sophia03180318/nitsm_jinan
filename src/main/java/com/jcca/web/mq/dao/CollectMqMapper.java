package com.jcca.web.mq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.mq.entity.CollectMq;
import lombok.Data;
import org.apache.ibatis.annotations.Delete;

/**
 * @description:
 * @author: sophia
 * @create: 2026/01/29 09:32
 **/

public interface CollectMqMapper extends BaseMapper<CollectMq> {
    @Delete("delete from COLLECT_MQ where CONNECTION_ID=#{connectionId}")
    void removeCollectData(String connectionId);

}