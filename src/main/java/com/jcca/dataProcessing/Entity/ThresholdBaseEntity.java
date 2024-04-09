package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.util.Objects;

/**
 * @description: 阈值相关配置
 * @author: Lvyp
 * @create: 2023/11/05 13:08
 */
@Data
public class ThresholdBaseEntity {

    /**
     * 普通阈值设定值
     */
    private Double baseValue;

    /**
     * 第一层阈值 最大的
     */
    private Double oneLevelValue;
    /**
     * 第二层阈值 中间的
     */
    private Double twoLevelValue;
    /**
     * 第三层阈值  最小的
     */
    private Double threeLevelValue;

    /**
     * 上下限上限
     */
    private Double maxValue;
    /**
     * 上下线下限
     */
    private Double minValue;


    /**
     * 判定一阶阈值已经设定
     * @author lvyp
     * @return
     */
    public boolean oneLevelIsNull(){
        return Objects.isNull(oneLevelValue);
    }
    /**
     * 判定二阶阈值已经设定
     * @author lvyp
     * @return
     */
    public boolean twoLevelIsNull(){
        return Objects.isNull(twoLevelValue);
    }
    /**
     * 判定三阶阈值已经设定
     * @author lvyp
     * @return
     */
    public boolean threeLevelIsNull(){
        return Objects.isNull(threeLevelValue);
    }

    /**
     * 判定普通阈值已经设定
     * @author lvyp
     * @return
     */
    public boolean baseValueIsNull(){
        return Objects.isNull(baseValue);
    }

    /**
     * 判定上下限阈值已经设定
     * @author lvyp
     * @return
     */
    public boolean sectionValueIsNull(){
        return Objects.isNull(minValue)||Objects.isNull(maxValue);
    }

}
