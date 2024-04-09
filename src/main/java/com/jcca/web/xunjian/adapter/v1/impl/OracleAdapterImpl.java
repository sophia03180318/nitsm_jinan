package com.jcca.web.xunjian.adapter.v1.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.collect.enums.CollectNetCardStatus;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web.xunjian.adapter.v1.XunJianAdapter;
import com.jcca.web.xunjian.adapter.v1.util.TemplateUtil;
import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.enums.XunJianTargetEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class OracleAdapterImpl  implements XunJianAdapter {

    @Resource
    private ManageDbService dbServ;
    @Resource
    private AlarmInfoService alarmServ;

    @Override
    public XunjianDetail xunJian(XunjianAsset asset, XunjianDetail detail) {
        QueryWrapper<ManageDb> query = new QueryWrapper<>();
        query.eq("ASSET_ID", asset.getAssetId());

        List<ManageDb> list = dbServ.list(query);
        if(list.isEmpty()){
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("该设备不存在此指标");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);

            return detail;
        }

        boolean isError = false;
        for (ManageDb manageDb : list) {
            QueryWrapper<AlarmInfo> alarmQuery = new QueryWrapper<>();
            alarmQuery.eq("ALARM_STATE",1);
            alarmQuery.eq("ASSET_ID",manageDb.getAssetId());
            alarmQuery.like("ALARM_CODE","DB");
            List<AlarmInfo> list1 = alarmServ.list(alarmQuery);
            if(!list1.isEmpty()){
                isError = true;
            }
        }

        if(isError){
            detail.setXunJianValue("状态异常");
            detail.setNormalFlag(XunjianDetail.EXCEPTION_FLAG);
            detail.setResultMsg("oracle存在异常状态");
            return detail;
        }

        detail.setXunJianValue("状态正常");
        detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
        detail.setResultMsg("oracle状态正常");

        return detail;
    }

    @Override
    public String getCode() {
        return XunJianTargetEnum.ORACLE.getCode();
    }
}
