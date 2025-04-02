package com.jcca.web2.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.admin.system.entity.SysFolder;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysFileService;
import com.jcca.admin.system.service.SysFolderService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.handbook.controller.bean.FolderQueryReq;
import com.jcca.web.handbook.vo.FolderVo;
import com.jcca.web2.entity.FileRelate;
import com.jcca.web2.vo.MaintainHandBookVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description: 维护手册
 * @author: sophia
 * @create: 2023/11/16 10:33
 **/
@Api(tags = "维护手册相关接口V2版本")
@RestController
@RequestMapping("/api/v2/maintain")
public class ApiMaintainHandBookControllerV2 {

    @Resource
    private SysFileService fileService;
    @Resource
    private SysFolderService folderService;
    @Resource
    private CabinetService cabinetServ;
    @Resource
    private SysOrgService orgService;
    @Resource
    private RoomService roomService;


    @ApiOperation(value = "查询维护手册关联元素")
    @GetMapping("/getItem")
    public ResultVo<Object> getItem(String cabinetId) {

        Cabinet one = cabinetServ.getById(cabinetId);
        if (Objects.isNull(one)) {
            return ResultVoUtil.error("未查询到机柜：" + cabinetId);
        }
        List<FileRelate> voList = cabinetServ.findAssetInCabinet(cabinetId);
        FileRelate vo = new FileRelate();
        vo.setItemId(one.getId());
        vo.setItemName(one.getName());
        vo.setItemType(Integer.parseInt(OrgTypeConst.CABINET + ""));
        voList.add(vo);

        return ResultVoUtil.success(voList);
    }

    /**
     * 维护手册文件分页查询
     *
     * @param req
     * @return ResultVo
     */
    @ApiOperation(value = "维护手册文件查询")
    @PostMapping("/queryFile")
    @ActionLog(name = "查看维护手册文件列表", title = "维护手册", key = LogTypeConstant.QUERY)
    ResultVo queryFile(@RequestBody FolderQueryReq req) {
        if (StrUtil.isEmpty(req.getPid())) {
            req.setPid(SysFolder.FOLDER_ROOT_ID);
        }
        List<String> cabinetIds = new ArrayList<>();
        QueryWrapper<Cabinet> cabinetQuery = Wrappers.query();
        PageBean<FolderVo> pageResp = new PageBean<>();

        //机柜或机房
        if (req.getType() != OrgTypeConst.ROOM && req.getType() != OrgTypeConst.CABINET) {
            String id;
            if (StrUtil.isEmpty(req.getOrgId())) {
                // 查询条件为空时默认展示第一个组织下的手册
                id = orgService.getDefaultOrg().getId();
            } else {
                id = req.getOrgId();
            }
            List<String> defaultAllOrg = new ArrayList<>();
            if (orgService.getById(id).getType() == OrgTypeConst.CENTER) {
                defaultAllOrg.add(id);
            } else {
                // 非中心节点展示当前节点以及下属所有节点数据
                QueryWrapper<SysOrg> query = Wrappers.query();
                query.like("PIDS", id);
                defaultAllOrg = orgService.list(query).stream().map(SysOrg::getId).collect(Collectors.toList());
                defaultAllOrg.add(id);
            }
            // 查询机房
            QueryWrapper<Room> roomQuery = Wrappers.query();
            roomQuery.in("ORG_ID", defaultAllOrg);
            List<String> roomIds = roomService.list(roomQuery).stream().map(Room::getId).collect(Collectors.toList());
            // 组织下没有机房的直接返回空
            if (CollUtil.isEmpty(roomIds) || roomIds.size() < 1) {
                return ResultVoUtil.success("该组织下没有机房！", pageResp);
            }
            // 查询机柜
            cabinetQuery.in("ROOM_ID", roomIds);
            cabinetIds = cabinetServ.list(cabinetQuery).stream().map(Cabinet::getId).collect(Collectors.toList());
        } else if (req.getType() == OrgTypeConst.ROOM) {
            // 所有机柜
            cabinetQuery.in("ROOM_ID", req.getOrgId());
            cabinetIds = cabinetServ.list(cabinetQuery).stream().map(Cabinet::getId).collect(Collectors.toList());
        } else if (req.getType() == OrgTypeConst.CABINET) {
            cabinetIds.add(req.getOrgId());
        }

        if (CollUtil.isEmpty(cabinetIds) || cabinetIds.isEmpty()) {
            return ResultVoUtil.success("该组织下没有机柜！", pageResp);
        }

        QueryWrapper<SysFolder> wrapper = new QueryWrapper<>();
        wrapper.eq("STATUS", StatusEnum.OK.getCode());
        wrapper.in("REMARK", cabinetIds);
        wrapper.orderByAsc("SORT");

        List<SysFolder> folderList = folderService.list(wrapper);
        QueryWrapper<SysFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("FOLDER_ID", cabinetIds);
        queryWrapper.eq("STATUS", StatusEnum.OK.getCode());
        List<SysFile> fileList = fileService.list(queryWrapper);


        List<MaintainHandBookVo> resultList = new ArrayList<>();
        for (SysFolder folder : folderList) {
            MaintainHandBookVo maintainHandBookVo = new MaintainHandBookVo();
            maintainHandBookVo.setId(folder.getId());
            maintainHandBookVo.setTitle(folder.getTitle());
            // 文件夹位置
            StringBuilder index = new StringBuilder();
            // 机柜
            Cabinet cabinet = cabinetServ.getById(folder.getRemark());
            // 机房
            Room room = roomService.getById(cabinet.getRoomId());
            index.append(room.getName()).append("-").append(cabinet.getName());
            maintainHandBookVo.setIndex(index.toString());
            resultList.add(maintainHandBookVo);
        }

        for (SysFile file : fileList) {
            MaintainHandBookVo maintainHandBookVo = new MaintainHandBookVo();
            maintainHandBookVo.setId(file.getId());
            maintainHandBookVo.setTitle(file.getOrignName());
            maintainHandBookVo.setRemark(file.getRemark());
            // 文件夹位置
            StringBuilder index = new StringBuilder();
            // 机柜
            Cabinet cabinet = cabinetServ.getById(file.getFolderId());
            // 机房
            Room room = roomService.getById(cabinet.getRoomId());
            index.append(room.getName()).append("-").append(cabinet.getName());
            maintainHandBookVo.setIndex(index.toString());
            resultList.add(maintainHandBookVo);
        }

        return ResultVoUtil.success(resultList);


    }
}