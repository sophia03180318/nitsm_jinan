package com.jcca.dataProcessing.DataFilter.bhm.memory;

import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.CollectBhmFanEntity;
import com.jcca.dataProcessing.Entity.CollectBhmMemoryEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import java.util.List;


@Component("bhmMemorySaveHandler")
public class BhmMemorySaveHandler  extends IFilterHandler<List<CollectBhmMemoryEntity>> {


    @Override
    public boolean handler(List<CollectBhmMemoryEntity> info) throws ResultException, Exception {
        return false;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
