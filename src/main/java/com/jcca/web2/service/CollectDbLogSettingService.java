package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.CollectDbLogSetting;

import java.util.List;

/**
 * 采集日志配置信息
 */
public interface CollectDbLogSettingService extends IService<CollectDbLogSetting> {



    void updateCollectData(List<CollectDbLogSetting> collectDbLogSettingList, String dbId);

}
