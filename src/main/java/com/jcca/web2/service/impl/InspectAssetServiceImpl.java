package com.jcca.web2.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ReceiveCollectDto;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.InspectAssetMapper;
import com.jcca.web2.dto.xunjian.*;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.service.InspectAssetService;
import com.jcca.web2.service.XunjianScheduleService;
import com.jcca.web2.vo.InspectAssetAndTarget;
import com.jcca.web2.vo.ItemVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jcca.web2.constant.Web2Const.*;
import static com.jcca.web2.service.XunjianCollectRun.*;

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
    private XunjianScheduleService xunjianScheduleService;

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
        String username = ShiroUtil.getSubject().getUsername();
        String inspectRecordId = XUNJIAN_JOB_RECORD.get(jobId);
        if (!StringUtils.isEmpty(inspectRecordId)) {
            Integer totalTarget = targetTotalMap.get(inspectRecordId);
            Integer countTarget = currentTargetCountMap.get(inspectRecordId);
            if (totalTarget == null || countTarget == null) {
                this.sendMsg(username, XunjianWSDto.WHOLE_PROCESS, jobId, 0);
            } else {
                BigDecimal process = new BigDecimal(countTarget).divide(new BigDecimal(totalTarget), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                int i = process.intValue();
                i = Math.min(i, 100);
                this.sendMsg(username, XunjianWSDto.WHOLE_PROCESS, jobId, i);
            }
            this.sendMsg(username, XunjianWSDto.XUNJIANING_ASSET, jobId, inspectRecordId, currentAssetIdMap.get(inspectRecordId), 2); // 当前巡检资产
        }

        List<ItemVo> list = inspectAssetMapper.getAllCheckedAsset(jobId);
        QueryWrapper<InspectAsset> query = Wrappers.query();
        for (ItemVo itemVo : list) {
            query.eq("asset_id", itemVo.getId());
            query.eq("JOB_ID", jobId);
            query.in("INSPECT_STATE", Arrays.asList(1, 2));
            int count = this.count(query);
            if (count > 0) {
                itemVo.setStatus(2);
            }
        }
        return list;
    }

    private void sendMsg(String operator, Integer msgType, String jobId, String id, String name, Integer status) {
        XunjianWSDto wsDto = new XunjianWSDto();
        wsDto.setUsername(operator);
        wsDto.setMsgType(msgType);
        XunjianWSDto msg = new XunjianWSDto();
        msg.setJobId(jobId);
        msg.setId(id);
        msg.setName(name);
        msg.setStatus(status);
        msg.setCount(0);
        wsDto.setMessage(msg);
        xunjianScheduleService.sendWsMsg(wsDto);
    }

    private void sendMsg(String operator, Integer msgType, String jobId, Integer status) {
        XunjianWSDto wsDto = new XunjianWSDto();
        wsDto.setUsername(operator);
        wsDto.setMsgType(msgType);
        XunjianWSDto msg = new XunjianWSDto();
        msg.setJobId(jobId);
        msg.setId("100");
        msg.setName("进度条");
        msg.setStatus(status);
        msg.setCount(0);
        wsDto.setMessage(msg);
        xunjianScheduleService.sendWsMsg(wsDto);
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
        String inspectRecordId = XUNJIAN_JOB_RECORD.get(jobId);
        if (StringUtils.isEmpty(inspectRecordId)) {
            return new ArrayList<>();
        }
        return inspectAssetMapper.getTargetAssetInfo(inspectRecordId, eventTypeId);
    }

    @Override
    public List<InspectTargetDetailInfo> getAssetTargetInfo(String jobId, String assetId) {
        String inspectRecordId = XUNJIAN_JOB_RECORD.get(jobId);
        if (StringUtils.isEmpty(inspectRecordId)) {
            return new ArrayList<>();
        }
        return inspectAssetMapper.getAssetTargetInfo(inspectRecordId, assetId);
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
            CollectExecReq req = new CollectExecReq();
            req.setInspectRecordId(asset.getInspectRecordId());
            req.setAssetId(assetId);
            respBody = collectAgent.sendPostToCenter(XUNJIAN_CENTER_URI, JSONUtil.toJsonStr(req), XUNJIAN_TIME_OUT);
        } catch (CollectAgencyException e) {
            this.sendAll2Queue(asset, e.getMsg());
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "巡检采集异常", e);
            return;
        }
        if (!JSONUtil.isJson(respBody)) {
            this.sendAll2Queue(asset, "巡检采集数据格式错误");
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "巡检采集数据格式错误", respBody);
            return;
        }
        JSONObject jsonObject = JSONUtil.parseObj(respBody);
        Object o = jsonObject.get("code");
        if (!"success".equals(o)) {
            this.sendAll2Queue(asset, "巡检采集不成功");
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "巡检采集不成功", respBody);
            return;
        }
        o = jsonObject.get("body");
        JSONObject body = JSONUtil.parseObj(o.toString());
        CollectExecResp collectExecResp = JSONUtil.toBean(body.toString(), CollectExecResp.class);
        List<CollectExecResult> execRespList = collectExecResp.getExecRespList();
        execRespList.sort(Comparator.comparing(CollectExecResult::getCode));
        Set<String> ipSet = new HashSet<>();
        Set<String> assetIdSet = new HashSet<>();
        Set<String> idFlagSet = new HashSet<>();
        ExecutorService executor = Executors.newFixedThreadPool(execRespList.size());

        try {
            CountDownLatch latch = new CountDownLatch(execRespList.size());

            for (CollectExecResult execResult : execRespList) {
                AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "巡检采集返回数据", execResult);
                Integer code = execResult.getCode();
                if (code == 2) {
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

                if (code == 3 || code == 4) {
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
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
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

    private final List<String> targets = Arrays.asList(SYSPORT_TARGET_ARR);

    private void send2Queue(InspectAsset asset, String msg) {
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
            dto.setInspectState(Web2Const.INSPECT_ERROR);
            dto.setResultMsg(msg);
            dto.setEventTypeId(inspectAsset.getEventTypeId());
            IEvent event = new IEvent();
            event.setXunjianDataDto(dto);
            try {
                Web2Const.XUNJIAN_COLLECT_QUEUE.put(event);
            } catch (InterruptedException ignored) {

            }
        }
    }

    private void sendAll2Queue(InspectAsset asset, String msg) {
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
            dto.setInspectState(Web2Const.INSPECT_ERROR);
            dto.setResultMsg(msg);
            dto.setEventTypeId(inspectAsset.getEventTypeId());
            IEvent event = new IEvent();
            event.setXunjianDataDto(dto);
            try {
                Web2Const.XUNJIAN_COLLECT_QUEUE.put(event);
            } catch (InterruptedException ignored) {

            }
        }
    }
}
