package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 动环BEAN
 * @author:
 * @create: 2024/09/11 14:31
 */
@Data
public class DongHuanEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String flag;

}
