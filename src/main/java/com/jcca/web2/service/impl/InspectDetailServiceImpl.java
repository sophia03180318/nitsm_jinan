package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import com.jcca.web.asset.vo.AssetMsgVo;
import com.jcca.web.xunjian.controller.util.XunjianReportUtil;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import com.jcca.web.xunjian.entity.bean.XunjianServerDetailBean;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.InspectDetailMapper;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.service.InspectDetailService;
import com.jcca.web2.vo.InspectRecordListVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HanHW
 * @description 巡检详情
 * @className InspectDetailServiceImpl
 * @date 2024/1/19 11:32
 * @since 2.1.0.0
 */
@Service
public class InspectDetailServiceImpl extends ServiceImpl<InspectDetailMapper, InspectDetail> implements InspectDetailService {

    @Resource
    private InspectDetailMapper inspectDetailMapper;
    @Resource
    private AssetService assetService;

    @Override
    public List<InspectRecordListVo> recordList() {
        List<InspectRecordListVo> downList;
        List<InspectRecordListVo> resList = new ArrayList<>();
        List<InspectRecordListVo> upList = inspectDetailMapper.findRecordList();
        for (InspectRecordListVo vvo : upList) {
            InspectRecordListVo ttvo = new InspectRecordListVo();
            downList = inspectDetailMapper.findDownList(vvo.getInspectCode());
            for (InspectRecordListVo vo : downList) {
                vo.setId(MyIdUtil.getId());
            }
            ttvo.setCreateTime(vvo.getCreateTime());
            ttvo.setInspectCode(vvo.getInspectCode());
            ttvo.setResultPath(vvo.getResultPath());
            ttvo.setList(downList);
            resList.add(ttvo);
        }

        return resList;
    }

    @Override
    public List<InspectRecordListVo> findRecordDetail(String inspectCode) {
        return inspectDetailMapper.findRecordDetail(inspectCode);
    }

    @Override
    public List<InspectDetail> findAssetDetail(String assetId, String inspectCode) {
        return inspectDetailMapper.findAssetDetail(assetId, inspectCode);
    }

    @Override
    public void deleteRecord(String inspectCode) {
        inspectDetailMapper.deleteRecord(inspectCode);
    }

    @Override
    public void exportAssetRecord(String inspectCode, String assetId, HttpServletResponse response) {
        List<InspectDetail> assetDetail = this.findAssetDetail(assetId, inspectCode);
        if (StringUtils.isEmpty(assetDetail)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        List<XunjianDetailV2> list = new ArrayList<>();
        XunjianServerDetailBean req = this.creatBean(assetId);
        for (InspectDetail record : assetDetail) {
            XunjianDetailV2 v2 = new XunjianDetailV2();
            v2.setXunjianTargetItem(record.getTargetName());
            v2.setCommand(record.getCommand());
            v2.setNormalFlagStr(record.getInspectState().equals(Web2Const.INSPECTED) ? "正常" : "异常");
            v2.setInputErrorStr(record.getResultMsg());
            list.add(v2);
        }

        req.setDetailList(list);
        SXSSFWorkbook createExcel = XunjianReportUtil.createExcelV2(req);

        DispatchRecordExcelUtil.responseBody(createExcel, response, "智能巡检报告单");
    }

    private XunjianServerDetailBean creatBean(String assetId) {
        XunjianServerDetailBean req = new XunjianServerDetailBean();
        AssetMsgVo oneMsg = assetService.findMsgById(assetId);
        req.setAppName(oneMsg.getAssetName());
        BeanUtils.copyProperties(oneMsg, req);
        return req;
    }
}
