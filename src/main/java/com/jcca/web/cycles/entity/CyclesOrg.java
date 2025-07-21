package com.jcca.web.cycles.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 影响组织
 * @author: Lvyp
 * @create: 2024/11/20 09:39
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cycles_org")
public class CyclesOrg extends Model<CyclesOrg> implements java.io.Serializable {

    /**
     * id
     */
    private String id;
    /**
     * 周期信息ID
     */
    private String cyclesInfoId;
    /**
     * 父组织ID
     */
    private String orgPid;
    /**
     * 组织ID
     */
    private String orgId;


}
