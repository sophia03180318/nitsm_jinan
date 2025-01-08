package com.jcca.web2.controller;

import com.jcca.admin.biz.entity.RoomReq;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.service.RoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HanHW
 * @description 机房管理V2
 * @className RoomControllerV2
 * @date 2023/11/30 14:29
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/room")
@Api(tags = "机房管理V2")
public class RoomControllerV2 {

    @Resource
    private RoomService roomService;


    @GetMapping("/list")
    @ApiOperation("获取机房列表")
    public ResultVo<Object> getList(String orgTreeId) {

        List<Room> list = roomService.getRoomListV2(orgTreeId);

        return ResultVoUtil.success(list);
    }

    @PostMapping("/save")
    @RequiresPermissions("api:v2:room:save")
    @ApiOperation("保存机房信息")
    @ActionLog(name = "保存机房信息", title = "机房管理", key = LogTypeConstant.ADD)
    public ResultVo<Object> save(@Validated @RequestBody RoomReq req) {
        return roomService.saveRoomsInOrg(req);
    }

    @PostMapping("/del/{id}")
    @RequiresPermissions("api:v2:room:del")
    @ApiOperation("删除机房信息")
    @ActionLog(name = "删除机房信息", title = "机房管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> del(@PathVariable String id) {
        return roomService.delById(id);
    }
}
