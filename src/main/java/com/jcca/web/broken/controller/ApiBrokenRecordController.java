package com.jcca.web.broken.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.admin.system.service.SysFileService;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.BrokenRecordConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.*;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.*;
import com.jcca.common.utils.file.FileUpload;
import com.jcca.common.utils.file.config.properties.UploadProjectProperties;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.broken.controller.bean.*;
import com.jcca.web.broken.entity.BrokenRecord;
import com.jcca.web.broken.entity.BrokenRecordFile;
import com.jcca.web.broken.entity.BrokenRecordOpinion;
import com.jcca.web.broken.service.BrokenRecordFileService;
import com.jcca.web.broken.service.BrokenRecordOpinionService;
import com.jcca.web.broken.service.BrokenRecordService;
import com.jcca.web.broken.vo.BrokenRecordOpinionVo;
import com.jcca.web.broken.vo.BrokenRecordVo;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.service.BizManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 故障记录
 *
 * @author lyp
 */

@Slf4j
@Api(tags = "故障记录相关接口")
@RestController
@RequestMapping("/api/brokenRecord")
public class ApiBrokenRecordController {

    private static final String DOWN_URI = "/api/brokenRecord/download";

    private static final String UPLOAD_MODEL_PROT_PATH = "/brokenProd";

    @Resource
    private BrokenRecordService brokenRecordService;


    @Resource
    private BrokenRecordFileService brokenRecordFileServ;
    @Resource
    private SysFileService fileService;
    @Resource
    private AssetService assetService;
    @Resource
    private BizManageService bizService;
    @Resource
    private BrokenRecordOpinionService opinionService;
    @Resource
    private UploadProjectProperties fileProp;
    @Value("${project.base-url}")
    private String baseUrl;

    /**
     * 故障记录分页查询
     *
     * @param req
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    @ApiOperation(value = "故障记录分页查询")
    @PostMapping("/pageQuery")
    @ActionLog(name = "查看故障记录", title = "故障记录", key = LogTypeConstant.QUERY)
    ResultVo pageQuery(@RequestBody BrokenRecordPageReq req) {
        IPage iPage = queryPageBean(req);
        List records = iPage.getRecords();
        List<BrokenRecordVo> copyList = EntityBeanUtil.copyList(records, BrokenRecordVo.class);
        for (BrokenRecordVo item : copyList) {
            JSONObject assetObj = new JSONObject();
            Asset asset = assetService.getById(item.getAssetId());
            if (Objects.isNull(asset)) continue;
            item.setAssetIp(asset.getIp());
            item.setOrgId(asset.getOrgId());
            item.setAssetName(asset.getName());

            assetObj.put("name", asset.getName());
            assetObj.put("ip", asset.getIp());
            assetObj.put("id", asset.getId());
            assetObj.put("orgId", asset.getOrgId());
            item.setAssetList(Collections.singletonList(assetObj));
            item.setStatusStr(BrokenRecordEnum.getName(item.getStatus()));
        }
        PageBean<BrokenRecordVo> pageBean = new PageBean<BrokenRecordVo>();
        pageBean.setContent(copyList);
        pageBean.setTotal(iPage.getTotal());

        return ResultVoUtil.success(pageBean);
    }

    /**
     * 故障记录附件列表查询
     *
     * @param id
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    @ApiOperation(value = "故障记录附件列表")
    @PostMapping("/fileList/{id}")
    @ActionLog(name = "查看故障记录附件列表", title = "故障记录", key = LogTypeConstant.QUERY)
    ResultVo fileList(@PathVariable("id") String id) {
        if (StrUtil.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "请输入故障id");
        }
        QueryWrapper<BrokenRecordFile> wrapper = new QueryWrapper<BrokenRecordFile>();
        wrapper.eq("BROKEN_RECORD_ID", id);
        List<BrokenRecordFile> list = brokenRecordFileServ.list(wrapper);

        List<JSONObject> respList = new ArrayList<JSONObject>();
        for (BrokenRecordFile item : list) {
            JSONObject parseObj = JSONUtil.parseObj(item);
            SysFile file = fileService.getById(item.getSysFileId());
            if (Objects.isNull(file)) {
                continue;
            }
            parseObj.put("downloadUrl", baseUrl + DOWN_URI + item.getSysFileId());
            parseObj.put("orignName", file.getOrignName());
            respList.add(parseObj);
        }

        return ResultVoUtil.success(respList);
    }

    /**
     * 文件下载
     *
     * @param id
     */
    @ApiOperation(value = "故障记录附件下载")
    @RequiresPermissions({"api:brokenRecord:download"})
    @GetMapping("/download")
    @ActionLog(name = "下载故障记录附近", title = "故障记录", key = LogTypeConstant.DOWNLOAD)
    void downloadFile(String id, HttpServletRequest request, HttpServletResponse response) {
        if (StrUtil.isEmpty(id)) {
            log.error("【故障记录附件下载请求ID空】");
            return;
        }
        SysFile file = fileService.getById(id);
        if (Objects.isNull(file)) {
            log.error("【故障记录附件下载请求文件 不存在】");
            return;
        }
        String pathname = fileProp.getFilePath() + file.getFilePath().replace(fileProp.getStaticPath(), "");
        File downFile = new File(pathname.replace("///", "/"));
        if (!downFile.exists()) {
            log.error("故障记录-下载文件不存在：{}", pathname);
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
                out.close();
            } catch (NullPointerException e) {
                log.error("responseFileStream stream close() error:NullPointerException:{}", e.getMessage(), e);
            } catch (Exception e) {
                log.error("responseFileStream stream close() error:{}", e.getMessage(), e);
            }
        }
    }

    /**
     * 文件预览
     *
     * @return
     */
    @ApiOperation(value = "故障记录附件预览")
    @RequiresPermissions({"api:brokenRecord:preview"})
    @PostMapping("/preview/{id}")
    @ActionLog(name = "故障记录文件预览", title = "故障记录", key = LogTypeConstant.QUERY)
    ResultVo<String> preview(@PathVariable("id") String id) {
        if (StrUtil.isEmpty(id)) {
            log.error("【故障记录附件预览请求ID空】");
            return ResultVoUtil.paramError("【故障记录附件预览请求ID空】", String.class);
        }
        SysFile file = fileService.getById(id);
        if (Objects.isNull(file)) {
            log.error("【故障记录附件预览请求文件 不存在】");
            return ResultVoUtil.paramError("【故障记录附件预览请求文件 不存在】", String.class);
        }
        try {
            String previewUrl = FileUpload.getPreviewUrl(file);
            return ResultVoUtil.success(previewUrl);
        } catch (Exception e) {
            log.error("故障记录附件预览失败：{}", e.getMessage(), e);
        }

        return ResultVoUtil.error("预览文件失败");
    }

    /**
     * 文件移除
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "故障记录删除附件")
    @RequiresPermissions({"api:brokenRecord:removeFile"})
    @PostMapping("/removeFile")
    @ActionLog(name = "删除故障记录文件", title = "故障记录", key = LogTypeConstant.REMOVEE)
    ResultVo<String> removeFile(@RequestBody BrokenRecordRemoveFileReq req) {
        QueryWrapper<BrokenRecordFile> queryWrapper = new QueryWrapper<BrokenRecordFile>();
        queryWrapper.eq("SYS_FILE_ID", req.getIds());
        queryWrapper.eq("BROKEN_RECORD_ID", req.getBrokenRecordId());
        List<BrokenRecordFile> list = brokenRecordFileServ.list(queryWrapper);

        if (Objects.isNull(list)) {
            return ResultVoUtil.paramError("文件不存在", String.class);
        }
        if (list.size() == 0) {
            return ResultVoUtil.paramError("文件不存在", String.class);
        }
        brokenRecordFileServ.remove(queryWrapper);

        return ResultVoUtil.success("删除成功");
    }

    /**
     * 故障记录保存
     *
     * @param req
     * @return
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "故障记录新增")
    @RequiresPermissions({"api:brokenRecord:save"})
    @PostMapping("/save")
    @ActionLog(name = "新增故障记录", title = "故障记录", key = LogTypeConstant.ADD)
    ResultVo save(@Validated @RequestBody BrokenRecordSaveReq req) {
        Asset asset = assetService.getById(req.getAssetId());
        if (Objects.isNull(asset)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "系统未找到该资产" + req.getAssetId());
        }

        BrokenRecord copy = BeanUtil.copyProperties(req, BrokenRecord.class);
        copy.setOrigin(BrokenOriginEnum.MANUAL_WORK.getCode());
        copy.setStatus(BrokenRecordConst.UNPROCESSED);
        brokenRecordService.createBroken(copy);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 故障记录编辑
     *
     * @param req
     * @return
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "故障记录编辑")
    @RequiresPermissions({"api:brokenRecord:update"})
    @PostMapping("/update")
    @ActionLog(name = "修改故障记录", title = "故障记录", key = LogTypeConstant.MODIFY)
    ResultVo update(@Validated @RequestBody BrokenRecordUpdateReq req) {
        Asset asset = assetService.getById(req.getAssetId());
        if (Objects.isNull(asset)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "系统未找到该资产" + req.getAssetId());
        }
        BrokenRecord brokenRecord = brokenRecordService.getById(req.getId());
        if (Objects.isNull(brokenRecord)) {
            ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "编辑的故障记录不存在");
        }
        BrokenRecord record = EntityBeanUtil.replaceParameter(req, brokenRecord, BrokenRecord.class);
        brokenRecordService.updateById(record);

        return ResultVoUtil.success();
    }

    /**
     * 删除
     *
     * @param ids
     * @return
     */
    @ApiOperation(value = "故障记录删除")
    @RequiresPermissions({"api:brokenRecord:remove"})
    @PostMapping("/remove/{id}")
    @ActionLog(name = "删除故障记录", title = "故障记录", key = LogTypeConstant.REMOVEE)
    ResultVo<String> remove(@PathVariable("id") String ids) {
        if (StrUtil.isEmpty(ids)) {
            return ResultVoUtil.paramError("缺少主键", String.class);
        }
        BrokenRecord broken = brokenRecordService.getById(ids);
        if (Objects.isNull(broken)) {
            return ResultVoUtil.paramError("不存在的 记录", String.class);
        }
        brokenRecordService.removeBroken(ids);

        return ResultVoUtil.success("删除成功");
    }

    /**
     * 处理故障记录
     *
     * @return
     */
    @PostMapping("/handle")
    @RequiresPermissions("api:brokenRecord:handle")
    @ApiOperation(value = "处理故障记录")
    @ActionLog(name = "处理故障记录", title = "故障记录", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> handle(@RequestBody RecordHandleReq recordHandleReq) {

        brokenRecordService.handle(recordHandleReq);
        return ResultVoUtil.success();
    }


    @GetMapping("/export/verify")
    @ApiOperation(value = "故障记录导出校验")
    ResultVo exportVerify(BrokenRecordExportReq request) {
        FileUtil.createTmpPath();
        BrokenRecordPageReq req = EntityBeanUtil.copy(request, BrokenRecordPageReq.class);
        if (StrUtil.isNotEmpty(request.getOccurEndTime())) {
            if (request.getOccurEndTime().length() > 11) {
                Date endTime = DateUtil.parse(request.getOccurEndTime(), DatePattern.NORM_DATETIME_PATTERN);
                req.setOccurEndTime(endTime);
            }
        }
        if (StrUtil.isNotEmpty(request.getOccurStartTime())) {
            if (request.getOccurStartTime().length() > 11) {
                Date startTime = DateUtil.parse(request.getOccurStartTime(), DatePattern.NORM_DATETIME_PATTERN);
                req.setOccurStartTime(startTime);
            }
        }

        req.setPage(0);
        req.setSize(20000);
        IPage queryPageBean = this.queryPageBean(req);
        List records = queryPageBean.getRecords();
        if (Objects.isNull(records) || records.isEmpty()) {
            return ResultVoUtil.error("未选择故障记录~");
        }
        return ResultVoUtil.success();
    }


    /**
     * 故障记录导出
     *
     * @param
     * @param response
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "故障记录导出")
    @RequiresPermissions({"api:brokenRecord:export"})
    @GetMapping("/export")
    @ActionLog(name = "导出故障记录", title = "故障记录", key = LogTypeConstant.DOWNLOAD)
    void export(BrokenRecordExportReq request, HttpServletResponse response) {
        BrokenRecordPageReq req = EntityBeanUtil.copy(request, BrokenRecordPageReq.class);
        if (StrUtil.isNotEmpty(request.getOccurEndTime())) {
            if (request.getOccurEndTime().length() > 11) {
                Date endTime = DateUtil.parse(request.getOccurEndTime(), DatePattern.NORM_DATETIME_PATTERN);
                req.setOccurEndTime(endTime);
            }
        }
        if (StrUtil.isNotEmpty(request.getOccurStartTime())) {
            if (request.getOccurStartTime().length() > 11) {
                Date startTime = DateUtil.parse(request.getOccurStartTime(), DatePattern.NORM_DATETIME_PATTERN);
                req.setOccurStartTime(startTime);
            }
        }

        req.setPage(0);
        req.setSize(20000);
        IPage queryPageBean = this.queryPageBean(req);

        List<List<String>> rows = CollUtil.newArrayList();
        List<String> rowOne = Arrays.asList("设备名称", "设备ip", "故障现象", "故障原因", "故障发生时间", "故障等级", "状态", "来源", "签认",
                "故障处理完成时间");
        rows.add(rowOne);

        List records = queryPageBean.getRecords();
        for (Object obj : records) {
            List<String> itemList = new ArrayList<String>();
            BrokenRecord item = (BrokenRecord) obj;

            Asset asset = assetService.getById(item.getAssetId());
            itemList.add(asset.getName());
            itemList.add(asset.getIp());
            itemList.add(item.getDescription());
            itemList.add(item.getReason());
            itemList.add(DateUtil.format(item.getOccurTime(), "yyyy-MM-dd HH:mm:ss"));
            if (item.getAlarmLevel() == null) {
                itemList.add("");
            } else {
                itemList.add(AlarmLevelEnum.getMsg(item.getAlarmLevel().intValue()));
            }
            itemList.add(AlarmStatusEnum.getMsg(item.getStatus()));
            itemList.add(BrokenOriginEnum.getMsg(item.getOrigin()));
            itemList.add(item.getCreator());
            itemList.add(DateUtil.format(item.getCompleteTime(), "yyyy-MM-dd HH:mm:ss"));
            rows.add(itemList);
        }

        ExcelWriter writer = ExcelUtil.getWriter();
        writer.merge(rowOne.size() - 1, "故障记录(导出人：" + ShiroUtil.getSubject().getUsername() + ")");
        writer.write(rows, true);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");

        ServletOutputStream out = null;
        try {
            String fileName = URLEncoder.encode("故障记录", "utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
            out = response.getOutputStream();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return;
        }
        writer.flush(out);
        writer.close();
        IoUtil.close(out);
    }

    /**
     * 故障记录附件上传
     *
     * @param file
     * @param id
     */
    @SuppressWarnings({"unchecked"})
    @ApiOperation(value = "故障记录附件上传")
    @RequiresPermissions({"api:brokenRecord:upload"})
    @PostMapping("/upload/{id}")
    @ActionLog(name = "上传故障记录附件", title = "故障记录", key = LogTypeConstant.UPLOAD)
    ResultVo<String> upload(@RequestParam("file") MultipartFile[] file, @PathVariable("id") String id) {
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
        String name = multipartFile.getOriginalFilename();
        if (!name.contains(".")) {
            return ResultVoUtil.error("文件上传失败-文件名不合法-缺少后缀");
        }
        BrokenRecord broken = brokenRecordService.getById(id);
        if (Objects.isNull((broken))) {
            return ResultVoUtil.paramError("故障记录不存在", String.class);
        }

        SysFile sysFile = FileUpload.getFile(multipartFile, UPLOAD_MODEL_PROT_PATH);
        try {
            FileUpload.transferTo(multipartFile, sysFile);
        } catch (Exception e) {
            log.error("故障记录文件上传异常：{}", e.getMessage(), e);
            return ResultVoUtil.error("文件保存失败");
        }
        String id2 = MyIdUtil.getId();
        sysFile.setStatus(StatusEnum.OK.getCode());
        sysFile.setId(id2);
        try {
            brokenRecordFileServ.addFile(sysFile, broken);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            return ResultVoUtil.error("保存数据失败");
        }

        return ResultVoUtil.SAVE_SUCCESS;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private IPage queryPageBean(BrokenRecordPageReq req) {
        List<String> bizIds = bizService.listByBizAndOrg(BizManageConstant.BROKEN);

        IPage page = PagePlugin.startPage(req.getPage(), req.getSize());
        QueryWrapper<BrokenRecord> wrapper = new QueryWrapper<>();

        List<List<String>> inSplit = AppListUtils.inSplit(bizIds, 900);

        Consumer<QueryWrapper<BrokenRecord>> consumer = null;
        boolean onces = true;
        for (List<String> list : inSplit) {
            if (onces) {
                consumer = query -> query.in("ID", list);
                onces = false;
            } else {
                Consumer<? super QueryWrapper<BrokenRecord>> after = query -> query.or().in("ID", list);
                consumer = consumer.andThen(after);
            }
        }

        if (Objects.nonNull(consumer)) {
            wrapper.and(consumer);
        }

        String assetIp = req.getAssetIp();
        String assetName = req.getAssetName();
        if (StrUtil.isNotEmpty(assetIp) || StrUtil.isNotEmpty(assetName)) {
            QueryWrapper<Asset> assetWrapper = new QueryWrapper<Asset>();
            if (StrUtil.isNotEmpty(assetName)) {
                assetWrapper.like("NAME", assetName);
            }
            if (StrUtil.isNotEmpty(assetIp)) {
                assetWrapper.like("IP", assetIp);
            }
            List<Asset> assetList = assetService.list(assetWrapper);
            List<String> ids = assetList.stream().map(Asset::getId).collect(Collectors.toList());
            ids.add("X");

            wrapper.in("ASSET_ID", ids);
        }

        if (Objects.nonNull(req.getStatus())) {
            wrapper.eq("STATUS", req.getStatus());
        }
        if (StrUtil.isNotEmpty(req.getDescription())) {
            wrapper.like("DESCRIPTION", req.getDescription());
        }
        if (Objects.nonNull(req.getOccurStartTime()) && Objects.nonNull(req.getOccurEndTime())) {
            wrapper.between("OCCUR_TIME", req.getOccurStartTime(), req.getOccurEndTime());
        }
        if (Objects.nonNull(req.getCreateStartTime()) && Objects.nonNull(req.getCreateEndTime())) {
            wrapper.between("CREATE_TIME", req.getCreateStartTime(), req.getCreateEndTime());
        }
        if (StrUtil.isNotEmpty(req.getAlarmTitle())) {
            wrapper.eq("ALARM_TITLE", req.getAlarmTitle());
        }

        wrapper.orderByAsc("status");
        wrapper.orderByDesc("CREATE_TIME");

        IPage iPage = brokenRecordService.page(page, wrapper);
        return iPage;
    }

    //// ===========================以下为故障记录处理意见相关操作===============================

    /**
     * 获取故障记录处理意见列表
     *
     * @param id 故障记录ID
     * @return
     */
    @GetMapping("/opinionList/{id}")
    @ApiOperation(value = "获取处理意见列表")
    public ResultVo opinionList(@PathVariable("id") String id) {
        QueryWrapper<BrokenRecordOpinion> opinionWrapper = Wrappers.query();
        opinionWrapper.eq("broken_record_id", id);
        opinionWrapper.orderByDesc("CREATE_TIME");
        List<BrokenRecordOpinion> opinionList = opinionService.list(opinionWrapper);
        List<BrokenRecordOpinionVo> resultList = new ArrayList<>();
        for (BrokenRecordOpinion opinion : opinionList) {
            BrokenRecordOpinionVo vo = new BrokenRecordOpinionVo();
            BeanUtil.copyProperties(opinion, vo);
            resultList.add(vo);
        }

        return ResultVoUtil.success(resultList);
    }

    /**
     * 编辑保存处理意见
     *
     * @param opinion
     * @return
     */
    @PostMapping("/saveOpinion")
    @ApiOperation(value = "编辑保存处理意见")
    @RequiresPermissions("api:brokenRecord:saveOpinion")
    @ActionLog(name = "编辑故障记录处理意见", title = "故障记录", key = LogTypeConstant.MODIFY)
    public ResultVo saveOpinion(@RequestBody BrokenRecordOpinion opinion) {

        opinionService.updateById(opinion);

        return ResultVoUtil.SAVE_SUCCESS;
    }


}
