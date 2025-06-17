package com.jcca.dataProcessing.DataFilter;

import com.jcca.dataProcessing.Entity.CommonEntity;
import com.jcca.dataProcessing.manager.impl.DataChangeMangerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.dataProcessing.support.XunjianEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Zhaozheng
 * @description TODO CPU 普通阈值变动处理类
 * @className CpuFitlerHandler
 * @date 2023/10/27 9:26
 * @since 2.1.0.0
 */
@Component("saveFilterHandler")
public class SaveFilterHandler extends IFilterHandler<CommonEntity> {

    @Resource
    private DataChangeMangerService dataChangeMangerService;

    @Override
    public boolean handler(CommonEntity info) {
        dataChangeMangerService.saveInfo(info.getMaps());
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
