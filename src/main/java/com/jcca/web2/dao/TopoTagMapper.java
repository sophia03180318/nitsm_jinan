package com.jcca.web2.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.vo.AssetHealthDegreeModuleConf;
import com.jcca.web.asset.controller.bean.AssetQueryReq;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.vo.AssetBelong;
import com.jcca.web.asset.vo.AssetExportVo;
import com.jcca.web.graph.vo.GraphStatusVo;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.entity.TopoTag;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @description: 拓扑图页签
 * @author sophia
 * @create: 2023/10/20 09:45
 **/
public interface TopoTagMapper extends BaseMapper<TopoTag> {


}
