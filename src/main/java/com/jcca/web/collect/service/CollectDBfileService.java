package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectDBfile;
import com.jcca.web.common.service.bean.PullAlertLogResultReq;
import com.jcca.web.common.service.bean.PullAlertLogResultResp;

/**
 * 数据库文件
 *
 * @author sophia
 */
public interface CollectDBfileService extends IService<CollectDBfile> {
     PullAlertLogResultResp verifyDBfile(PullAlertLogResultReq req) throws Exception;

     PullAlertLogResultResp downDBfile(PullAlertLogResultReq req) throws Exception;
}
