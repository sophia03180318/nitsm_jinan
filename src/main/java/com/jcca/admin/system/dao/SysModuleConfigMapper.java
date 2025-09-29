package com.jcca.admin.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SysModuleConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.mapstruct.Mapper;


/**
 * <p>
 * 系统功能模块参数设置 Mapper 接口
 * </p>
 *
 * @author LuBan
 * @since 2021-01-05
 */
@Mapper
public interface SysModuleConfigMapper extends BaseMapper<SysModuleConfig> {

    /**
     * 更新
     * @param name
     * @param value
     */
    void updateByName(@Param("name") String name,@Param("value") String value);
}
