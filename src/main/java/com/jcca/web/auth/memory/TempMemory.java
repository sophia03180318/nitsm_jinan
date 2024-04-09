package com.jcca.web.auth.memory;

import com.jcca.web.auth.vo.NavigationVo;

import java.util.*;

/**
 * @author hanwone
 */
public interface TempMemory {

    /**
     * 导航栏
     */
    Map<String, List<NavigationVo>> NAVIGATION_MAP = new HashMap<>(16);
    /**
     * 登录用户名列表
     */
    Set<String> USERNAME_SET = new HashSet<>();
    /**
     * 记录用户是否弹出过告警框
     */
    Map<String, String> USER_ID_MAP_V1 = new HashMap<>();

    Map<String, String> USER_ID_MAP_V2 = new HashMap<>();
}
