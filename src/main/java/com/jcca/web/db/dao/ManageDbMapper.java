package com.jcca.web.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.db.entity.ManageDb;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理数据库库配置
 *
 * @author Lvyp
 */
@Mapper
public interface ManageDbMapper extends BaseMapper<ManageDb> {

}
