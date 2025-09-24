package com.jcca.admin.system.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SysRole;
import com.jcca.common.bean.constant.StatusConst;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-06 12:15:20
 **/
public interface SysRoleMapper extends BaseMapper<SysRole> {


    /**
     * 根据用户ID获取登录用户角色列表
     *
     * @param userId
     * @return
     */
    List<SysRole> findRoleByUserId(String userId);

    /**
     * 获取最大排序数
     *
     * @param pid
     * @return
     */
    Byte getSortMax(String pid);
}
