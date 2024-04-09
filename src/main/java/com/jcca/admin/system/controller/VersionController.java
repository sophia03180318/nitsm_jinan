package com.jcca.admin.system.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.admin.system.controller.bean.VersionManagerSaveReq;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.admin.system.entity.VersionMsg;
import com.jcca.admin.system.service.SysFileService;
import com.jcca.admin.system.service.VersionMsgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.file.FileUpload;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 版本控制
 *
 * @author lyp
 */
@Slf4j
@Controller
@RequestMapping("/system/version/manager")
public class VersionController {

    @Resource
    private SysFileService uploadService;
    @Resource
    private VersionMsgService versionMsgService;


    /**
     * 版本管理
     *
     * @return
     */
    @GetMapping("/index")
    @RequiresPermissions("system:version:manager:index")
    @ActionLog(name = "查看车站软件列表", title = "车站软件管理", key = LogTypeConstant.QUERY)
    public String manager(Model model, VersionMsg msg, Integer size, Integer page) {
        // 获取用户列表
        IPage iPage = PagePlugin.startPage(page, size);
        QueryWrapper<VersionMsg> queryWrapper = new QueryWrapper<VersionMsg>();
        if (StrUtil.isNotEmpty(msg.getVersion())) {
            queryWrapper.eq("VERSION", msg.getVersion());
        }
        if (StrUtil.isNotEmpty(msg.getCreateDateStr())) {
            Date parse = DateUtil.parse(msg.getCreateDateStr(), "yyyy-MM-dd").toJdkDate();

            DateTime beginOfDay = DateUtil.beginOfDay(parse);
            DateTime endOfDay = DateUtil.endOfDay(parse);
            queryWrapper.ge("CREATE_DATE", beginOfDay.toJdkDate());
            queryWrapper.le("CREATE_DATE", endOfDay.toJdkDate());
        }

        queryWrapper.orderByDesc("CREATE_DATE");
        IPage page2 = versionMsgService.page(iPage, queryWrapper);

        model.addAttribute("list", page2.getRecords());
        model.addAttribute("page", iPage);

        return "/system/version/versionManager";
    }

    @GetMapping("/detail/{id}")
    @RequiresPermissions("system:version:manager:detail")
    public String detail(@PathVariable("id") String id, Model model) {
        VersionMsg msg = versionMsgService.getById(id);
        model.addAttribute("detailObj", msg);
        return "/system/version/manager/detail";
    }

    /**
     * 打开上传页面
     *
     * @return
     */
    @GetMapping("/uploadPage")
    public String uploadPage() {
        return "/system/version/manager/uploadPage";
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/remove")
    @ResponseBody
    @RequiresPermissions("system:version:manager:remove")
    @ActionLog(name = "删除数据", title = "车站软件管理", key = LogTypeConstant.REMOVEE)
    public ResultVo remove(String ids) {
        if (StrUtil.isEmpty(ids)) {
            return ResultVoUtil.error("缺少删除数据的主键");
        }
        versionMsgService.removeById(ids);
        return ResultVoUtil.success("删除成功");
    }

    /**
     * 保存记录
     *
     * @param req
     * @return
     */
    @SuppressWarnings("rawtypes")
    @PostMapping("/save")
    @ResponseBody
    @ActionLog(name = "保存数据", title = "车站软件管理", key = LogTypeConstant.ADD)
    public ResultVo save(@Validated VersionManagerSaveReq req) {
        SysFile file = uploadService.getById(req.getSysFileId());
        if (Objects.isNull(file)) {
            return ResultVoUtil.error("文件不存在");
        }
        QueryWrapper<VersionMsg> queryWrapper = new QueryWrapper<VersionMsg>();
        queryWrapper.eq("VERSION", req.getVersionNum());
        List<VersionMsg> list = versionMsgService.list(queryWrapper);
        if (!list.isEmpty()) {
            return ResultVoUtil.error("该版本JAR已经存在，请勿重复上传");
        }

        VersionMsg entity = new VersionMsg();
        entity.setId(MyIdUtil.getId());
        entity.setDescStr(req.getDescStr());
        entity.setFilePath(req.getFilePath());
        entity.setSysFileId(req.getSysFileId());
        entity.setRemark(req.getRemark());
        entity.setVersion(req.getVersionNum());
        entity.setMd5(file.getMd5());
        entity.setCreateDate(new Date());

        versionMsgService.save(entity);

        return ResultVoUtil.success("保存成功");
    }

    /**
     * 上传jar
     *
     * @param file
     * @param jarType
     * @return
     */
    @SuppressWarnings("rawtypes")
    @PostMapping("/uploadJar")
    @ResponseBody
    @ActionLog(name = "上传JAR包", title = "车站软件管理", key = LogTypeConstant.UPLOAD)
    public ResultVo ploadImage(@RequestParam("file") MultipartFile file, String jarType) {
        // 创建Upload实体对象
        SysFile upload = FileUpload.getFile(file, "/" + jarType);
        try {
            return saveJar(file, upload);
        } catch (ResultException e) {
            return ResultVoUtil.error("请上传jar文件");
        } catch (Exception e) {
            log.error("jar包上传失败：" + e);
            return ResultVoUtil.error("上传失败");
        }

    }

    @SuppressWarnings("rawtypes")
    private ResultVo saveJar(MultipartFile multipartFile, SysFile upload) throws Exception {
        String[] types = {"jar", "application/octet-stream", "text/plain"};
        if (!FileUpload.isContentType(multipartFile, types)) {
            throw new ResultException(ResultEnum.NO_FILE_TYPE);
        }
        // 判断文件是否存在
        SysFile uploadSha1 = uploadService.getBySha1(FileUpload.getFileSha1(multipartFile));
        if (uploadSha1 != null) {
            return ResultVoUtil.success(uploadSha1);
        }
        FileUpload.transferTo(multipartFile, upload);
        // 将文件信息保存到数据库中
        uploadService.save(upload);

        return ResultVoUtil.success(upload);
    }

}