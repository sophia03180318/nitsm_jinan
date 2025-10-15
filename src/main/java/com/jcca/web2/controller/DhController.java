package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.common.entity.DhFlag;
import com.jcca.web.common.service.DeviceService;
import com.jcca.web.common.service.DhFlagService;
import com.jcca.web.common.service.bean.DhFlagVo;
import com.jcca.web.common.service.bean.DhNodeVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 动环前端控制接口
 * @author: sophia
 * @create: 2025/10/10 10:42
 **/

@Api(tags = "动环配置")
@RestController
@RequestMapping("/api/v2/dh")
public class DhController {
    @Resource
    private DeviceService deviceService;
    @Resource
    private DhFlagService flagService;

    //删除规则
    @GetMapping("/delete/{eventId}")
    @ApiOperation("删除规则")
    public ResultVo<Object> delete(@PathVariable String eventId) {
        QueryWrapper<DhFlag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("EVENT_ID", eventId);
        flagService.remove(queryWrapper);
        return ResultVoUtil.success("删除成功");
    }

    //新增规则
    @PostMapping("/add")
    @ApiOperation("新增规则")
    public ResultVo<Object> add(@RequestBody DhFlagVo flagVo) {
        delete(flagVo.getEventId());
        String eventId = flagVo.getEventId();
        String flag = flagVo.getStatusFlag();
        List<String> idList = flagVo.getIdList();
        if (ObjectUtil.isNotNull(flagVo.getType())&&"PUB".equals(flagVo.getType())){
            flagService.savePub(flag,eventId);
            return ResultVoUtil.success("新增成功");
        }
        if (idList != null && idList.size() > 0) {
            for (String id : idList) {
                DhFlag dhFlag = new DhFlag();
                dhFlag.setFlagWord(flag);
                dhFlag.setEventId(eventId);
                dhFlag.setDeviceId(id);
                flagService.save(dhFlag);
            }
        }
        return ResultVoUtil.success("新增成功");
    }


    //获取设备选取树
    @GetMapping("/getDeviceTree")
    @ApiOperation("获取设备选取树")
    public ResultVo<Object> getDeviceTree() {
        List<DhNodeVo> deviceTree = deviceService.getDeviceTree();
        return ResultVoUtil.success(deviceTree);
    }


    //获取已选择设备
    @GetMapping("/getDevices/{eventId}")
    @ApiOperation("获取被选中的设备")
    public ResultVo<Object> getDevices(@PathVariable String eventId) {
        List<DhNodeVo> deviceTree = deviceService.getDevices(eventId);
        return ResultVoUtil.success(deviceTree);
    }

    //获取设备测点
    @GetMapping("/getProperty/{deviceId}")
    @ApiOperation("获取设备测点")
    public ResultVo<Object> getProperty(@PathVariable String deviceId) {
        List<DhNodeVo> deviceTree = deviceService.getProperty(deviceId);
        return ResultVoUtil.success(deviceTree);
    }

}