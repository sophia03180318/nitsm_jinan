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
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.InspectAssetMapper;
import com.jcca.web2.dto.xunjian.*;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.service.InspectAssetService;
import com.jcca.web2.vo.InspectAssetAndTarget;
import com.jcca.web2.vo.ItemVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
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
    public List<InspectTargetDetailInfo> getTargetAssetInfo(String jobId, String targetItem) {
        String inspectRecordId = Web2Const.XUNJIAN_JOB_RECORD.get(jobId);
        if (StringUtils.isEmpty(inspectRecordId)) {
            return new ArrayList<>();
        }
        targetItem = targetItem + "%";
        return inspectAssetMapper.getTargetAssetInfo(inspectRecordId, targetItem);
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
            this.send2Queue(asset, e.getMsg(), Web2Const.INSPECT_ERROR);
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
        for (CollectExecResult execResult : execRespList) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "巡检采集返回数据", execResult);
//            Integer code = execResult.getCode();
//            if (code == 4) {
//                try {
//                    TimeUnit.SECONDS.sleep(5L);
//                } catch (InterruptedException ignored) {
//
//                }
//                this.send2Queue(asset, execResult.getMsg(), Web2Const.INSPECTED);
//                continue;
//            }
//            if (code == 2 || code == 3) {
//                try {
//                    TimeUnit.SECONDS.sleep(5L);
//                } catch (InterruptedException ignored) {
//
//                }
//                this.send2Queue(asset, execResult.getMsg(), Web2Const.INSPECT_ERROR);
//                continue;
//            }
            ReceiveCollectDto dto = execResult.getResult();
            if (dto == null) {
                continue;
            }
            String content1 = dto.getContent();
            IAdapter adapter1 = dataProcessManager.getAdapter(dto.getCategory());
            JSONArray jsonArray1 = JSONUtil.parseArray(content1);
            adapter1.dispose(jsonArray1);

            SendPingAlarmReq statusResult = execResult.getStatusResult();
            if (ipSet.contains(statusResult.getAssetIp())) {
                continue;
            }
            ipSet.add(statusResult.getAssetIp());
            statusResult.setInspectRecordId(asset.getInspectRecordId());
            redisService.convertAndSend(RedisQueueConst.ALARM_QUEUE, JSONUtil.toJsonStr(statusResult));
        }
    }

    private final List<String> targets = Arrays.asList(STATUS_TARGET_ARR);

    private void send2Queue(InspectAsset asset, String msg, String inspectState) {
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
            dto.setInspectState(inspectState);
            dto.setResultMsg(msg);
            IEvent event = new IEvent();
            event.setXunjianDataDto(dto);
            try {
                Web2Const.XUNJIAN_COLLECT_QUEUE.put(event);
            } catch (InterruptedException ignored) {

            }
        }
    }
}
