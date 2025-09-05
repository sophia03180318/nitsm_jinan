package com.jcca.web2.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.CollectDbSlowSql;

import java.util.List;

public interface CollectDbSlowSqlService  extends IService<CollectDbSlowSql> {

    void updateCollectData(List<CollectDbSlowSql> collectSlowList, String dbId);
}
