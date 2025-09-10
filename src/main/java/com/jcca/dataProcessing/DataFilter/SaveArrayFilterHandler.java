package com.jcca.dataProcessing.DataFilter;

import com.jcca.dataProcessing.Entity.CommonEntity;
import com.jcca.dataProcessing.manager.impl.DataChangeMangerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO CPU 普通阈值变动处理类
 * @className CpuFitlerHandler
 * @date 2023/10/27 9:26
 * @since 2.1.0.0
 */
@Component("saveArrayFilterHandler")
public class SaveArrayFilterHandler extends IFilterHandler<List<CommonEntity>> {

    @Resource
    private DataChangeMangerService dataChangeMangerService;

    @Override
    public boolean handler(List<CommonEntity> infoList) {
        for (CommonEntity commonEntity : infoList) {
            dataChangeMangerService.saveInfo(commonEntity.getMaps());
        }
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
