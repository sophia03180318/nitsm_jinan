package com.jcca.web2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.event.service.AlarmEventTypeService;
import com.jcca.web2.dto.XunjianJobDto;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.service.AssetModeService;
import com.jcca.web2.service.XunjianScheduleService;
import com.jcca.web2.vo.ItemVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

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
        List<XunjianSchedule> list = xunjianScheduleService.list(query);
        return ResultVoUtil.success(list);
    }

    @PostMapping("/job/add")
    @ApiOperation("新增巡检任务")
    public ResultVo<Object> jobAdd(@RequestBody @Validated XunjianJobDto dto) {
        String username = ShiroUtil.getSubject().getUsername();
        dto.setOperator(username);
        xunjianScheduleService.saveSchedule(dto);
        return ResultVoUtil.success();
    }

    @PostMapping("/job/remove/{id}")
    @ApiOperation("删除巡检任务")
    public ResultVo<Object> jobRemove(@PathVariable String id) {
        xunjianScheduleService.removeSchedule(id);
        return ResultVoUtil.success();
    }

    @GetMapping("/job/pause")
    @ApiOperation("暂停周期巡检任务")
    public ResultVo<Object> jobPause(String id) {
        xunjianScheduleService.pauseJob(id);
        return ResultVoUtil.success();
    }

    @GetMapping("/job/recover")
    @ApiOperation("恢复周期巡检任务")
    public ResultVo<Object> jobRecover(String id) {
        xunjianScheduleService.recoverJob(id);
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
