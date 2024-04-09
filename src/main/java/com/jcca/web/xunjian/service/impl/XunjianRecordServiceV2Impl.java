package com.jcca.web.xunjian.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetMsgVo;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.service.impl.OutServiceImpl;
import com.jcca.web.xunjian.adapter.v2.XunjianV2Handler;
import com.jcca.web.xunjian.controller.XunjianV2Controller;
import com.jcca.web.xunjian.controller.bean.BeginXunJianReq;
import com.jcca.web.xunjian.dao.XunjianRecordV2Dao;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import com.jcca.web.xunjian.entity.XunjianRecordV2;
import com.jcca.web.xunjian.entity.bean.XunjianLogBean;
import com.jcca.web.xunjian.entity.bean.XunjianServerDetailBean;
import com.jcca.web.xunjian.service.XunjianDetailV2Service;
import com.jcca.web.xunjian.service.XunjianRecordV2Service;
import com.jcca.web.xunjian.service.bean.XunjianTab;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author yu_chen
 * @date 2021-02-24 16:12
 **/
@Slf4j
@Service
public class XunjianRecordServiceV2Impl extends ServiceImpl<XunjianRecordV2Dao, XunjianRecordV2> implements XunjianRecordV2Service {

    private static final List<Integer> NET_ASSET_MODE = Arrays.asList(201, 42);

    @Value("${project.upload.file-path}")
    private String filePath;
    @Value("${project.upload.static-path}")
    private String staticPath;
    @Value("${project.upload.static-url}")
    private String staticUrl;

    @Resource
    private AssetService assetServ;
    @Resource
    private XunjianV2Handler xunjianV2Handler;
    @Resource
    private XunjianDetailV2Service xunjianDetailServ;

    @Resource
    private ThreadPoolExecutor xunjianExecutor;
    @Resource
    private OutService outService;


    @Async("xunjianAsync")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String startXunJian(BeginXunJianReq req) {
        try {
            String username = ShiroUtil.getSubject().getUsername();
            List<String> assetIdList = req.getAssetIdList();
            List<String> targetList = req.getTargetList();

            if (assetIdList.isEmpty()) {
                return "";
            }

            String id = MyIdUtil.getId();
            XunjianRecordV2 xcunjianRecordV2 = new XunjianRecordV2();
            xcunjianRecordV2.setId(id);
            xcunjianRecordV2.setOperator(username);
            xcunjianRecordV2.setXunjianShift("");
            xcunjianRecordV2.setXunjianTime(new Date());

            List<XunjianDetailV2> detailList = new ArrayList<XunjianDetailV2>();
            //开启多线程处理巡检
            if (assetIdList.size() == 0) {
                return "";
            }
            CountDownLatch cdh = new CountDownLatch(assetIdList.size());

            for (String assetId : assetIdList) {
                xunjianExecutor.execute(() -> {
                    try {
                        executeXunJian(assetId, targetList, id, xcunjianRecordV2, detailList);
                    } catch (Exception e) {
                        log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_XUNJIAN, ErrorCodeEnum.WEB_XUNJIAN_COLLECT_ERROR, "", e.getMessage()), e);
                        //增加一条巡检失败的提醒
                        XunjianDetailV2 temp = createErrorDetail(id, "巡检过程中系统出错", "--", assetServ.getById(assetId));
                        temp.setXunjianAdapter("");
                        temp.setInputOrgStr(e.getMessage());
                        detailList.add(temp);

                    } finally {
                        cdh.countDown();
                    }
                });
            }


            try {
                cdh.await();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            if (!detailList.isEmpty()) {
                xunjianDetailServ.saveBatch(detailList);
            }

            StringBuilder errorAssetOrgStr = new StringBuilder("【智能巡检原始报告】\r\n");
            StringBuilder successAssetOrgStr = new StringBuilder("【智能巡检原始报告】\r\n");

            Map<String, List<XunjianDetailV2>> tabMap = detailList.stream().collect(Collectors.groupingBy(XunjianDetailV2::getAssetId));
            Set<String> assetTabList = tabMap.keySet();
            List<Integer> errorFlag = Arrays.asList(1, 2);
            for (String assetId : assetTabList) {
                List<XunjianDetailV2> xunjianDetailV2s = tabMap.get(assetId);
                List<XunjianDetailV2> collect = xunjianDetailV2s.stream().filter(XunjianDetailV2 -> errorFlag.contains(XunjianDetailV2.getNormalFlag())).collect(Collectors.toList());
                if (collect.isEmpty()) {
                    //拼接正常
                    getStringBuilder(successAssetOrgStr,xunjianDetailV2s,assetId);
                } else {
                    //拼接异常
                    getStringBuilder(errorAssetOrgStr,xunjianDetailV2s,assetId);
                }

            }


            String id1 = MyIdUtil.getId();
            String path = "/xunjian/" + DateUtil.today() + "/" +id1;
            //保存正常的日志
            FileWriter writer = new FileWriter(filePath + path+"normal.txt");
            writer.write(successAssetOrgStr.toString());

            //保存异常的日志
            FileWriter writer2 = new FileWriter(filePath + path+"error.txt");
            writer2.write(errorAssetOrgStr.toString());

            //保存全量的日志
            FileWriter writer3 = new FileWriter(filePath + path+"all.txt");
            writer3.write(errorAssetOrgStr.append(successAssetOrgStr).toString());

            //放入文件查看地址
            xcunjianRecordV2.setXunjianTarget(staticUrl + staticPath + path);
            save(xcunjianRecordV2);

            return id;
        } finally {
            XunjianV2Controller.XUN_JIAN_FLG = 0;
        }
    }


    @Override
    public List<XunjianLogBean> queryXunjianLog(String username) {
        QueryWrapper<XunjianRecordV2> query = new QueryWrapper<XunjianRecordV2>();
        query.eq("OPERATOR", username);
        query.orderByDesc("XUNJIAN_TIME");
        List<XunjianRecordV2> list = list(query);

        List<XunjianLogBean> logs = new ArrayList<XunjianLogBean>();
        for (XunjianRecordV2 xunjianRecordV2 : list) {
            List<String> filterList = new ArrayList<String>();
            XunjianLogBean xunjianLogBean = new XunjianLogBean();
            xunjianLogBean.setDateTime(xunjianRecordV2.getCreateTime());
            xunjianLogBean.setDate(xunjianRecordV2.getCreateTime());
            xunjianLogBean.setXunjianRecordId(xunjianRecordV2.getId());
            if (StrUtil.isNotEmpty(xunjianRecordV2.getXunjianTarget()) && xunjianRecordV2.getXunjianTarget().startsWith("http")) {
                xunjianLogBean.setOrgFilePath(xunjianRecordV2.getXunjianTarget());
            }
            xunjianLogBean.setRemark(xunjianRecordV2.getRemark());
            QueryWrapper<XunjianDetailV2> queryDetail = new QueryWrapper<XunjianDetailV2>();
            queryDetail.eq("XUNJIAN_RECORD_ID", xunjianRecordV2.getId());
            List<XunjianDetailV2> detailList = xunjianDetailServ.list(queryDetail);
            if (Objects.isNull(detailList) || detailList.isEmpty()) {
                logs.add(xunjianLogBean);
                continue;
            }

            List<XunjianDetailV2> collect = detailList.stream().filter(
                    e -> {
                        boolean found = !filterList.contains(e.getAssetId());
                        filterList.add(e.getAssetId());
                        return found;
                    }
            ).collect(Collectors.toList());

            xunjianLogBean.setAsset(collect);
            logs.add(xunjianLogBean);
        }

        return logs;
    }

    @Override
    public XunjianServerDetailBean queryXunjianDetail(String xcunjianRecordV2Id, String assetId) {
        QueryWrapper<XunjianDetailV2> queryDetail = new QueryWrapper<XunjianDetailV2>();
        queryDetail.eq("XUNJIAN_RECORD_ID", xcunjianRecordV2Id);
        queryDetail.eq("ASSET_ID", assetId);
        List<XunjianDetailV2> detailList = xunjianDetailServ.list(queryDetail);

        XunjianServerDetailBean detail = new XunjianServerDetailBean();

        if (detailList.isEmpty()) {
            return detail;
        }

        AssetMsgVo asset = assetServ.findMsgById(assetId);
        StringBuilder position = new StringBuilder("");
        position.append(asset.getOrgName());
        if (StrUtil.isNotEmpty(asset.getRoomName())) {
            position.append("-");
            position.append(asset.getRoomName());
        }
        if (StrUtil.isNotEmpty(asset.getCabinetName())) {
            position.append("-");
            position.append(asset.getCabinetName());
            position.append("中");
            position.append(asset.getPositionStr());
            position.append("U");
        }

        detail.setAppName(asset.getAssetName());
        detail.setIp(asset.getIp());
        detail.setAssetImage(asset.getAssetImage());
        detail.setSerialNumber(asset.getSerialNumber());
        detail.setSysVersion(asset.getOperationSystem());

        detail.setAssetPosition(position.toString());
        detail.setOnlineTime(asset.getOnlineTime());
        detail.setAssetSupplier(asset.getAssetSupplier());
        detail.setAssetVersion(asset.getAssetVersion());
        detail.setRemark(asset.getRemark());
        detail.setDetailList(detailList);

        return detail;
    }

    @Override
    public Collection<XunjianTab> getLogTab(String xcunjianRecordV2Id) {
        QueryWrapper<XunjianDetailV2> query = new QueryWrapper<XunjianDetailV2>();
        query.eq("XUNJIAN_RECORD_ID", xcunjianRecordV2Id);
        List<XunjianDetailV2> list = xunjianDetailServ.list(query);

        Map<String, XunjianTab> cacheMap = new HashMap<String, XunjianTab>();
        for (XunjianDetailV2 detail : list) {
            String assetId = detail.getAssetId();

            XunjianTab tab = cacheMap.get(assetId);
            if (Objects.isNull(tab)) {
                XunjianRecordV2 byId = getById(xcunjianRecordV2Id);
                tab = new XunjianTab();
                tab.setAssetId(assetId);
                tab.setId(detail.getId());
                tab.setAssetName(detail.getAssetName());
                tab.setRemark(byId.getRemark());
                cacheMap.put(assetId, tab);
            }
            if (detail.getNormalFlag() == 1) {
                //异常
                tab.setResult("异常");
                cacheMap.put(assetId, tab);
            }
            if (detail.getNormalFlag() == 2) {
                tab.setResult("含有二次确认项");
                cacheMap.put(assetId, tab);
            }
        }

        Collection<XunjianTab> values = cacheMap.values();
        for (XunjianTab value : values) {
            if (StrUtil.isEmpty(value.getResult())) {
                value.setResult("正常");
            }
        }

        return values;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeXunjianLog(String xunjianRecordId) {
        removeById(xunjianRecordId);
        QueryWrapper<XunjianDetailV2> query = new QueryWrapper<>();
        query.eq("XUNJIAN_RECORD_ID", xunjianRecordId);
        xunjianDetailServ.remove(query);
    }

    /**
     * 执行巡检
     *
     * @param assetId
     * @param targetList
     * @param id
     * @param xcunjianRecordV2
     * @param detailList
     */
    private void executeXunJian(String assetId, List<String> targetList, String id, XunjianRecordV2 xcunjianRecordV2, List<XunjianDetailV2> detailList) {
        BigDecimal normal = new BigDecimal(0);
        BigDecimal exception = new BigDecimal(0);

        Asset asset = assetServ.getById(assetId);
        Integer assetMode = asset.getAssetMode();
        Map<String, String> commandMap = new HashMap<>();

        List<String> collect = targetList.stream().distinct().collect(Collectors.toList());

        for (String target : collect) {
            String xunjianKey = target + ":" + assetMode + ":" + asset.getCollectionType();

            String targetName = xunjianV2Handler.getTargetName(xunjianKey, asset);
            String command = xunjianV2Handler.getCommand(xunjianKey);

            if (NET_ASSET_MODE.contains(assetMode)) {
                xunjianKey = target + ":" + assetMode;
                if (!AssetModeConst.B24.equals(asset.getAssetImage())) {
                    xunjianKey = target;
                }
                command = xunjianV2Handler.getCommand(xunjianKey);
                //发送telnet集合命令 统一发过去
                if (StrUtil.isEmpty(command)) {
                    continue;
                }
                commandMap.put(xunjianKey, command);
            } else {
                XunjianDetailV2 result = null;
                try {
                    result = xunjianV2Handler.xunjian(xunjianKey, asset, id, "");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                Boolean normalFlag = disposeResp(result, detailList, targetName, id, command, asset, target);

                if (normalFlag) {
                    normal = normal.add(new BigDecimal(1));
                } else {
                    exception = exception.add(new BigDecimal(1));
                }
            }
        }

        //批量处理telnet
        if (NET_ASSET_MODE.contains(asset.getAssetMode())) {
            Collection<String> values = commandMap.values();
            Set<String> adapterCodeList = commandMap.keySet();
            List<String> telnetResult = new ArrayList<>();

            try {
                telnetResult = outService.getTelnetResult(asset, values);
            } catch (Exception e) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_XUNJIAN, ErrorCodeEnum.WEB_XUNJIAN_TELNET_ERROR, asset.getIp(), e.getMessage()), e);
            }

            int i = 0;
            for (String xunjianKey : adapterCodeList) {
                String targetName = xunjianV2Handler.getTargetName(xunjianKey, asset);
                String command = xunjianV2Handler.getCommand(xunjianKey);
                //分析结果
                String orgMsg = OutServiceImpl.ERROR_FLAG;
                if (!telnetResult.isEmpty()) {
                    orgMsg = telnetResult.get(i);
                }
                XunjianDetailV2 result = null;
                if (!OutServiceImpl.ERROR_FLAG.equals(orgMsg)) {
                    result = xunjianV2Handler.xunjian(xunjianKey, asset, id, orgMsg);
                }
                i++;
                Boolean normalFlag = disposeResp(result, detailList, targetName, id, command, asset, xunjianKey.split(":")[0]);
                if (normalFlag) {
                    normal = normal.add(new BigDecimal(1));
                } else {
                    exception = exception.add(new BigDecimal(1));
                }
            }
        }

        //没有匹配上任何的东西
        if (normal.intValue() == 0 && exception.intValue() == 0) {
            XunjianDetailV2 temp = createErrorDetail(id, "没有选择此类型设备的巡检项", "--", asset);
            temp.setNormalFlagStr("");
            temp.setXunjianAdapter("");
            detailList.add(temp);
        }

        xcunjianRecordV2.setNormalNum(normal.intValue());
        xcunjianRecordV2.setExceptionNum(exception.intValue());
    }

    /**
     * 分析采集结果
     *
     * @param result
     * @param detailList
     * @param targetName
     * @param id
     * @param command
     * @param asset
     * @param adapterStr 前端传过来的项
     * @return
     */
    private Boolean disposeResp(XunjianDetailV2 result, List<XunjianDetailV2> detailList, String targetName, String id, String command, Asset asset, String adapterStr) {
        boolean resp = false;
        if (Objects.nonNull(result)) {
            if (result.getNormalFlag() == XunjianDetailV2.NORMAL_FLAG) {
                resp = true;
            } else if (result.getNormalFlag() == XunjianDetailV2.EXCEPTION_FLAG) {
                resp = false;
            }
            detailList.add(result);
        } else if (StrUtil.isNotEmpty(targetName)) {
            result = createErrorDetail(id, targetName, command, asset);
            result.setXunjianAdapter(adapterStr);
            detailList.add(result);
            resp = false;
        }
        return resp;
    }

    /**
     * 创建巡检错误的明细
     *
     * @param id
     * @param targetName
     * @param command
     * @param asset
     */
    private XunjianDetailV2 createErrorDetail(String id, String targetName, String command, Asset asset) {
        XunjianDetailV2 result = new XunjianDetailV2();
        result.setCreateTime(new Date());
        result.setModifyTime(new Date());
        result.setId(MyIdUtil.getId());
        result.setXunjianRecordId(id);
        result.setXunjianTargetItem(targetName);
        result.setCommand(command);
        result.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
        result.setNormalFlagStr("此项自动巡检报错，请手动巡检");
        //如果是网络设备，没有用户密码提示他错误原因
        if(NET_ASSET_MODE.contains(asset.getAssetMode()) &&StrUtil.isEmpty(asset.getLoginPwd())){
            result.setNormalFlagStr("此设备没有设定密码，请手动巡检");
        }
        result.setAssetMode(asset.getAssetMode());
        result.setAssetId(asset.getId());
        result.setAssetName(asset.getName());

        return result;
    }

    /**
     * 拼接报告
     * StringBuilder msg
     *
     * @return
     */
    private StringBuilder getStringBuilder(StringBuilder msg, List<XunjianDetailV2> xunjianDetailV2s, String assetId) {
        Asset asset = assetServ.getById(assetId);
        msg.append("【设备：");
        msg.append(asset.getName());
        msg.append("(");
        msg.append(asset.getIp());
        msg.append(")】");
        msg.append("\r\n");
        for (XunjianDetailV2 xunjianItem : xunjianDetailV2s) {
            msg.append("巡检项（");
            msg.append(xunjianItem.getXunjianTargetItem());
            msg.append("）命令：");
            msg.append("\r\n");
            msg.append(xunjianItem.getCommand());
            msg.append("\r\n");
            if (XunjianDetailV2.NORMAL_FLAG == xunjianItem.getNormalFlag()) {
                msg.append("系统提示结果：【正常✓】");
            } else if (XunjianDetailV2.EXCEPTION_FLAG == xunjianItem.getNormalFlag()) {
                msg.append("系统提示结果：【【★★异常★★】】");
            } else if (XunjianDetailV2.UN_KNOW_FLAG == xunjianItem.getNormalFlag()) {
                msg.append("系统提示结果：【【★★需要二次确认★★】】");
            }
            msg.append("\r\n");
            msg.append("输出：");
            msg.append("\r\n");
            msg.append(xunjianItem.getInputOrgStr());
            msg.append("\r\n");
            msg.append("----------------------------分割线----------------------------");
            msg.append("\r\n");
            msg.append("\r\n");
            msg.append("\r\n");
        }
        return msg;
    }


}