package com.jcca.web.xunjian.adapter.v1;

import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.entity.XunjianDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 巡检
 *
 * @author Lvyp
 */
@Slf4j
@Component
public class XunJianHandler implements ApplicationContextAware {

    public static final String TEMPLATE = "";
    private static final Byte WATCH_FLAG = 1;

    @Resource
    private AssetService assetServ;

    /**
     * 采集的处理集合
     */
    public Map<String, XunJianAdapter> xunjianDisposeMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (Objects.nonNull(xunjianDisposeMap)) {
            return;
        }
        Map<String, XunJianAdapter> beanMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
                XunJianAdapter.class);
        xunjianDisposeMap = new HashMap<>(beanMap.size());
        for (Map.Entry<String, XunJianAdapter> entry : beanMap.entrySet()) {
            if (xunjianDisposeMap.containsKey(entry.getValue().getCode())) {
                throw new RuntimeException("初始化巡检适配器失败,code[" + entry.getValue().getCode() + "] 发现多个适配器");
            } else {
                xunjianDisposeMap.put(entry.getValue().getCode(), entry.getValue());
            }
        }
        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "初始化巡检处理适配器完成:" + xunjianDisposeMap.size()));
        }
    }

    public XunjianDetail xunjian(String code, XunjianAsset asset, String xunjianRecordId) {
        XunjianDetail detail = new XunjianDetail();
        detail.setXunjianRecordId(xunjianRecordId);
        detail.setXunjianTargetItem(code);
        detail.setId(MyIdUtil.getId());
        detail.setAssetId(asset.getAssetId());
        detail.setAssetMode(asset.getAssetMode());
        detail.setAssetName(asset.getAssetName());

        Asset itsmAsset = assetServ.getById(asset.getAssetId());
        if(Objects.isNull(itsmAsset)){
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("该设备已被删除");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }
        if (!WATCH_FLAG.equals(itsmAsset.getWatch())) {
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("该设备未纳入系统监控需人工巡检");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }


        XunJianAdapter xunJianAdapter = xunjianDisposeMap.get(code);
        if (Objects.isNull(xunJianAdapter)) {
            log.error("无指标处理类：{}", code);
            return null;
        }
        return xunJianAdapter.xunJian(asset, detail);
    }

}
