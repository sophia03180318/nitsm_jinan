package com.jcca.web.handbook.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.FolderCategoryEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.file.FileUpload;
import com.jcca.common.utils.file.config.properties.UploadProjectProperties;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.handbook.controller.bean.*;
import com.jcca.web.handbook.vo.FileVo;
import com.jcca.web.handbook.vo.FolderVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 维护手册
 *
 * @author lyp
 */
@Slf4j
@Api(tags = "维护手册相关接口")
@RestController
@RequestMapping("/api/maintain")
public class ApiMaintainHandBookController {

    private static final String UPLOAD_MODEL_TMP_PATH = "/maintainTmp";
    private static final String UPLOAD_MODEL_PROD_PATH = "/maintainProd";
    private static final String DOWN_URI = "/api/maintain/download?id=";

    @Resource
    private SysFileService fileService;
    @Resource
    private SysFolderService folderService;
    @Resource
    private UploadProjectProperties fileProp;
    @Resource
    private CabinetService cabinetServ;
    @Resource
    private SysOrgService orgService;
    @Resource
    private RoomService roomService;

    @Value("${project.base-url}")
    private String baseUrl;

    /**
     * 维护手册文件夹分页查询
     *
     * @param req 参见FolderQueryReq
     * @return ResultVo
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiOperation(value = "维护手册文件夹查询")
    @PostMapping("/queryFolder")
    @ActionLog(name = "查看维护手册文件夹列表", title = "维护手册", key = LogTypeConstant.QUERY)
    ResultVo queryFolder(@RequestBody FolderQueryReq req) {
        if (StrUtil.isEmpty(req.getPid())) {
            req.setPid(SysFolder.FOLDER_ROOT_ID);
        }
        // 根据org查询除的机柜ID
        List<String> cabinetIds = new ArrayList<>();
        // 机柜ID的查询构造器
        QueryWrapper<Cabinet> cabinetQuery = Wrappers.query();
        // 返回值
        PageBean<FolderVo> pageResp = new PageBean<>();
        // 判断所选组织类型 syt
        if (req.getType() != OrgTypeConst.ROOM && req.getType() != OrgTypeConst.CABINET) {
            // 所选组织下辖的所有组织 syt
            String id;
            if (StrUtil.isEmpty(req.getOrgId())) {
                // 查询条件为空时默认展示第一个组织下的手册 syt
                id = orgService.getDefaultOrg().getId();
            } else {
                id = req.getOrgId();
            }

            List<String> defaultAllOrg = new ArrayList<>();
            QueryWrapper<SysOrg> query = Wrappers.query();
            query.like("PIDS", id);
            if (orgService.getById(id).getType() == OrgTypeConst.CENTER) {
                // 中心节点只展示中心的数据 syt
                defaultAllOrg.add(id);
            } else {
                // 非中心节点展示当前节点以及下属所有节点数据
                defaultAllOrg = orgService.list(query).stream().map(SysOrg::getId).collect(Collectors.toList());
                defaultAllOrg.add(id);
            }
            // 所有机房ID syt
            QueryWrapper<Room> roomQuery = Wrappers.query();
            roomQuery.in("ORG_ID", defaultAllOrg);
            List<String> roomIds = roomService.list(roomQuery).stream().map(Room::getId).collect(Collectors.toList());
            // 组织下没有机房的直接返回空
            if (CollUtil.isEmpty(roomIds) || roomIds.size() < 1) {
                return ResultVoUtil.warning("该组织下没有机房！");
            }
            // 所有机柜 syt
            cabinetQuery.in("ROOM_ID", roomIds);
            cabinetIds = cabinetServ.list(cabinetQuery).stream().map(Cabinet::getId).collect(Collectors.toList());
        } else if (req.getType() == OrgTypeConst.ROOM) {
            // 所有机柜 syt
            cabinetQuery.in("ROOM_ID", req.getOrgId());
            cabinetIds = cabinetServ.list(cabinetQuery).stream().map(Cabinet::getId).collect(Collectors.toList());
        } else if (req.getType() == OrgTypeConst.CABINET) {
            cabinetIds.add(req.getOrgId());
        }

        if (CollUtil.isEmpty(cabinetIds) || cabinetIds.size() < 1) {
            return ResultVoUtil.warning("该组织下没有机柜！");
        }
        IPage page = PagePlugin.startPage(req.getPage(), req.getSize() == 0 ? 10 : req.getSize());
        QueryWrapper<SysFolder> wrapper = new QueryWrapper<>();
//        wrapper.eq("PID", req.getPid());
        wrapper.eq("STATUS", StatusEnum.OK.getCode());
//        wrapper.eq("CATEGORY", FolderCategoryEnum.MAINTAIN_HAND_BOOK.getCode());
        wrapper.in("REMARK", cabinetIds);
        wrapper.orderByAsc("SORT");

        IPage pageResult = folderService.page(page, wrapper);
        List records = pageResult.getRecords();

        List<FolderVo> resultList = new ArrayList<>();
        for (Object object : records) {
            // 文件夹位置 syt
            StringBuilder index = new StringBuilder();
            FolderVo folderVo = BeanUtil.copyProperties(object, FolderVo.class);
            // 机柜 syt
            Cabinet cabinet = cabinetServ.getById(folderVo.getRemark());
            // 机房 syt
            Room room = roomService.getById(cabinet.getRoomId());
            index/*.append(org.getTitle()).append("-")*/.append(room.getName()).append("-").append(cabinet.getName());

            folderVo.setIndex(index.toString());
            resultList.add(folderVo);
        }

        pageResp.setContent(resultList);
        pageResp.setTotal(pageResult.getTotal());

        return ResultVoUtil.success(pageResp);
    }

    /**
     * 维护手册文件查询
     *
     * @return ResultVo
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiOperation(value = "维护手册文件查询")
    @PostMapping("/queryFile")
    @ActionLog(name = "查看维护手册文件列表", title = "维护手册", key = LogTypeConstant.QUERY)
    ResultVo queryFile(@RequestBody FileQueryReq req) {
        if (StrUtil.isEmpty(req.getFolderId())) {
            return ResultVoUtil.error("请选择文件夹");
        }
        IPage page = PagePlugin.startPage(req.getPage(), req.getSize());
        QueryWrapper<SysFile> wrapper = new QueryWrapper<>();
        wrapper.eq("FOLDER_ID", req.getFolderId());
        wrapper.eq("STATUS", StatusEnum.OK.getCode());
        wrapper.orderByDesc("create_time");
        IPage pageResult = fileService.page(page, wrapper);
        List records = pageResult.getRecords();

        List<FileVo> fileList = new ArrayList<>();
        for (Object object : records) {
            SysFile sysFile = BeanUtil.copyProperties(object, SysFile.class);
            FileVo fileVo = new FileVo();
            fileVo.setCreateTime(sysFile.getCreateTime());
            fileVo.setDownloadUrl(baseUrl + DOWN_URI + sysFile.getId());
            fileVo.setId(sysFile.getId());
            fileVo.setOrignName(sysFile.getOrignName());
            fileVo.setRemark(sysFile.getRemark());

            fileList.add(fileVo);
        }

        PageBean<FileVo> filePage = new PageBean<>();
        filePage.setContent(fileList);
        filePage.setTotal(pageResult.getTotal());

        return ResultVoUtil.success(filePage);
    }

    /**
     * 查询可用的机柜信息
     *
     * @return ResultVo
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "维护手册查询可用机柜")
    @PostMapping("/queryCanUseCabinet")
    ResultVo queryCanUseCabinet() {
        QueryWrapper<SysFolder> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("REMARK");
        queryWrapper.eq("CATEGORY", FolderCategoryEnum.MAINTAIN_HAND_BOOK.getCode());
        queryWrapper.eq("STATUS", StatusEnum.OK.getCode());
        List<SysFolder> folders = folderService.list(queryWrapper);
        List<String> cabinetIds = folders.stream().map(SysFolder::getRemark).collect(Collectors.toList());
        if (cabinetIds.isEmpty()) {
            cabinetIds.add("x");
        }
        QueryWrapper<Cabinet> cabinetQuery = new QueryWrapper<>();
        List<Cabinet> list = cabinetServ.list(cabinetQuery);

        return ResultVoUtil.success(list);
    }

    /**
     * 添加文件夹
     *
     * @return ResultVo
     */
    @SuppressWarnings({"rawtypes"})
    @ApiOperation(value = "维护手册新增文件夹")
    @RequiresPermissions({"api:maintain:addFolder"})
    @PostMapping("/addFolder")
    @ActionLog(name = "创建文件夹", title = "维护手册", key = LogTypeConstant.ADD)
    ResultVo addFolder(@Validated @RequestBody AddFolderReq req) {
        String pid = req.getPid();
        if (!SysFolder.FOLDER_ROOT_ID.equals(pid)) {
            SysFolder pObj = folderService.getById(pid);
            if (Objects.isNull(pObj)) {
                return ResultVoUtil.error("父文件夹不存在");
            }
        }

        SysFolder folder = new SysFolder();
        folder.setCategory(FolderCategoryEnum.MAINTAIN_HAND_BOOK.getCode());
        folder.setPid(pid);
        folder.setPids(folderService.getPids(pid));
        folder.setRemark(req.getRemark());
        folder.setSort((byte) folderService.count());
        folder.setStatus(StatusEnum.OK.getCode());
        folder.setTitle(req.getTitle());

        try {
            folderService.createFolder(folder);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultVoUtil.error("创建失败");
        }

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @return ResultVo
     */
    @SuppressWarnings({"rawtypes"})
    @ApiOperation(value = "维护手册上传文件")
    @RequiresPermissions({"api:maintain:upload"})
    @PostMapping("/upload")
    @ActionLog(name = "上传文件", title = "维护手册", key = LogTypeConstant.UPLOAD)
    ResultVo addFile(@RequestParam("file") MultipartFile[] file) {
        if (Objects.isNull(file)) {
            return ResultVoUtil.error("文件上传失败-文件空");
        }
        if (file.length == 0) {
            return ResultVoUtil.error("文件上传失败-文件空");
        }
        MultipartFile multipartFile = file[0];
        if (Objects.isNull(multipartFile)) {
            return ResultVoUtil.error("文件上传失败-文件空");
        }
        if (!checkFileSize(multipartFile.getSize(), 50, "M")) {
            return ResultVoUtil.error("文件不能超过50M");
        }

        String name = multipartFile.getOriginalFilename();
        if (!name.contains(".")) {
            return ResultVoUtil.error("文件上传失败-文件名不合法-缺少后缀");
        }
        SysFile sysFile = FileUpload.getFile(multipartFile, UPLOAD_MODEL_TMP_PATH);
        try {
            FileUpload.transferTo(multipartFile, sysFile);
        } catch (Exception e) {
            log.error("维护手册-上传文件失败：{}", e.getMessage(), e);
            return ResultVoUtil.error("文件保存失败");
        }
        return ResultVoUtil.success(sysFile);
    }

    /**
     * 判断文件大小是否超限制
     *
     * @param len  文件大小
     * @param size 参考大小
     * @param unit unit
     * @return boolean
     */
    public static boolean checkFileSize(Long len, int size, String unit) {
        double fileSize = 0;
        if ("B".equals(unit.toUpperCase())) {
            fileSize = (double) len;
        } else if ("K".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1024;
        } else if ("M".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1048576;
        } else if ("G".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1073741824;
        }
        if (fileSize > size) {
            return false;
        }
        return true;
    }

    /**
     * 维护手册保存文件
     *
     * @param file 参见AddFileReq
     * @return ResultVo
     */
    @SuppressWarnings({"rawtypes"})
    @ApiOperation(value = "维护手册保存文件")
    @RequiresPermissions({"api:maintain:saveFile"})
    @PostMapping("/saveFile")
    ResultVo saveFile(@Validated @RequestBody AddFileReq file) {
        String filePath = file.getFilePath();
        String newPath = filePath.replace(UPLOAD_MODEL_TMP_PATH, UPLOAD_MODEL_PROD_PATH)
                .replace("/" + file.getFileName(), "");
        try {
            log.info("维护手册-原路径[" + filePath + "],新路径[" + newPath + "]");
            FileUpload.moveFile(filePath, newPath);
        } catch (Exception e) {
            log.error("维护手册-保存文件失败：{}", e.getMessage(), e);
            return ResultVoUtil.error("文件提交保存失败请重新上传");
        }
        SysFile sysFile = BeanUtil.copyProperties(file, SysFile.class);
        sysFile.setId(null);
        sysFile.setStatus(StatusEnum.OK.getCode());
        sysFile.setFilePath(file.getFilePath().replace(UPLOAD_MODEL_TMP_PATH, UPLOAD_MODEL_PROD_PATH));

        fileService.save(sysFile);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 维护手册删除文件
     *
     * @param ids 删除的IDs
     * @return ResultVo
     */
    @SuppressWarnings({"rawtypes"})
    @ApiOperation(value = "维护手册删除文件")
    @RequiresPermissions({"api:maintain:removeFile"})
    @PostMapping("/removeFile/{id}")
    @ActionLog(name = "维护手册删除文件", title = "维护手册", key = LogTypeConstant.REMOVEE)
    ResultVo removeFile(@PathVariable("id") String ids) {
        SysFile file = fileService.getById(ids);
        if (Objects.isNull(file)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "文件id不正确");
        }
        file.setStatus(StatusEnum.DELETE.getCode());
        fileService.updateById(file);

        return ResultVoUtil.success();
    }

    @SuppressWarnings({"rawtypes"})
    @ApiOperation(value = "维护手册删除文件夹")
    @RequiresPermissions({"api:maintain:removeFolder"})
    @PostMapping("/removeFolder/{id}")
    @ActionLog(name = "维护手册删除文件夹", title = "维护手册", key = LogTypeConstant.REMOVEE)
    ResultVo removeFolder(@PathVariable("id") String ids) {
        SysFolder folder = folderService.getById(ids);
        if (Objects.isNull(folder)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "文件夹id不正确");
        }
        QueryWrapper<SysFile> fileWrapper = new QueryWrapper<>();
        fileWrapper.eq("FOLDER_ID", folder.getId());
        fileWrapper.eq("STATUS", StatusEnum.OK.getCode());
        List<SysFile> fileList = fileService.list(fileWrapper);

        if (fileList.size() > 0) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "文件夹内有未删除文件");
        }
        folder.setStatus(StatusEnum.DELETE.getCode());
        folderService.updateById(folder);
        return ResultVoUtil.success("删除成功");
    }

    /**
     * 文件下载
     *
     * @param id 文件ID
     */
    @ApiOperation(value = "维护手册文件下载")
    @RequiresPermissions(value = {"api:maintain:download", "api:cloud:download"}, logical = Logical.OR)
    @GetMapping("/download")
    @ActionLog(name = "维护手册下载文件", title = "维护手册", key = LogTypeConstant.DOWNLOAD)
    void downloadFile(String id, HttpServletResponse response, HttpServletRequest request) {
        log.info("【维护手册-下载文件id】:{}", id);
        if (StrUtil.isEmpty(id)) {
            return;
        }
        SysFile file = fileService.getById(id);
        if (Objects.isNull(file)) {
            return;
        }
        String pathname;
        pathname = fileProp.getFilePath() + file.getFilePath().replace(fileProp.getStaticPath(), "");
        File downFile = new File(pathname.replace("///", "/"));
        if (!downFile.exists()) {
            log.error("维护手册-下载文件不存在：{}", pathname);
            return;
        }
        String fileName = file.getOrignName();

        OutputStream out = null;
        try {
            @SuppressWarnings("resource")
            InputStream fileInput = new BufferedInputStream(new FileInputStream(downFile));
            // 乱码问题
            String agent = request.getHeader("user-agent");
            if (agent.contains("FireFox")) {
                fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            } else {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            }
            String mineType = request.getServletContext().getMimeType(fileName);
            response.setContentType(mineType);
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            out = response.getOutputStream();
            byte[] buf = new byte[1024];
            int readTmp = 0;

            while ((readTmp = fileInput.read(buf)) != -1) {
                out.write(buf, 0, readTmp);
            }
            out.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                assert out != null;
                out.close();
            } catch (NullPointerException e) {
                log.error("responseFileStream stream close() error:NullPointerException", e);
            } catch (Exception e) {
                log.error("responseFileStream stream close() error:", e);
            }
        }

    }

    /**
     * 中心机房机柜组织树
     *
     * @author syt
     */
    @ApiOperation(value = "中心机房机柜组织树")
    @PostMapping("/centerRoomCabinetTree")
    public ResultVo centerRoomCabinetTree() {
        // 当前用户所有组织
        List<SysOrg> tmpList = ShiroUtil.getSubjectOrgs();
        List<SysOrg> subjectOrgs = new ArrayList<>();
        for (SysOrg org : tmpList) {
            if (org.getType() != OrgTypeConst.CENTER) {
                continue;
            }
            List<Room> byOrgId = roomService.list(new QueryWrapper<Room>().eq("ORG_ID", org.getId()));
            // 判断组织下是否有机房
            if (CollUtil.isNotEmpty(byOrgId)) {
                org.setExistRoom(true);
            } else {
                org.setExistRoom(false);
            }
            subjectOrgs.add(org);
        }
        SysOrg org = orgService.getDefaultOrg();

        // 组织下的机房机柜
        // 机房
        List<String> orgIds = subjectOrgs.stream().map(SysOrg::getId).collect(Collectors.toList());
        QueryWrapper<Room> roomQuery = Wrappers.query();
        orgIds.add("x");
        roomQuery.in("ORG_ID", orgIds);
        List<Room> roomList = roomService.list(roomQuery);
        // 机柜
        List<String> roomIds = roomList.stream().map(Room::getId).collect(Collectors.toList());
        QueryWrapper<Cabinet> cabinetQuery = Wrappers.query();
        roomIds.add("x");
        cabinetQuery.in("ROOM_ID", roomIds);
        List<Cabinet> cabinetList = cabinetServ.list(cabinetQuery);

        // 组织机房机柜放到一起
        List<Object> resList = new ArrayList<>();
        resList.addAll(subjectOrgs);
        // 组合数据
        for (Room room : roomList) {
            RoomTreeVo roomTreeVo = new RoomTreeVo();
            roomTreeVo.setId(room.getId());
            roomTreeVo.setPid(room.getOrgId());
            for (SysOrg subjectOrg : subjectOrgs) {
                if (room.getOrgId().equals(subjectOrg.getId())) {
                    roomTreeVo.setPTitle(subjectOrg.getTitle());
                }
            }
            roomTreeVo.setTitle(room.getName());
            roomTreeVo.setType(OrgTypeConst.ROOM);
            // 返数据的时候，先返机房，再返组织
            resList.add(0, roomTreeVo);
        }
        for (Cabinet cabinet : cabinetList) {
            CabinetTreeVo cabinetTreeVo = new CabinetTreeVo();
            cabinetTreeVo.setId(cabinet.getId());
            cabinetTreeVo.setType(OrgTypeConst.CABINET);
            cabinetTreeVo.setPid(cabinet.getRoomId());
            // 父级组织名称
            for (Room room : roomList) {
                if (cabinet.getRoomId().equals(room.getId())) {
                    cabinetTreeVo.setPTitle(room.getName());
                }
            }
            cabinetTreeVo.setTitle(cabinet.getName());
            // 查找组织ID 因为机柜只关联了机房，所以需要找到对应机房的orgId
            cabinetTreeVo.setOrgId(roomService.getOne(new QueryWrapper<Room>().eq("ID", cabinet.getRoomId())).getOrgId());
            resList.add(cabinetTreeVo);
        }


        Map<String, Object> map = new HashMap<>(16);
        map.put("id", org.getId());
        if (!roomIds.isEmpty()) {
            map.put("defaultRoomId", roomIds.get(0));
        }
        map.put("title", org.getTitle());
        map.put("orgList", resList);

        return ResultVoUtil.success(map);
    }


    /**
     * 机房机柜组织树
     *
     * @author syt
     */
    @ApiOperation(value = "机房机柜组织树")
    @PostMapping("/roomCabinetTree")
    public ResultVo roomCabinetTree() {
        // 当前用户所有组织
        List<SysOrg> subjectOrgs = ShiroUtil.getSubjectOrgs();
        for (SysOrg org : subjectOrgs) {
            List<Room> byOrgId = roomService.list(new QueryWrapper<Room>().eq("ORG_ID", org.getId()));
            // 判断组织下是否有机房
            if (CollUtil.isNotEmpty(byOrgId)) {
                org.setExistRoom(true);
            } else {
                org.setExistRoom(false);
            }
        }
        SysOrg org = orgService.getDefaultOrg();

        // 组织下的机房机柜
        // 机房
        List<String> orgIds = subjectOrgs.stream().map(SysOrg::getId).collect(Collectors.toList());
        QueryWrapper<Room> roomQuery = Wrappers.query();
        orgIds.add("x");
        roomQuery.in("ORG_ID", orgIds);
        List<Room> roomList = roomService.list(roomQuery);
        // 机柜
        List<String> roomIds = roomList.stream().map(Room::getId).collect(Collectors.toList());
        QueryWrapper<Cabinet> cabinetQuery = Wrappers.query();
        roomIds.add("x");
        cabinetQuery.in("ROOM_ID", roomIds);
        List<Cabinet> cabinetList = cabinetServ.list(cabinetQuery);

        // 组织机房机柜放到一起
        List<Object> resList = new ArrayList<>();
        resList.addAll(subjectOrgs);
        // 组合数据
        for (Room room : roomList) {
            RoomTreeVo roomTreeVo = new RoomTreeVo();
            roomTreeVo.setId(room.getId());
            roomTreeVo.setPid(room.getOrgId());
            for (SysOrg subjectOrg : subjectOrgs) {
                if (room.getOrgId().equals(subjectOrg.getId())) {
                    roomTreeVo.setPTitle(subjectOrg.getTitle());
                }
            }
            roomTreeVo.setTitle(room.getName());
            roomTreeVo.setType(OrgTypeConst.ROOM);
            // 返数据的时候，先返机房，再返组织
            resList.add(0, roomTreeVo);
        }
        for (Cabinet cabinet : cabinetList) {
            CabinetTreeVo cabinetTreeVo = new CabinetTreeVo();
            cabinetTreeVo.setId(cabinet.getId());
            cabinetTreeVo.setType(OrgTypeConst.CABINET);
            cabinetTreeVo.setPid(cabinet.getRoomId());
            // 父级组织名称
            for (Room room : roomList) {
                if (cabinet.getRoomId().equals(room.getId())) {
                    cabinetTreeVo.setPTitle(room.getName());
                }
            }
            cabinetTreeVo.setTitle(cabinet.getName());
            // 查找组织ID 因为机柜只关联了机房，所以需要找到对应机房的orgId
            cabinetTreeVo.setOrgId(roomService.getOne(new QueryWrapper<Room>().eq("ID", cabinet.getRoomId())).getOrgId());
            resList.add(cabinetTreeVo);
        }


        Map<String, Object> map = new HashMap<>(16);
        map.put("id", org.getId());
        if (!roomIds.isEmpty()) {
            map.put("defaultRoomId", roomIds.get(0));
        }
        map.put("title", org.getTitle());
        map.put("orgList", resList);
        return ResultVoUtil.success(map);
    }

}
