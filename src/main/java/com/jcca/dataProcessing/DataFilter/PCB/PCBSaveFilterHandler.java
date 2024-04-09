package com.jcca.dataProcessing.DataFilter.PCB;

import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectPcbEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectPcb;
import com.jcca.web.collect.service.CollectPcbService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 板卡信息保存处理类
 * @className PCBFilterHandler
 * @date 2023/10/27 9:51
 * @since 2.1.0.0
 */
@Component("pCBSaveFilterHandler")
public class PCBSaveFilterHandler extends IFilterHandler<List<CollectPcbEntity>> {

    @Resource
    private CollectPcbService pcbService;

    @Override
    public boolean handler(List<CollectPcbEntity> info) {
        CollectPcbEntity collectPcbEntity = info.get(0);
        Date date = new Date();
        date.setTime(collectPcbEntity.getCollectTime());
        String collectCode = MyIdUtil.getId();
        List<CollectPcb> pcbList = new ArrayList<CollectPcb>();
        for (CollectPcbEntity pcb : info) {
            CollectPcb pcbData = EntityBeanUtil.copy(pcb, CollectPcb.class);
            pcbData.setCollectTime(date);
            pcbData.setCollectCode(collectCode);
            pcbData.setId(MyIdUtil.getId());
            pcbList.add(pcbData);
        }
        if (!pcbList.isEmpty()) {
            // 更新板卡信息
            pcbService.updateBatchByAssetId(pcbList);
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
