package com.jcca.web2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.web.event.service.AlarmEventTypeService;
import com.jcca.web2.dto.XunjianJobDto;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.service.AssetModeService;
import com.jcca.web2.service.XunjianScheduleService;
import com.jcca.web2.vo.ItemVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: hhw
 * @description: XunjianFinalController 主要是用来处理终版智能巡检
 * @date: 2025-05-18  15:38
 * @since: 2.1.6.0
 */
@RestController
@RequestMapping("/api/v2/xfinal")
@Api(tags = "终版巡检")
public class XunjianFinalController {

    @Resource
    private XunjianScheduleService xunjianScheduleService;
    @Resource
    private AssetModeService assetModeService;
    @Resource
    private AlarmEventTypeService alarmEventTypeService;


    @GetMapping("/job/list")
    @ApiOperation("巡检任务列表")
    public ResultVo<Object> jobList(Integer autoFlag, Integer jobState) {
        String username = ShiroUtil.getSubject().getUsername();
        QueryWrapper<XunjianSchedule> query = Wrappers.query();
        query.eq("OPERATOR", username);
        if (jobState != null) {
            query.eq("JOB_STATE", jobState);
        }
        if (autoFlag != null) {
            query.eq("AUTO_FLAG", autoFlag);
        }
        query.orderByDesc("JOB_ID");
        List<XunjianSchedule> list = xunjianScheduleService.list(query);
        if (list.isEmpty()) {
            return ResultVoUtil.success(list);
        }

        Map<String, Integer> stateMap = new HashMap<>();
        Map<String, Date> lastMap = new HashMap<>();
        Map<String, List<String>> map = new HashMap<>();
        for (XunjianSchedule schedule : list) {
            if (schedule.getAutoFlag() == 2) {
                if (stateMap.get(schedule.getJobId()) == null) {
                    stateMap.put(schedule.getJobId(), schedule.getJobState());
                } else {
                    if (schedule.getJobState() > stateMap.get(schedule.getJobId())) {
                        stateMap.put(schedule.getJobId(), schedule.getJobState());
                    }
                }
                if (lastMap.get(schedule.getJobId()) == null) {
                    lastMap.put(schedule.getJobId(), schedule.getLastTime());
                } else {
                    if (schedule.getLastTime() != null && schedule.getLastTime().after(lastMap.get(schedule.getJobId()))) {
                        lastMap.put(schedule.getJobId(), schedule.getLastTime());
                    }
                }

                List<String> strings = map.get(schedule.getJobId());
                if (strings == null) {
                    strings = new ArrayList<>();
                }
                strings.add(schedule.getCronTime());
                map.put(schedule.getJobId(), strings);
            }
        }

        Set<String> set = new HashSet<>();
        List<XunjianSchedule> resultList = new ArrayList<>();
        for (XunjianSchedule schedule : list) {
            if (stateMap.get(schedule.getJobId()) != null) {
                schedule.setJobState(stateMap.get(schedule.getJobId()));
            }
            if (lastMap.get(schedule.getJobId()) != null) {
                schedule.setLastTime(lastMap.get(schedule.getJobId()));
            }
            if (set.contains(schedule.getJobId())) {
                continue;
            }
            set.add(schedule.getJobId());
            if (schedule.getAutoFlag() == 2 && map.containsKey(schedule.getJobId())) {
                schedule.setCronList(map.get(schedule.getJobId()));
            }
            resultList.add(schedule);
        }
        return ResultVoUtil.success(resultList);
    }

    @PostMapping("/job/add")
    @ApiOperation("新增巡检任务")
    public ResultVo<Object> jobAdd(@RequestBody @Validated XunjianJobDto dto) {
        String username = ShiroUtil.getSubject().getUsername();
        dto.setOperator(username);
        xunjianScheduleService.addSchedule(dto);
        return ResultVoUtil.success();
    }

    @GetMapping("/job/begin")
    @ApiOperation("开始巡检")
    public ResultVo<Object> jobBegin(String jobId) {
        List<XunjianSchedule> list = xunjianScheduleService.findByJobId(jobId);
        if (list.size() > 1) {
            return ResultVoUtil.error(ResultEnum.ERROR.getCode(), "周期巡检不需要手动开始");
        }
        XunjianSchedule schedule = list.get(0);
        if (schedule.getAutoFlag() != 1) {
            return ResultVoUtil.error(ResultEnum.ERROR.getCode(), "只能开始手动巡检");
        }

        ThreadPoolExecutor executor = (ThreadPoolExecutor) SpringContextUtil.getBean(ThreadPoolEnum.xunjianExecutor);
        executor.execute(() -> {
            XunjianJobDto dto = new XunjianJobDto();
            dto.setId(list.get(0).getId());
            dto.setOperator(schedule.getOperator());
            xunjianScheduleService.beginXunjian(dto);
        });
        return ResultVoUtil.success();
    }

    @GetMapping("/job/remove")
    @ApiOperation("删除巡检任务")
    public ResultVo<Object> jobRemove(String jobId) {
        xunjianScheduleService.removeSchedule(jobId);
        return ResultVoUtil.success();
    }

    @PostMapping("/job/update")
    @ApiOperation("修改巡检任务")
    public ResultVo<Object> jobUpdate(@RequestBody @Validated XunjianJobDto dto) {
        String jobId = dto.getJobId();
        if (StringUtils.isEmpty(jobId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }

        List<XunjianSchedule> list = xunjianScheduleService.findByJobId(jobId);
        if (list.isEmpty()) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND);
        }

        String username = ShiroUtil.getSubject().getUsername();
        dto.setOperator(username);
        xunjianScheduleService.updateSchedule(dto);
        return ResultVoUtil.success();
    }

    @GetMapping("/job/pause")
    @ApiOperation("暂停周期巡检任务")
    public ResultVo<Object> jobPause(String jobId) {
        xunjianScheduleService.pauseJob(jobId);
        return ResultVoUtil.success();
    }

    @GetMapping("/job/recover")
    @ApiOperation("恢复周期巡检任务")
    public ResultVo<Object> jobRecover(String jobId) {
        xunjianScheduleService.recoverJob(jobId);
        return ResultVoUtil.success();
    }

    @GetMapping("/asset/list")
    @ApiOperation("资产分类列表")
    public ResultVo<Object> assetList() {
        List<ItemVo> list = xunjianScheduleService.getOrgModeAssetList();
        return ResultVoUtil.success(list);
    }

    @PostMapping("/target/list")
    @ApiOperation("指标分类列表")
    public ResultVo<Object> targetList(@RequestBody List<String> assetDesks) {
        List<ItemVo> resultList = new ArrayList<>();
        Map<Integer, String> modeMap = assetModeService.getModeMap();
        Set<String> keySet = new HashSet<>(assetDesks);
        for (String key : keySet) {
            String id = key + ",";
            String mode = modeMap.get(Integer.parseInt(key));
            ItemVo vo = new ItemVo();
            vo.setId(id);
            vo.setName(mode);

            List<ItemVo> list = alarmEventTypeService.listTypeByAssetDesk(id);
            if (!list.isEmpty()) {
                vo.setChildren(list);
                resultList.add(vo);
            }
        }

        return ResultVoUtil.success(resultList);
    }
}
