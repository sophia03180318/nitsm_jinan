package com.jcca.web.asset.controller;

import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.service.RoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName ApiRoomController
 * @Description 机房前端接口
 * @Date 2020/4/27 10:07
 * @Author hanwone
 */
@RestController
@RequestMapping("/api/room")
@Api(tags = "机房相关接口")
@Slf4j
public class ApiRoomController {

    @Resource
    private RoomService roomService;

    /**
     * 获取组织下所有机房
     *
     * @param orgId
     * @return
     */
    @GetMapping("/list/{orgId}")
    @ApiOperation(value = "获取组织机房列表")
    public ResultVo list(@PathVariable String orgId) {
        return ResultVoUtil.success(roomService.listByOrgId(orgId));
    }
}
