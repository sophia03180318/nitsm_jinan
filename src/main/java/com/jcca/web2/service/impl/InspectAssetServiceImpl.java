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
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ReceiveCollectDto;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.InspectAssetMapper;
import com.jcca.web2.dto.xunjian.*;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.service.InspectAssetService;
import com.jcca.web2.service.InspectRecordService;
import com.jcca.web2.service.XunjianScheduleService;
import com.jcca.web2.vo.InspectAssetAndTarget;
import com.jcca.web2.vo.ItemVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jcca.web2.constant.Web2Const.*;
import static com.jcca.web2.service.XunjianCollectRun.currentTargetCountMap;
import static com.jcca.web2.service.XunjianCollectRun.targetTotalMap;

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
    private AlarmInfoService alarmInfoService;
    @Resource
    private InspectRecordService inspectRecordService;
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
        List<InspectRecord> records = inspectRecordService.findByJobId(jobId);
        if (!records.isEmpty()) {
            InspectRecord inspectRecord = records.get(0);
            String inspectRecordId = inspectRecord.getId();
            Integer totalTarget = targetTotalMap.get(inspectRecordId);
            Integer countTarget = currentTargetCountMap.get(inspectRecordId);
            if (totalTarget == null || countTarget == null) {
                this.sendMsg(inspectRecord.getModeType(), XunjianWSDto.WHOLE_PROCESS, jobId, 0);
            } else {
                BigDecimal process = new BigDecimal(countTarget).divide(new BigDecimal(totalTarget), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                this.sendMsg(inspectRecord.getModeType(), XunjianWSDto.WHOLE_PROCESS, jobId, process.intValue());
            }
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

    private void sendMsg(String operator, Integer msgType, String jobId, Integer status) {
        if (Web2Const.XUNJIAN_JOB_RECORD.get(jobId) == null) {
            return;
        }
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
        String inspectRecordId = Web2Const.XUNJIAN_JOB_RECORD.get(jobId);
        if (StringUtils.isEmpty(inspectRecordId)) {
            return new ArrayList<>();
        }
        return inspectAssetMapper.getTargetAssetInfo(inspectRecordId, eventTypeId);
    }

    @Override
    public List<InspectTargetDetailInfo> getAssetTargetInfo(String jobId, String assetId) {
        return inspectAssetMapper.getAssetTargetInfo(jobId, assetId);
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
            return;
        }
        if (!JSONUtil.isJson(respBody)) {
//            this.send2Queue(asset, respBody, Web2Const.INSPECT_ERROR);
            return;
        }
        JSONObject jsonObject = JSONUtil.parseObj(respBody);
        Object o = jsonObject.get("code");
        if (!"success".equals(o)) {
//            this.send2Queue(asset, jsonObject.get("msg").toString(), Web2Const.INSPECT_ERROR);
            return;
        }

        o = jsonObject.get("body");
        JSONObject body = JSONUtil.parseObj(o.toString());
        CollectExecResp collectExecResp = JSONUtil.toBean(body.toString(), CollectExecResp.class);
        List<CollectExecResult> execRespList = collectExecResp.getExecRespList();
        execRespList.sort(Comparator.comparing(CollectExecResult::getCode));
        Set<String> ipSet = new HashSet<>();
        Set<String> idFlagSet = new HashSet<>();
        Set<String> assetIdSet = new HashSet<>();
        for (CollectExecResult execResult : execRespList) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "巡检采集返回数据", execResult);
            Integer code = execResult.getCode();
            if (code == 2) {
                if (!assetIdSet.contains(asset.getAssetId())) {
                    this.sendAll2Queue(asset, execResult.getMsg());
                    assetIdSet.add(asset.getAssetId());
                }
                continue;
            }
            ReceiveCollectDto dto = execResult.getResult();
            if (dto == null) {
                continue;
            }

            if (code == 3 || code == 4) {
                String flag = assetId + dto.getCategory();
                if (idFlagSet.contains(flag) || !ReceiveCollectConst.SYS_PORT.equals(dto.getCategory())) {
                    continue;
                }
                idFlagSet.add(flag);

                try {
                    TimeUnit.SECONDS.sleep(5L);
                } catch (InterruptedException ignored) {

                }

                this.send2Queue(asset, execResult.getMsg());
                continue;
            }

            String content1 = dto.getContent();
            if (content1 == null) {
                continue;
            }
            SendPingAlarmReq statusResult = execResult.getStatusResult();
            if (!ipSet.contains(statusResult.getAssetIp())) {
                ipSet.add(statusResult.getAssetIp());
                statusResult.setInspectRecordId(asset.getInspectRecordId());
                redisService.convertAndSend(RedisQueueConst.ALARM_QUEUE, JSONUtil.toJsonStr(statusResult));
            }
            IAdapter adapter1 = dataProcessManager.getAdapter(dto.getCategory());
            JSONArray jsonArray1 = JSONUtil.parseArray(content1);
            adapter1.dispose(jsonArray1);
        }
        // 巡检结束
        if (asset.getInspectTotal().intValue() == asset.getInspectNow().intValue()) {
            this.checkStatusTarget(asset.getJobId(), asset.getInspectRecordId());

            IEvent event = new IEvent();
            event.setXunjianIsFinish(1);
            event.setInspectRecordId(asset.getInspectRecordId());
            try {
                Web2Const.XUNJIAN_COLLECT_QUEUE.put(event);
            } catch (InterruptedException ignored) {

            }
        }
    }

    // 状态类单独处理
    private void checkStatusTarget(String jobId, String inspectRecordId) {
        QueryWrapper<InspectAsset> query1 = Wrappers.query();
        query1.eq("JOB_ID", jobId);
        query1.in("INSPECT_STATE", Arrays.asList("1", "2"));
        List<InspectAsset> list1 = this.list(query1);
        for (InspectAsset inspectAsset : list1) {
            List<String> list = Arrays.asList(ALARM_TARGET_ARR);
            if (!list.contains(inspectAsset.getTargetItem())) {
                continue;
            }
            inspectAsset.setInspectRecordId(inspectRecordId);

            QueryWrapper<AlarmInfo> query = Wrappers.query();
            query.eq("ASSET_ID", inspectAsset.getAssetId());
            query.eq("ALARM_CODE", inspectAsset.getTargetItem());
            query.eq("ALARM_STATE", 1);
            query.eq("BLANK", 1);
            List<AlarmInfo> infos = alarmInfoService.list(query);
            if (infos.isEmpty()) {
                inspectAsset.setInspectValue("1");
                inspectAsset.setInspectState(Web2Const.INSPECTED);
                inspectAsset.setResultMsg("正常");
                this.send2Queue(inspectAsset);
                continue;
            }
            for (AlarmInfo info : infos) {
                inspectAsset.setInspectValue("-1");
                inspectAsset.setInspectState(Web2Const.INSPECT_ERROR);
                inspectAsset.setResultMsg(info.getDescription());
                inspectAsset.setAlarmId(info.getId());
                this.send2Queue(inspectAsset);
            }
        }
    }

    private void send2Queue(InspectAsset asset) {
        XunjianDataDto dto = new XunjianDataDto();
        dto.setInspectRecordId(asset.getInspectRecordId());
        dto.setAssetId(asset.getAssetId());
        dto.setTargetItem(asset.getTargetItem());
        dto.setInspectValue(asset.getInspectValue());
        dto.setInspectState(asset.getInspectState());
        dto.setResultMsg(asset.getResultMsg());
        dto.setAlarmId(asset.getAlarmId());
        dto.setEventTypeId(asset.getEventTypeId());
        IEvent event = new IEvent();
        event.setInspectRecordId(asset.getInspectRecordId());
        event.setStatus(Web2Const.INSPECT_ERROR.equals(asset.getInspectState()) ? -1 : 1);
        event.setXunjianDataDto(dto);
        Web2Const.XUNJIAN_COLLECT_QUEUE.add(event);
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
