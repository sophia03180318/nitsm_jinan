package com.jcca.web.xunjian.adapter.v1.impl;

import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetHidConf;
import com.jcca.web.asset.service.AssetHidConfService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.vo.ThresholdAssetVo;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.xunjian.adapter.v1.XunJianAdapter;
import com.jcca.web.xunjian.adapter.v1.util.TemplateUtil;
import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.enums.XunJianTargetEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 端口巡检
 *
 * @author Lvyp
 */
@Service
public class InterfaceInAdpaterImpl implements XunJianAdapter {

    private static final List<Integer> NEED_MODE = Arrays.asList(42, 201);

    @Resource
    private CollectInterfacesService collectInterfaceServ;
    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private AssetService assetServ;
    @Resource
    private AssetHidConfService hidConfServ;

    @Override
    public XunjianDetail xunJian(XunjianAsset asset, XunjianDetail detail) {
        Asset itsmAsset = assetServ.getById(asset.getAssetId());

        if (!NEED_MODE.contains(itsmAsset.getAssetMode())) {
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("该设备类型无此指标");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }

        List<AssetHidConf> flagListByAsset = hidConfServ.getFlagListByAsset(asset.getAssetId(), AssetHidConf.TypeEnum.PORT.name());
        List<String> hidPortNameList = new ArrayList<String>();
        for (AssetHidConf assetHidConf : flagListByAsset) {
            hidPortNameList.add(assetHidConf.getFlag());
        }

        ThresholdAssetVo threshold = thresholdAssetService.findAssetThreshold(asset.getAssetId());
        List<CollectInterfaces> interResultN  = collectInterfaceServ.getRealTimeData(asset.getAssetId());
        List<CollectInterfaces> interResult= new ArrayList<>();
        for (CollectInterfaces collectInterfaces : interResultN) {
            if(collectInterfaces.getPortName().contains("vlan")||collectInterfaces.getPortName().contains("VLAN")){
                continue ;
            }
            //排除掉配置隐藏的端口
            if(hidPortNameList.contains(collectInterfaces.getPortName())){
                continue;
            }
            interResult.add(collectInterfaces);
        }


        if(interResult.isEmpty()){
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("系统未获取到任何数据，请人工确认");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }

        Optional<CollectInterfaces> maxPortIn = interResult.stream().filter(prot->{
           return prot.getPortSpeed()!=0;
        }).max(Comparator.comparingLong(CollectInterfaces::getPortInSpeed));
        CollectInterfaces interfaces = maxPortIn.get();

        if (Objects.isNull(interfaces)) {
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("系统未获取到任何数据，请人工确认");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }

        BigDecimal speedBig = new BigDecimal(interfaces.getPortSpeed()).multiply(new BigDecimal(8));
        Double portInRate = new BigDecimal(interfaces.getPortInSpeed()).divide(speedBig, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)).doubleValue();

        if (Objects.isNull(threshold) || Objects.isNull(threshold.getPortRateIn())) {
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("未设定阈值，请人工确认。当前最大值:" + portInRate + "%" + "端口：" + interfaces.getPortName());
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }

        detail.setThresholdValue(threshold.getPortRateIn().toString());

        if (portInRate > threshold.getPortRateIn()) {
            detail.setResultMsg(TemplateUtil.getThresholdTemp(threshold.getPortRateIn(), portInRate, true, true) + "<br/>【所属端口】：" + interfaces.getPortName());
            detail.setNormalFlag(XunjianDetail.EXCEPTION_FLAG);
            detail.setXunJianValue(portInRate.toString());
            return detail;
        }

        detail.setResultMsg(TemplateUtil.getThresholdTemp(threshold.getPortRateIn(), portInRate, true, true) + "<br/>【所属端口】：" + interfaces.getPortName());
        detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
        detail.setXunJianValue(portInRate.toString());
        return detail;

    }

    @Override
    public String getCode() {
        return XunJianTargetEnum.INTERFACE_FLOW_IN.getCode();
    }

}
