package com.jcca.admin.biz.service.bean;


import com.jcca.common.bean.RestBean;
import lombok.Data;

@Data
public class UpdateResult {

    private RestBean result;

    private String version;

    private String commitId;

}
