package com.jcca.web.handbook.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.admin.system.entity.SysFolder;
import com.jcca.admin.system.service.SysFileService;
import com.jcca.admin.system.service.SysFolderService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.FolderCategoryEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.file.FileUpload;
import com.jcca.common.utils.file.config.properties.UploadProjectProperties;
import com.jcca.web.handbook.controller.bean.AddFolderReq;
import com.jcca.web.handbook.vo.FileVo;
import com.jcca.web.handbook.vo.FolderVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author hanwone
 * @description 共享云盘
 * @className ApiCloudDiskController
 * @date 2022/8/2 11:45
 * @since 2.0.0.1
 */
@Slf4j
@Api(tags = "共享云盘")
@RestController
@RequestMapping("/api/cloud")
public class ApiCloudDiskController {

    @Resource
    private SysFolderService folderService;
    @Resource
    private SysFileService fileService;
    @Resource
    private UploadProjectProperties properties;

    /**
     * 新增文件夹
     *
     * @return 业务数据
     */
    @ApiOperation(value = "新建文件夹")
    @PostMapping("/addFolder")
    @RequiresPermissions({"api:cloud:addFolder"})
    @ActionLog(name = "新建文件夹", title = "文件共享", key = LogTypeConstant.ADD)
    public ResultVo addFolder(@Validated @RequestBody AddFolderReq req) {

        String reg = "\\pP|\\pS|\\s+";
        String title = req.getTitle();
        title = Pattern.compile(reg).matcher(title).replaceAll("").trim();
        if (StrUtil.isEmpty(title)) {
            return ResultVoUtil.warning("文件夹名称为空或为特殊字符");
        }

        QueryWrapper<SysFolder> query = Wrappers.query();
        query.eq("title", title);
        query.eq("status", StatusEnum.OK.getCode());
        query.eq("category", FolderCategoryEnum.CLOUD_DISK.getCode());
        List<SysFolder> list = folderService.list(query);
        if (CollectionUtil.isNotEmpty(list)) {
            return ResultVoUtil.warning("文件夹[" + title + "]已存在");
        }

        String pids = "[" + SysFolder.FOLDER_ROOT_DISK_ID + "]";
        String pid = req.getPid();
        SysFolder folder = new SysFolder();
        if (SysFolder.FOLDER_ROOT_DISK_ID.equals(pid)) {
            folder.setPids(pids);
        } else {
            SysFolder pObj = folderService.getById(pid);

            if (Objects.isNull(pObj)) {
                return ResultVoUtil.warning("请选择新文件夹位置");
            }
            if (pObj.getPids().length() > 108) {
                return ResultVoUtil.warning("文件夹位置不能超过6级");
            }
            folder.setPids(pObj.getPids() + ",[" + pid + "]");
        }
        folder.setCategory(FolderCategoryEnum.CLOUD_DISK.getCode());
        folder.setPid(pid);
        folder.setRemark(req.getRemark());
        folder.setStatus(StatusEnum.OK.getCode());
        folder.setTitle(title);

        query = Wrappers.query();
        query.eq("pid", pid);
        query.eq("status", StatusEnum.OK.getCode());
        query.eq("category", FolderCategoryEnum.CLOUD_DISK.getCode());
        folder.setSort((byte) (folderService.count(query) + 1));
        folderService.save(folder);

        return ResultVoUtil.success("添加成功");
    }

    /**
     * 重命名文件夹
     *
     * @return 业务数据
     */
    @ApiOperation("重命名文件夹")
    @PostMapping("/rename/folder")
    @RequiresPermissions({"api:cloud:rename:folder"})
    @ActionLog(name = "重命名文件夹", title = "文件共享", key = LogTypeConstant.MODIFY)
    public ResultVo renameFolder(@RequestBody Map<String, String> map) {
        String id = map.get("id");
        String newTile = map.get("newTile");
        if (StrUtil.isEmpty(id) || StrUtil.isEmpty(newTile)) {
            return ResultVoUtil.warning("文件夹名称不能为空");
        }

        if (SysFolder.FOLDER_ROOT_DISK_ID.equals(id)) {
            return ResultVoUtil.warning("不能操作根文件夹");
        }

        QueryWrapper<SysFolder> query = Wrappers.query();
        query.eq("TITLE", newTile);
        query.eq("STATUS", StatusConst.OK);
        List<SysFolder> list = folderService.list(query);
        if (CollectionUtil.isNotEmpty(list)) {
            for (SysFolder folder : list) {
                if (!folder.getId().equals(id)) {
                    return ResultVoUtil.warning("文件夹名称不能重复");
                }
            }
        }

        if (newTile.length() > 160) {
            newTile = newTile.substring(0, 160);
        }

        SysFolder one = folderService.getById(id);
        if (Objects.nonNull(one)) {
            one.setTitle(newTile);
            folderService.updateById(one);
        }
        return ResultVoUtil.success("修改成功");
    }

    /**
     * 删除文件夹
     *
     * @param id 文件夹ID
     * @return 业务数据
     */
    @ApiOperation("删除文件夹")
    @PostMapping("/remove/folder")
    @RequiresPermissions({"api:cloud:remove:folder"})
    @ActionLog(name = "删除文件夹", title = "文件共享", key = LogTypeConstant.REMOVEE)
    public ResultVo removeFolder(String id) {
        if (StrUtil.isEmpty(id)) {
            return ResultVoUtil.warning("缺少参数");
        }

        if (SysFolder.FOLDER_ROOT_DISK_ID.equals(id)) {
            return ResultVoUtil.warning("不能删除根文件夹");
        }

        QueryWrapper<SysFolder> folderQuery = Wrappers.query();
        folderQuery.eq("pid", id);
        folderQuery.eq("status", StatusEnum.OK.getCode());
        folderQuery.eq("category", FolderCategoryEnum.CLOUD_DISK.getCode());
        int folderCount = folderService.count(folderQuery);
        if (folderCount > 0) {
            return ResultVoUtil.warning("文件夹内不为空不能删除");
        }

        QueryWrapper<SysFile> fileQuery = Wrappers.query();
        fileQuery.eq("folder_id", id);
        fileQuery.eq("status", StatusEnum.OK.getCode());
        int fileCount = fileService.count(fileQuery);
        if (fileCount > 0) {
            return ResultVoUtil.warning("文件夹内有文件不能删除");
        }
        SysFolder folder = folderService.getById(id);
        if (Objects.nonNull(folder)) {
            folder.setStatus(StatusEnum.DELETE.getCode());
            folderService.updateById(folder);
        }
        return ResultVoUtil.success("删除成功");
    }

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 业务数据
     */
    @ApiOperation("删除文件")
    @PostMapping("/remove/file")
    @RequiresPermissions({"api:cloud:remove:file"})
    @ActionLog(name = "删除文件", title = "文件共享", key = LogTypeConstant.REMOVEE)
    public ResultVo removeFile(String id) {
        if (StrUtil.isEmpty(id)) {
            return ResultVoUtil.warning("缺少参数");
        }

        SysFile one = fileService.getById(id);
        if (Objects.nonNull(one)) {
            one.setStatus(StatusEnum.DELETE.getCode());
            fileService.updateById(one);
        }
        return ResultVoUtil.success("删除成功");
    }

    /**
     * 文件检索
     *
     * @param orignName 文件名称
     * @param folderId  文件夹id
     * @return 业务数据
     */
    @ApiOperation("搜索文件")
    @GetMapping("/search")
    @RequiresPermissions({"api:cloud:query"})
    @ActionLog(name = "搜索文件", title = "文件共享", key = LogTypeConstant.QUERY)
    public ResultVo<Object> search(String folderId, String orignName) {


        QueryWrapper<SysFile> query = Wrappers.query();
        query.eq("status", StatusEnum.OK.getCode());
        if (Objects.nonNull(folderId)) {
            query.eq("folder_id", folderId);
        } else {
            List<String> idList = folderService.getAllFolderId(FolderCategoryEnum.CLOUD_DISK.getCode());
            idList.add(SysFolder.FOLDER_ROOT_DISK_ID);
            query.in("folder_id", idList);
        }

        if (Objects.nonNull(orignName)) {
            query.like("orign_name", orignName);
        }

        List<SysFile> fileList = fileService.list(query);

        return ResultVoUtil.success(getFileVoList(fileList));
    }

    private List<FileVo> getFileVoList(List<SysFile> fileList) {
        List<FileVo> fileVoList = new ArrayList<>();
        String staticUrl = properties.getStaticUrl();
        for (SysFile file : fileList) {
            String filePath = file.getFilePath();
            filePath = filePath.replace("/upload", "");

            FileVo fileVo = BeanUtil.copyProperties(file, FileVo.class);
            fileVo.setDownloadUrl(staticUrl + filePath);
            fileVo.setFilePath(fileService.getPathById(file.getId(), SysFolder.FOLDER_ROOT_DISK_ID));
            fileVoList.add(fileVo);
        }
        return fileVoList;
    }

    /**
     * 文件列表
     *
     * @param id 文件夹ID
     * @return 业务数据
     */
    @ApiOperation("文件列表")
    @GetMapping("/query")
    @RequiresPermissions({"api:cloud:query"})
    @ActionLog(name = "查看文件列表", title = "文件共享", key = LogTypeConstant.QUERY)
    public ResultVo<Object> query(String id) {
        if (StrUtil.isEmpty(id)) {
            id = SysFolder.FOLDER_ROOT_DISK_ID;
        }
        Map<String, Object> resultMap = new HashMap<>();
        QueryWrapper<SysFolder> folderQuery = Wrappers.query();
        folderQuery.eq("pid", id);
        folderQuery.eq("status", StatusEnum.OK.getCode());
        folderQuery.eq("category", FolderCategoryEnum.CLOUD_DISK.getCode());
        folderQuery.orderByDesc("create_time");
        List<SysFolder> folderList = folderService.list(folderQuery);

        QueryWrapper<SysFile> fileQuery = Wrappers.query();
        fileQuery.eq("folder_id", id);
        fileQuery.eq("status", StatusEnum.OK.getCode());
        fileQuery.orderByDesc("create_time");
        List<SysFile> fileList = fileService.list(fileQuery);

        List<FolderVo> folderVoList = new ArrayList<>();
        for (SysFolder folder : folderList) {
            FolderVo folderVo = BeanUtil.copyProperties(folder, FolderVo.class);
            folderVoList.add(folderVo);
        }

        resultMap.put("folderList", folderVoList);
        resultMap.put("fileList", getFileVoList(fileList));

        return ResultVoUtil.success(resultMap);
    }

    /**
     * 下载文件
     *
     * @return 业务数据
     */
    @ApiOperation("下载文件")
    @GetMapping("/download")
    @RequiresPermissions({"api:cloud:download"})
    @ActionLog(name = "下载文件", title = "文件共享", key = LogTypeConstant.DOWNLOAD)
    public void download(String id, HttpServletResponse response) {
        if (StrUtil.isEmpty(id)) {
            log.warn("文件共享-下载文件参数错误");
            return;
        }
        SysFile sysFile = fileService.getById(id);
        if (Objects.isNull(sysFile) || sysFile.getStatus().byteValue() == StatusEnum.DELETE.getCode()) {
            log.warn("文件[" + id + "]已删除或不存在");
            return;
        }

        String filePath = sysFile.getFilePath();
        filePath = filePath.replace("/upload", "");
        String staticUrl = properties.getFilePath();
        filePath = staticUrl + filePath;
        File downFile = new File(filePath);

        OutputStream out = null;
        InputStream fileInput = null;
        try {
            if (!downFile.exists()) {
                sysFile.setStatus(StatusEnum.DELETE.getCode());
                fileService.updateById(sysFile);
                throw new ResultException(ResultEnum.CANNOT_FIND);
            }
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(sysFile.getOrignName(), "utf-8"));

            out = response.getOutputStream();
            byte[] buf = new byte[1024];
            int readTmp = 0;

            fileInput = new BufferedInputStream(new FileInputStream(downFile));
            while ((readTmp = fileInput.read(buf)) != -1) {
                out.write(buf, 0, readTmp);
            }
            out.flush();
        } catch (IOException e) {
            log.error("文件共享下载文件[" + id + "]异常", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInput != null) {
                try {
                    fileInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 保存文件信息
     *
     * @param sysFile 文件信息
     * @return 业务数据
     */
    @ApiOperation("保存文件信息")
    @PostMapping("/saveFile")
    @RequiresPermissions({"api:cloud:upload"})
    public ResultVo saveFile(@RequestBody SysFile sysFile) {
        if (Objects.isNull(sysFile)) {
            return ResultVoUtil.warning("参数为空");
        }
        if (StrUtil.isEmpty(sysFile.getId())) {
            return ResultVoUtil.warning("缺少参数");
        }
        SysFile one = fileService.getById(sysFile.getId());
        if (Objects.nonNull(one)) {
            return ResultVoUtil.warning("文件已存在");
        }
        sysFile.setStatus(StatusEnum.OK.getCode());
        fileService.save(sysFile);

        return ResultVoUtil.success("保存成功");
    }


    /**
     * 上传文件
     *
     * @param multipartFile 文件
     * @param folderId      文件夹ID
     * @return 业务数据
     */
    @ApiOperation("上传文件")
    @PostMapping("/upload")
    @RequiresPermissions({"api:cloud:upload"})
    @ActionLog(name = "上传文件", title = "文件共享", key = LogTypeConstant.UPLOAD)
    public ResultVo<Object> uploadFile(@RequestParam("file") MultipartFile multipartFile, String folderId) {
        if (Objects.isNull(multipartFile)) {
            return ResultVoUtil.warning("文件上传失败-不能上传空文件");
        }
        if (multipartFile.getSize() == 0) {
            return ResultVoUtil.warning("文件上传失败-不能上传空文件");
        }

        String originalFilename = multipartFile.getOriginalFilename();
        if (StrUtil.isEmpty(originalFilename) || originalFilename.length() > 60) {
            return ResultVoUtil.warning("文件上传失败-文件名为空或超过60个字符");
        }
        if (StrUtil.isEmpty(folderId)) {
            folderId = SysFolder.FOLDER_ROOT_DISK_ID;
        } else {
            SysFolder one = folderService.getById(folderId);
            if (Objects.isNull(one) || one.getStatus().byteValue() == StatusEnum.DELETE.getCode()) {
                return ResultVoUtil.warning("文件上传失败-请先选择文件夹");
            }
        }

        SysFile sysFile = null;
        String fileSha1 = FileUpload.getFileSha1(multipartFile);
        QueryWrapper<SysFile> query = Wrappers.query();
        query.eq("sha1", fileSha1);
        query.eq("status", StatusEnum.OK.getCode());
        query.orderByDesc("id");
        List<SysFile> list = fileService.list(query);
        if (CollectionUtil.isNotEmpty(list)) {
            sysFile = list.get(0);
            sysFile.setOrignName(originalFilename);
        } else {
            sysFile = FileUpload.getFile(multipartFile, "/disk");
            try {
                FileUpload.transferTo(multipartFile, sysFile);
            } catch (IOException | NoSuchAlgorithmException e) {
                log.error("文件共享上传文件[" + originalFilename + "]异常", e);
                return ResultVoUtil.warning("文件上传失败");
            }
        }
        sysFile.setId(MyIdUtil.getId());
        sysFile.setFolderId(folderId);
        checkOrignName(sysFile, 1);

        return ResultVoUtil.success(sysFile);
    }

    private void checkOrignName(SysFile upload, int nameIndex) {
        String orignName = upload.getOrignName();
        QueryWrapper<SysFile> query = Wrappers.query();
        query.eq("folder_id", upload.getFolderId());
        query.eq("orign_name", orignName);
        query.eq("status", StatusEnum.OK.getCode());
        List<SysFile> list = fileService.list(query);
        if (list.size() > 0) {
            int i = orignName.lastIndexOf(".");
            if (i == -1) {
                if (orignName.lastIndexOf("(") < 0) {
                    orignName = orignName + "(" + nameIndex + ")";
                } else {
                    orignName = orignName.substring(0, orignName.lastIndexOf("("));
                    orignName = orignName + "(" + nameIndex + ")";
                }
            } else {
                String nameTail = orignName.substring(i + 1);
                if (orignName.lastIndexOf("(") < 0) {
                    String nameFix = orignName.substring(0, i);
                    orignName = nameFix + "(" + nameIndex + ")." + nameTail;
                } else {
                    orignName = orignName.substring(0, orignName.lastIndexOf("("));
                    orignName = orignName + "(" + nameIndex + ")." + nameTail;
                }
            }

            upload.setOrignName(orignName);
            checkOrignName(upload, ++nameIndex);
        }
    }
}
