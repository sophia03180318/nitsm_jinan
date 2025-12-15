package com.jcca.web2.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ReceiveCollectDto;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.web2.constant.XunJianConst;
import com.jcca.web2.dao.InspectAssetMapper;
import com.jcca.web2.dto.xunjian.*;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.enums.xunjian.CollectionStatus;
import com.jcca.web2.enums.xunjian.InspectionStatus;
import com.jcca.web2.service.InspectAssetService;
import com.jcca.web2.service.xunjian.InspectSessionManager;
import com.jcca.web2.service.xunjian.XunjianNotifier;
import com.jcca.web2.vo.InspectAssetAndTarget;
import com.jcca.web2.vo.ItemVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.jcca.web2.constant.Web2Const.*;

/**
 * @author HanHW
 * @description 巡检管理服务
 * @className InspectAssetServiceImpl
 * @date 2025/5/19 17:32
 * @since 2.1.6.0
 */
@Service
public class InspectAssetServiceImpl extends ServiceImpl<InspectAssetMapper, InspectAsset> implements InspectAssetService {


    @Resource
    private InspectAssetMapper inspectAssetMapper;
    @Resource
    private CollectAgent collectAgent;
    @Resource
    private RedisService redisService;
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private InspectSessionManager sessionManager;

    @Resource
    private XunjianNotifier notifier;

    @Override
    public List<InspectAsset> getInspectAssets(List<String> assetIds) {
        return inspectAssetMapper.getInspectAssets(assetIds);
    }

    @Override
    public void removeByJobId(String jobId) {
        QueryWrapper<InspectAsset> query1 = Wrappers.query();
        query1.eq("JOB_ID", jobId);
        this.remove(query1);
    }

    @Override
    public InspectAssetAndTarget getCheckedAssetTarget(String jobId) {
        InspectAssetAndTarget result = new InspectAssetAndTarget();
        result.setAssetList(this.getAllCheckedAsset(jobId));
        result.setTargetList(this.getAllCheckedTarget(jobId));
        return result;
    }

    @Override
    public List<ItemVo> getAllCheckedAsset(String jobId) {
        String inspectRecordId = XunJianConst.XUNJIAN_JOB_RECORD.get(jobId);
        if (!StringUtils.isEmpty(inspectRecordId)) {
            InspectSession session = sessionManager.getSession(inspectRecordId);
            if (null != session) {
                InspectBaseDataWsVo snapshot = session.buildSnapshot(session.getSchedule().getJobId(), false, false);
                notifier.sendSnapshot(snapshot, session.getSchedule().getOperator());
            }
        }

        // 查询当前所有job下的巡检的资产，以及状态
        List<ItemVo> list = inspectAssetMapper.getAllCheckedAsset(jobId);
        if (!StringUtils.isEmpty(inspectRecordId) && XunJianConst.currentAssetIdMap.get(inspectRecordId) != null) {
            for (ItemVo itemVo : list) {
                // 如果缓存中还存在，则设置为巡检中
                if (itemVo.getId().equals(XunJianConst.currentAssetIdMap.get(inspectRecordId))) {
                    itemVo.setStatus(Integer.valueOf(InspectionStatus.INSPECTING.getCode()));
                    break;
                }
            }
        }
        return list;
    }

    @Override
    public List<ItemVo> getAllCheckedTarget(String jobId) {
        List<ItemVo> resultList = new ArrayList<>();
        List<ItemVo> assetDesks = inspectAssetMapper.getDesksByJobId(jobId);
        List<InspectAsset> list = inspectAssetMapper.getAllCheckedTarget(jobId);
        Map<Integer, List<InspectAsset>> collect = list.stream().collect(Collectors.groupingBy(InspectAsset::getAssetDesk));
        for (ItemVo desk : assetDesks) {
            ItemVo vo = new ItemVo();
            vo.setId(desk.getId() + ",");
            vo.setName(desk.getName());
            List<InspectAsset> inspectAssets = collect.get(Integer.parseInt(desk.getId()));
            List<ItemVo> children = new ArrayList<>();
            for (InspectAsset asset : inspectAssets) {
                ItemVo vo1 = new ItemVo();
                vo1.setId(asset.getEventTypeId());
                vo1.setName(asset.getEventTypeName());
                children.add(vo1);
            }
            vo.setChildren(children);
            resultList.add(vo);
        }


        return resultList;
    }

    @Override
    public List<InspectAsset> getAllByJobId(String jobId) {
        return inspectAssetMapper.getAllByJobId(jobId);
    }

    @Override
    public List<ItemVo> getTargetStatus(String jobId) {
        return inspectAssetMapper.getTargetStatus(jobId);
    }

    @Override
    public List<InspectTargetDetailInfo> getTargetAssetInfo(String jobId, String eventTypeId) {
        String inspectRecordId = XunJianConst.XUNJIAN_JOB_RECORD.get(jobId);
        if (StringUtils.isEmpty(inspectRecordId)) {
            return new ArrayList<>();
        }
        return inspectAssetMapper.getTargetAssetInfo(inspectRecordId, eventTypeId);
    }

    @Override
    public List<InspectTargetDetailInfo> getAssetTargetInfo(String jobId, String assetId, String status) {
        String inspectRecordId = XunJianConst.XUNJIAN_JOB_RECORD.get(jobId);
        if (StringUtils.isEmpty(inspectRecordId)) {
            return new ArrayList<>();
        }
        return inspectAssetMapper.getAssetTargetInfo(inspectRecordId, assetId, status);
    }

    private List<String> getCategoryList(String assetId, String jobId) {
        List<String> categoryList = inspectAssetMapper.getCategoryList(assetId, jobId);
        return categoryList;
    }

    /**
     * 巡检实时采集
     *
     * @return
     */
    @Override
    public void xunjianCollect(InspectAsset asset) {
        String assetId = asset.getAssetId();
        String respBody = "";
        try {
            List<String> categoryList = this.getCategoryList(asset.getAssetId(), asset.getJobId());
            CollectExecReq req = new CollectExecReq();
            req.setInspectRecordId(asset.getInspectRecordId());
            req.setAssetId(assetId);
            req.setCategoryList(categoryList);
            respBody = collectAgent.sendPostToCenter(XunJianConst.XUNJIAN_CENTER_URI, JSONUtil.toJsonStr(req), XunJianConst.XUNJIAN_TIME_OUT);
        } catch (CollectAgencyException e) {
            this.sendAll2Queue(asset, e.getMsg());
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "巡检采集异常", e);
            return;
        }
        if (!JSONUtil.isJson(respBody)) {
            this.sendAll2Queue(asset, "巡检采集数据格式错误：" + respBody);
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "巡检采集数据格式错误", respBody);
            return;
        }
        JSONObject jsonObject = JSONUtil.parseObj(respBody);
        Object o = jsonObject.get("code");
        if (!"success".equals(o)) {
            this.sendAll2Queue(asset, "巡检采集不成功：" + jsonObject.get("msg").toString());
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "巡检采集不成功", respBody);
            return;
        }
        o = jsonObject.get("body");
        JSONObject body = JSONUtil.parseObj(o.toString());
        CollectExecResp collectExecResp = JSONUtil.toBean(body.toString(), CollectExecResp.class);
        List<CollectExecResult> execRespList = collectExecResp.getExecRespList();
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "巡检结果值", "jobId：" + asset.getJobId() + "|inspectRecordId：" + asset.getInspectRecordId() + "|assetId：" + asset.getAssetId() + "|size：" + execRespList.size());
        execRespList.sort(Comparator.comparing(CollectExecResult::getCode));
        Set<String> ipSet = new HashSet<>();
        Set<String> assetIdSet = new HashSet<>();
        Set<String> idFlagSet = new HashSet<>();
        ExecutorService executor = null;
        try {
            executor = Executors.newFixedThreadPool(execRespList.size());
            CountDownLatch latch = new CountDownLatch(execRespList.size());

            for (CollectExecResult execResult : execRespList) {
                AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "巡检采集返回数据", execResult);
                Integer code = execResult.getCode();

                if (code == CollectionStatus.IS_ERROR.getCode()) {
                    latch.countDown();
                    if (!assetIdSet.contains(asset.getAssetId())) {
                        assetIdSet.add(asset.getAssetId());
                        this.sendAll2Queue(asset, execResult.getMsg());
                    }
                    continue;
                }
                ReceiveCollectDto dto = execResult.getResult();
                if (dto == null) {
                    latch.countDown();
                    continue;
                }

                if (code == CollectionStatus.IS_FAIL.getCode() || code == CollectionStatus.IS_FAIL_DOUBLE.getCode()) {
                    latch.countDown();
                    String flag = assetId + dto.getCategory();
                    if (idFlagSet.contains(flag) || !Arrays.asList(SYSPORT_DS_ARR).contains(dto.getCategory())) {
                        continue;
                    }
                    idFlagSet.add(flag);
                    this.send2Queue(asset, execResult.getMsg());
                    continue;
                }

                String content1 = dto.getContent();
                if (StringUtils.isEmpty(content1)) {
                    latch.countDown();
                    continue;
                }
                executor.execute(() -> {
                    try {
                        SendPingAlarmReq statusResult = execResult.getStatusResult();
                        if (!ipSet.contains(statusResult.getAssetIp())) {
                            ipSet.add(statusResult.getAssetIp());
                            statusResult.setInspectRecordId(asset.getInspectRecordId());
                            redisService.convertAndSend(RedisQueueConst.ALARM_QUEUE, JSONUtil.toJsonStr(statusResult));
                        }
                        IAdapter adapter1 = dataProcessManager.getAdapter(dto.getCategory());
                        JSONArray jsonArray1 = JSONUtil.parseArray(content1);
                        adapter1.dispose(jsonArray1);
                    } catch (Exception ignored) {

                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        } catch (Exception ignored) {

        } finally {
            if (null != executor) {
                executor.shutdownNow();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private final List<String> targets = Arrays.asList(SYSPORT_TARGET_ARR);

    private void send2Queue(InspectAsset asset, String msg) {
        String flag = XunJianConst.XUNJIAN_JOB_RECORD.get(asset.getJobId());
        if (flag == null) {
            return;
        }
        QueryWrapper<InspectAsset> query = Wrappers.query();
        query.eq("JOB_ID", asset.getJobId());
        query.eq("ASSET_ID", asset.getAssetId());
        query.in("INSPECT_STATE", Arrays.asList("1", "2"));
        List<InspectAsset> list = this.list(query);
        for (InspectAsset inspectAsset : list) {
            if (!targets.contains(inspectAsset.getTargetItem())) {
                continue;
            }
            XunjianDataDto dto = new XunjianDataDto();
            dto.setInspectRecordId(asset.getInspectRecordId());
            dto.setAssetId(asset.getAssetId());
            dto.setTargetItem(inspectAsset.getTargetItem());
            dto.setInspectValue("--");
            dto.setInspectState(InspectionStatus.INSPECT_ERROR.getCode());
            dto.setResultMsg(msg);
            dto.setEventTypeId(inspectAsset.getEventTypeId());
            IEvent event = new IEvent();
            event.setXunjianDataDto(dto);
            try {
                XunJianConst.putXunJianCollectQueue(asset.getInspectRecordId(), event);
            } catch (Exception ignored) {

            }
        }
    }

    private void sendAll2Queue(InspectAsset asset, String msg) {
        String flag = XunJianConst.XUNJIAN_JOB_RECORD.get(asset.getJobId());
        if (flag == null) {
            return;
        }
        QueryWrapper<InspectAsset> query = Wrappers.query();
        query.eq("JOB_ID", asset.getJobId());
        query.eq("ASSET_ID", asset.getAssetId());
        List<InspectAsset> list = this.list(query);
        for (InspectAsset inspectAsset : list) {
            XunjianDataDto dto = new XunjianDataDto();
            dto.setInspectRecordId(asset.getInspectRecordId());
            dto.setAssetId(asset.getAssetId());
            dto.setTargetItem(inspectAsset.getTargetItem());
            dto.setInspectValue("--");
            dto.setInspectState(InspectionStatus.INSPECT_ERROR.getCode());
            dto.setResultMsg(msg);
            dto.setEventTypeId(inspectAsset.getEventTypeId());
            IEvent event = new IEvent();
            event.setXunjianDataDto(dto);
            try {
                XunJianConst.putXunJianCollectQueue(asset.getInspectRecordId(), event);
            } catch (Exception ignored) {

            }
        }
    }
}
