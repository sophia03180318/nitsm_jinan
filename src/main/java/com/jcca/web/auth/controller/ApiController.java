package com.jcca.web.auth.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.admin.system.service.SysActionLogService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.SysUserService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.NavigationEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppListUtils;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.auth.constant.TokenConst;
import com.jcca.web.auth.memory.TempMemory;
import com.jcca.web.auth.service.LoginSecurityService;
import com.jcca.web.auth.service.impl.LoginSecurityServiceImpl;
import com.jcca.web.auth.util.TokenUtil;
import com.jcca.web.auth.vo.LoginUserVo;
import com.jcca.web.auth.vo.NavigationVo;
import com.jcca.web.auth.vo.SysDictVo;
import com.jcca.web.auth.vo.SysUserVo;
import com.jcca.web.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 外部公共接口,不需要权限
 *
 * @author hanwone
 * @date 2019/4/9
 */
@RestController
@Api(tags = "外部公共接口")
@RequestMapping("/api")
public class ApiController {

    @Resource
    private SysUserService userService;
    @Resource
    private AssetService assetService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private CabinetService cabinetService;
    @Resource
    private RoomService roomService;
    @Resource
    private LoginSecurityService loginSecurityServ;

    @PostMapping("/auth")
    @ApiOperation(value = "外部用户登录")
    @ActionLog(name = "前端用户登录", title = "登录管理", key = LogTypeConstant.LOGIN_OUT)
    public ResultVo<Object> auth(@Validated @RequestBody LoginUserVo loginUserVo, HttpServletRequest request) {
        // 判断账号密码是否为空
        String username = loginUserVo.getUsername();
        String password = loginUserVo.getPassword();

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return ResultVoUtil.error(ResultEnum.USERNAME_PWD_NULL.getCode(),
                    ResultEnum.USERNAME_PWD_NULL.getMessage());
        }

        // 根据用户名获取系统用户数据
        SysUser user = userService.findByUsername(username);
        if (Objects.isNull(user)) {
            return ResultVoUtil.error(ResultEnum.USER_EXIST_ERROR.getCode(), ResultEnum.USER_EXIST_ERROR.getMessage());
        }

        //增加登录用户安全审计
        String remoteAddr = LoginSecurityServiceImpl.getIpAddr(request);
        try {
            loginSecurityServ.securityVerify(user.getId(), remoteAddr);
        } catch (ResultException e) {
            return ResultVoUtil.error(ResultEnum.LOGIN_SECURITY_ERROR.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultVoUtil.error(ResultEnum.LOGIN_SECURITY_ERROR.getCode(), ResultEnum.LOGIN_SECURITY_ERROR.getMessage());
        }

        Subject subject = SecurityUtils.getSubject();

        // 同一账号多处登录判断
        DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        DefaultWebSessionManager sessionManager = (DefaultWebSessionManager) securityManager.getSessionManager();
        Collection<Session> activeSessions = sessionManager.getSessionDAO().getActiveSessions();
        for (Session session : activeSessions) {
            if (subject.getSession().getId().equals(session.getId())) {
                break;
            }

            if (username.equals(session.getAttribute(AdminConst.LOGIN_USER_V))) {
                AppLogUtils.buildLogInfo(LogFunctionEnum.LOGIN_WEB, username, "已登录强制退出前一次登录");
                subject.logout();
                sessionManager.getSessionDAO().delete(session);
            }
        }

        subject.login(new UsernamePasswordToken(username, password));

        subject.getSession().setAttribute(AdminConst.LOGIN_USER_V, username);

        SysUserVo json = new SysUserVo();
        BeanUtils.copyProperties(user, json);

        TempMemory.USERNAME_SET.add(username);

        String token = TokenUtil.getToken(user, TokenConst.TOKEN_SECRECT, TokenConst.TOKEN_EXPIRY_AMOUNT);
        Map<String, Object> map = new HashMap<>(16);
        map.put("user", json);
        map.put("token", new String(Base64.getEncoder().encode(token.getBytes())));
        map.put("perms", ShiroUtil.getSubjectPerms());

        AppLogUtils.buildLogInfo(LogFunctionEnum.LOGIN_WEB, username, "前端登录成功，用户IP：" + remoteAddr);
        return ResultVoUtil.success("登录成功", map);
    }


    /**
     * 退出登录
     */
    @GetMapping("/quit")
    @ApiOperation(value = "退出登录")
    @ActionLog(name = "前端用户退出", title = "登录管理", key = LogTypeConstant.LOGIN_OUT)
    public ResultVo quit() {
        Subject subject = SecurityUtils.getSubject();
        if (Objects.nonNull(subject) && Objects.nonNull(subject.getPrincipal())) {
            String username = ShiroUtil.getSubject().getUsername();
            subject.logout();

            TempMemory.USERNAME_SET.remove(username);
            TempMemory.USER_ID_MAP_V1.remove(username);
            TempMemory.USER_ID_MAP_V2.remove(username);
            subject.logout();

            WebSocketServer.sessionPool.remove(username);

            AppLogUtils.buildLogInfo(LogFunctionEnum.LOGIN_WEB, username, "手动退出成功");
        }

        return ResultVoUtil.success("成功");
    }

    /**
     * 获取登录用户权限列表
     *
     * @return
     */
    @GetMapping("/auth/list")
    @ApiOperation(value = "用户权限列表")
    public ResultVo authList() {
        // 根据登录用户角色获取用户权限
        return ResultVoUtil.success(ResultEnum.SUCCESS.getMessage(), ShiroUtil.getSubjectPerms());
    }

    /**
     * 获取登录用户目录列表
     *
     * @return
     */
    @GetMapping("/v2/dir/list")
    @ApiOperation(value = "用户目录列表")
    public ResultVo authDir() {

        return ResultVoUtil.success(ResultEnum.SUCCESS.getMessage(), ShiroUtil.getSubjectDirsV2());
    }


    /**
     * 获取登录用户组织列表
     *
     * @param flag 做为西宁线路图组织结构是返回路局ID还是中心ID的标记。
     *             0返回中心ID，1返回路局ID
     * @return
     */
    @GetMapping("/org/list")
    @ApiOperation(value = "用户组织列表")
    public ResultVo orgList(Integer flag) {
        SysOrg org = orgService.getDefaultOrg(flag);
        if (Objects.isNull(org)) {
            return ResultVoUtil.error("请分配组织");
        }
        Map<String, Object> map = new HashMap<>(16);
        map.put("id", org.getId());
        map.put("title", org.getTitle());

        List<SysOrg> subjectOrgs = ShiroUtil.getSubjectOrgs();
        //将线提至与中心平级  PID设置为局
        QueryWrapper<SysOrg> qw = new QueryWrapper<>();
        qw.eq("TYPE", OrgTypeConst.GROUP);
        qw.eq("STATUS", "1");
        qw.orderByAsc("TITLE");
        List<SysOrg> list = orgService.list(qw);
        String pid = "0";
        if (ObjectUtil.isNotNull(list) && !list.isEmpty()) {
            pid = list.get(0).getId();
        }
        ArrayList<SysOrg> sysOrgs = new ArrayList<>();
        for (SysOrg subjectOrg : subjectOrgs) {
            subjectOrg.setTruePid(subjectOrg.getPid());
            if (subjectOrg.getType().byteValue() == OrgTypeConst.LINE) {
                subjectOrg.setPid(pid);
            }
            sysOrgs.add(subjectOrg);
        }

        List<SysOrg> collect = sysOrgs.stream().sorted(Comparator.comparingInt(SysOrg::getType).thenComparing(SysOrg::getSort)).collect(Collectors.toList());
        map.put("orgList", collect);
        return ResultVoUtil.success(ResultEnum.SUCCESS.getMessage(), map);
    }

    /**
     * 获取选中组织内资产列表
     *
     * @return
     */
    @GetMapping("/asset/list/{orgId}")
    @ApiOperation(value = "组织内资产列表")
    public ResultVo assetList(@PathVariable("orgId") String orgId, String assetMode, Integer watch) {
        QueryWrapper<Asset> query = Wrappers.query();
        query.select("id", "name", "ip", "ORG_ID", "ip2", "A_B_FLAG", "status");
        if (!"x".equals(orgId)) {
            query.eq("org_id", orgId);
        }
        if (StrUtil.isNotEmpty(assetMode)) {
            query.eq("ASSET_MODE", assetMode);
        }
        if (watch != null) {
            query.eq("WATCH", watch);
        }
        query.eq("IS_DEL", StatusEnum.OK.getCode());
        List<Asset> assetList = assetService.list(query);

        return ResultVoUtil.success(ResultEnum.SUCCESS.getMessage(), assetList);
    }

    @GetMapping("/asset/list")
    @ApiOperation(value = "组织内资产列表")
    public ResultVo assetList(@RequestParam("orgIdList") List<String> orgIdList, String assetMode, Integer watch) {
        // 根据机房查设备 syt
        // 默认展示所有的设备
        QueryWrapper<AssetAttach> attach = Wrappers.query();
        List<String> assetIds;
        if (CollectionUtil.isNotEmpty(orgIdList)) {
            attach.in("ROOM_ID", orgIdList);
            assetIds = assetAttachService.list(attach).stream().map(AssetAttach::getAssetId).collect(Collectors.toList());
        } else {
            assetIds = assetAttachService.list().stream().map(AssetAttach::getAssetId).collect(Collectors.toList());
        }

        QueryWrapper<Asset> query = Wrappers.query();
        query.select("id", "name", "ip", "ORG_ID", "ip2", "A_B_FLAG");
        if (StrUtil.isNotEmpty(assetMode)) {
            query.eq("ASSET_MODE", assetMode);
        }
        if (watch != null) {
            query.eq("WATCH", watch);
        }
        query.eq("IS_DEL", StatusEnum.OK.getCode());

        if (CollectionUtil.isNotEmpty(assetIds)) {
            List<List<String>> inSplit = AppListUtils.inSplit(assetIds, 900);
            Consumer<QueryWrapper<Asset>> consumer = null;
            boolean onces = true;
            for (List<String> list : inSplit) {
                if (onces) {
                    consumer = wrapper -> wrapper.in("ID", list);
                    onces = false;
                } else {
                    Consumer<? super QueryWrapper<Asset>> after = wrapper -> wrapper.or().in("ID", list);
                    consumer = consumer.andThen(after);
                }
            }
            if (Objects.nonNull(consumer)) {
                query.and(consumer);
            }
        } else if (!orgIdList.isEmpty()) {
            List<List<String>> inSplit = AppListUtils.inSplit(orgIdList, 900);
            Consumer<QueryWrapper<Asset>> consumer = null;
            boolean onces = true;
            for (List<String> list : inSplit) {
                if (onces) {
                    consumer = wrapper -> wrapper.in("org_id", list);
                    onces = false;
                } else {
                    Consumer<? super QueryWrapper<Asset>> after = wrapper -> wrapper.or().in("org_id", list);
                    consumer = consumer.andThen(after);
                }
            }
            if (Objects.nonNull(consumer)) {
                query.and(consumer);
            }
        }

        List<Asset> assetList = assetService.list(query);
        // 添加一个 机房id （roomId），机房名（roomName） 组织名称 （orgName） syt
        for (Asset asset : assetList) {
            String roomId = assetAttachService.getById(asset.getId()).getRoomId();
            asset.setRoomId(roomId);
            asset.setRoomName(roomService.getById(roomId).getName());
            asset.setOrgName(orgService.getById(asset.getOrgId()).getTitle());
        }


        return ResultVoUtil.success(ResultEnum.SUCCESS.getMessage(), assetList);

    }

    /**
     * 根据字典标识获取字典值
     *
     * @return
     */
    @GetMapping("/dict/{name}")
    @ApiOperation(value = "根据字典标识获取字典值")
    public ResultVo dictList(@PathVariable(value = "name") String name) {
        Map<String, String> value = DictUtil.value(name);
        if (Objects.isNull(value)) {
            return ResultVoUtil.error("未获取到字典信息：" + name);
        }
        String title = DictUtil.getTitle(name);
        String[] split = title.split("-");
        if (split.length > 0) {
            title = split[0];
        }
        List<SysDictVo> voList = new ArrayList<>();
        Set<Map.Entry<String, String>> entries = value.entrySet();

        for (Map.Entry<String, String> entry : entries) {
            SysDictVo vo = new SysDictVo();
            String key = entry.getKey();
            if (name.equals("WEB_IMAGE")) {
                key = key.replace("@", ":");
            }
            vo.setKey(key);
            vo.setValue(entry.getValue());

            vo.setPKey(name);
            vo.setPValue(title);
            voList.add(vo);
        }

        return ResultVoUtil.success(voList);
    }

    @Resource
    private SysActionLogService actionLogService;

    @GetMapping("/resetdb")
    public void resetdb() {
        actionLogService.resetdb();
    }

    /**
     * 导航栏
     *
     * @return
     */
    @PostMapping("/navigation")
    @ApiOperation(value = "获取导航栏")
    public ResultVo navigation(@Validated @RequestBody NavigationVo reqVo) {
        int type = reqVo.getType();
        String userId = ShiroUtil.getSubject().getId();

        Map<String, List<NavigationVo>> navigationMap = TempMemory.NAVIGATION_MAP;
        List<NavigationVo> navigationVoList = navigationMap.get(userId);
        if (CollectionUtil.isEmpty(navigationVoList) || type == NavigationEnum.INDEX.getCode()) {
            navigationVoList = new LinkedList<>();
            reqVo.setId("0");
            reqVo.setType(NavigationEnum.INDEX.getCode());
            reqVo.setName("首页");
            navigationVoList.add(reqVo);

            navigationMap.put(userId, navigationVoList);

            return ResultVoUtil.success(navigationVoList);
        }

        NavigationVo cabinet = this.setCabinet(reqVo);
        List<NavigationVo> list = new LinkedList<>();
        if (Objects.nonNull(cabinet)) {
            navigationVoList.add(NavigationEnum.CABINET.getCode(), cabinet);
            for (int i = 0; i < type; i++) {
                list.add(navigationVoList.get(i));
            }
        } else {
            list.add(navigationVoList.get(0));
        }

        list.add(reqVo);

        navigationMap.put(userId, list);

        return ResultVoUtil.success(list);
    }

    private NavigationVo setCabinet(NavigationVo reqVo) {
        NavigationVo cabinet = null;
        int type = reqVo.getType();
        Asset asset = assetService.getById(reqVo.getId());
        if (Objects.isNull(asset)) {
            return null;
        }
        reqVo.setAssetMode(asset.getAssetMode());
        if (type == NavigationEnum.ASSET.getCode()) {
            QueryWrapper<AssetAttach> attatchWrapper = Wrappers.query();
            attatchWrapper.eq("asset_id", reqVo.getId());
            AssetAttach attach = assetAttachService.getOne(attatchWrapper);
            if (Objects.nonNull(attach) && StrUtil.isNotEmpty(attach.getCabinetId())) {
                cabinet = new NavigationVo();
                cabinet.setName(cabinetService.getById(attach.getCabinetId()).getName());
                cabinet.setType(NavigationEnum.CABINET.getCode());
                cabinet.setId(attach.getCabinetId());
            }
        }
        return cabinet;
    }
}
