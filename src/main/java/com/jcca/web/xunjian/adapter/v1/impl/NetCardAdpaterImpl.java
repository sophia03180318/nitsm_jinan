package com.jcca.web.xunjian.adapter.v1.impl;

import cn.hutool.core.util.StrUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetHidConf;
import com.jcca.web.asset.service.AssetHidConfService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.enums.CollectNetCardStatus;
import com.jcca.web.collect.service.CollectNetworkCardService;
import com.jcca.web.xunjian.adapter.v1.XunJianAdapter;
import com.jcca.web.xunjian.adapter.v1.util.TemplateUtil;
import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.enums.XunJianTargetEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网卡状态
 *
 * @author Lvyp
 */
@Service
public class NetCardAdpaterImpl implements XunJianAdapter {

    private static final List<Integer> NEED_CPU_MODE = Arrays.asList(183);

    @Resource
    private CollectNetworkCardService networkServ;
    @Resource
    private AssetService assetServ;
    @Resource
    private AssetHidConfService  hidConfServ;

    @Override
    public XunjianDetail xunJian(XunjianAsset asset, XunjianDetail detail) {
        Asset itsmAsset = assetServ.getById(asset.getAssetId());

        if (!NEED_CPU_MODE.contains(itsmAsset.getAssetMode())) {
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("该设备类型无此指标");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }

        List<CollectNetworkCard> cadrList = networkServ.getRealTimeData(asset.getAssetId());
        detail.setThresholdValue("");
        if (cadrList.isEmpty()) {
            detail.setXunJianValue(CollectNetCardStatus.UP.name());
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            detail.setResultMsg(TemplateUtil.getNetWorkStatusTemp(true));

            return detail;
        }

        List<AssetHidConf> hidList = hidConfServ.getFlagListByAsset(asset.getAssetId(), AssetHidConf.TypeEnum.NET_CARD.name());
        List<String> cardList = new ArrayList<String>();
        if(!hidList.isEmpty()){
            cardList = hidList.stream().map(item->item.getFlag()).collect(Collectors.toList());
        }
        for (CollectNetworkCard card : cadrList) {
            if (StrUtil.isEmpty(card.getIp())) {
                continue;
            }
            if(cardList.contains(card.getName())){
                continue ;
            }
            if (CollectNetCardStatus.DOWN.getCode().equals(card.getStatus())) {
                detail.setXunJianValue(CollectNetCardStatus.DOWN.name());
                detail.setNormalFlag(XunjianDetail.EXCEPTION_FLAG);
                detail.setResultMsg(TemplateUtil.getNetWorkStatusTemp(false)+"异常网卡："+card.getName());

                return detail;
            }
        }

        detail.setXunJianValue(CollectNetCardStatus.UP.name());
        detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
        detail.setResultMsg(TemplateUtil.getNetWorkStatusTemp(true));

        return detail;
    }

    @Override
    public String getCode() {
        return XunJianTargetEnum.NET_CARD.getCode();
    }

}
