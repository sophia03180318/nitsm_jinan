package com.jcca.common.enums;

import com.jcca.common.exception.interfaces.ResultInterface;
import lombok.Getter;

/**
 * 后台返回结果集枚举
 *
 * @author hanwone
 * @date 2018/8/14
 */
@Getter
public enum ResultEnum implements ResultInterface {

    /**
     * 通用状态
     */
    SUCCESS(200, "请求成功"),
    ERROR(999, "系统未知错误"),
    WARNING(333, "操作错误"),

    /**
     * 账户问题
     */
    USER_EXIST_ERROR(900, "该用户名不存在"),
    USER_EXIST(901, "该用户名已经存在"),
    USER_INEQUALITY(902, "两次密码不一致"),
    USER_OLD_PWD_ERROR(903, "原密码不正确"),
    USERNAME_PWD_NULL(904, "用户名或密码不能为空"),
    USER_CAPTCHA_ERROR(905, "验证码错误"),
    USERNAME_PWD_ERROR(906, "用户名或密码错误"),
    ACCOUNT_FREEZED(907, "账户被冻结"),
    TOKEN_EXPIRY(908, "TOKEN过期"),
    MANY_LOGIN(909, "账号已在其他地方登录"),
    ILLEGAL_IP(9001, "非法IP不允许登录"),
    LOGIN_SECURITY_ERROR(9002, "用户安全审计出错"),

    /**
     * 角色问题
     */
    ROLE_EXIST(910, "角色标识不能重复！"),
    NOT_ADMIN(911, "不是后台管理员！"),
    NEED_LOGIN(912, "需要重新登录"),


    /**
     * 组织问题
     */
    ORG_EXIST_USER(920, "组织存在用户，无法删除"),
    ORG_NULL(921, "组织不存在"),

    /**
     * 字典问题
     */
    DICT_EXIST(930, "该字典标识不能重复！"),

    /**
     * 非法操作
     */
    STATUS_ERROR(940, "非法操作"),
    PWD_ENCRYT_ERROR(941, "密码加密错误"),

    /**
     * 参数问题
     */
    PARAM_ERROR(960, "缺少参数或参数错误"),
    TIME_OUT_ERROR(961, "连接超时"),
    CANNOT_FIND(962, "数据不存在"),
    DUPLICATE(963, "数据重复"),

    /**
     * 权限问题
     */
    NO_PERMISSIONS(950, "请先登录或授权相应操作"),
    NO_ADMIN_AUTH(951, "不允许操作超级管理员"),
    NO_ADMIN_STATUS(952, "不能修改超级管理员状态"),
    NO_ADMINROLE_AUTH(953, "不允许操作管理员角色"),
    ONLY_ADMINROLE_AUTH(954, "请使用管理员账户操作"),

    /**
     * 外部接口问题
     */
    OUT_SUCCESS(0, "成功"),
    OUT_PARAM_LOST(1001, "缺少参数"),
    OUT_COLLECT_TEST_ERROR(1002, "采集指标测试未通过"),
    OUT_PROCESS_ERROR(1003, "进程变动通知异常"),


    /**
     * 模板问题
     */
    TEMPLATE_NAME_EXIST(1101, "该模板名已经存在"),
    TEMPLATE_TYPE_NULL(1102, "模板类型不能为空"),
    TEMPLATE_NAME_NULL(1103, "模板名称不能为空"),
    TEMPLATE_PROCESS_NULL(1104, "模板进程不能为空"),
    TEMPLATE_ID_NULL(1105, "模板ID不能为空"),
    BUSINESS_NAME_EXIST(1106, "业务名称已存在"),
    TEMPLATE_NULL(1107, "请先上传模板文件"),
    TEMPLATE_EMPTY(1108, "不能上传空文件"),
    /**
     * 资产问题
     */
    ASSET_NULL(1201, "资产不存在"),
    ASSET_IMPORT_REP(1202, "已有任务正在运行"),
    ASSET_IMPORT_ERROR(1203, "导入出错"),
    ASSET_ADD_ERROR(1204, "添加资产异常"),
    /**
     * 采集配置
     */
    COLLECTOR_ASSET_MDOE(1301, "适配类型不能为空"),
    COLLECTOR_ASSET_IMAGE(1302, "适配型号不能为空"),
    COLLECTOR_ASSET_FACTORY(1303, "适配厂家不能为空"),
    COLLECTOR_SYSTEM_TYPE(1304, "操作系统不能为空"),
    COLLECTOR_SPEC_DICT(1305, "执行代码不能为空"),
    COLLECTOR_NULL(1306, "不可为空"),
    COLLECTOR_NUM(1307, "请填写1-59之间的整数"),

    /**
     * 巡检管理
     */
    INSPECT_NO_ASSET(1401, "请先选择设备"),
    INSPECT_BEGIN(1402, "有巡检任务正在执行，完成后可开始新的巡检"),
    INSPECT_PAUSE(1403, "巡检已暂停"),
    INSPECT_END(1404, "巡检已结束"),
    INSPECT_NO(1405, "巡检未开始"),
    INSPECT_NO_DATA(1406, "正在准备巡检数据"),
    INSPECT_PING_ERROR(1407, "巡检设备网络不通"),
    INSPECT_SCHEDULE_ERROR(1408, "添加巡检任务异常"),
    INSPECT_COLLECT_ERROR(1409, "巡检采集异常"),

    // 菜单、角色、组织共用的删除更新
    DATA_DELETE(1501, "请先删除下级数据"),
    UPDATE_FAIL(1502, "更新失败，请检查后再操作"),
    ID_PID_EQUAL(1503, "上级不能是自己"),

    /**
     * 文件操作
     */
    DOWNLOAD_ERROR(1601, "文件下载失败"),
    UPLOAD_ERROR(1602, "文件上传失败"),
    NO_FILE_NULL(1603, "文件不能为空"),
    NO_FILE_TYPE(1604, "不支持该文件类型"),

    /**
     * 性能数据处理
     */
    dataProcess_interface_interrupter(1701, "端口数据信息主动终止处理"),
    dataProcess_net_interrupter(1702, "网卡数据信息主动终止处理"),
    dataProcess_syslog_interrupter(1703, "syslog事件主动终止处理"),
    dataProcess_net_ip_null(1704, "网卡无ip地址,已过滤"),
    //执行失败
    DETAIL_EXE_ERROR(8000, "设备信息查询失败，不支持的数据源导致数据查询失败");

    private Integer code;

    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
