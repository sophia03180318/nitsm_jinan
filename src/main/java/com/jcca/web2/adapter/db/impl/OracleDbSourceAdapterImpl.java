package com.jcca.web2.adapter.db.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.jcca.web2.adapter.db.DbSourceAdapter;
import com.jcca.web2.adapter.db.DbSourceConstant;
import com.jcca.web2.dto.ExeSqlQuery;
import com.jcca.web2.vo.AssetDataVo;
import com.jcca.web2.vo.BoardObjVo;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @description: Oracle数据源
 * @author: Lvyp
 * @create: 2023/11/01 17:50
 */
@Service
public class OracleDbSourceAdapterImpl implements DbSourceAdapter {

    private static final String SELECT_FLAG = "SELECT";

    @Override
    public String getAdapterCode() {
        return DbSourceConstant.ORACLE.name();
    }

    @Override
    public List<AssetDataVo> exeCommand(ExeSqlQuery query) {
        List<AssetDataVo> respList = new ArrayList<AssetDataVo>();
        String sql = query.getSql();
        String groupSql = query.getGroupSql();
        if (!sql.toUpperCase().startsWith(SELECT_FLAG)) {
            return respList;
        }
        if (StrUtil.isNotEmpty(groupSql) && !groupSql.toUpperCase().startsWith(SELECT_FLAG)) {
            return respList;
        }
        List<String> groupSearchList = new ArrayList<String>();
        if (StrUtil.isNotEmpty(groupSql)) {
            if (StrUtil.isNotEmpty(query.getSearchName()) && StrUtil.isNotEmpty(query.getSearchValue())) {
                groupSql = groupSql + " WHERE " + query.getSearchName() + "=" + query.getSearchValue() + " ";
            }
            groupSql = groupSql + " GROUP BY " + query.getGroupField();
            List<Map<String, Object>> groups = SqlRunner.db().selectList(groupSql);
            for (Map<String, Object> group : groups) {
                Object value = group.get("key");
                groupSearchList.add(value.toString());
            }
        }

        Date startDate = new Date(query.getStart() * 1000);
        Date endDate = new Date(query.getEnd() * 1000);

        String exeSql = String.format("%s and %s >= TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS') and %s <= TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS') ", sql, query.getTimeField(), DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss"), query.getTimeField(), DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));

        if (StrUtil.isNotEmpty(query.getSearchName()) && StrUtil.isNotEmpty(query.getSearchValue())) {
            exeSql = exeSql + "and " + query.getSearchName() + "=" + query.getSearchValue() + " ";
        }

        if (groupSearchList.isEmpty()) {
            AssetDataVo fatten = fatten(exeSql, query, "");
            respList.add(fatten);
            return respList;
        }

        for (String groupField : groupSearchList) {
            String newExeSql = String.format("%s and %s = %s", exeSql, query.getGroupField(), "'" + groupField + "'");
            AssetDataVo fatten = fatten(newExeSql, query, groupField);
            respList.add(fatten);
        }

        return respList;
    }

    /**
     * 封装响应信息
     *
     * @param exeSql
     * @param query
     * @param groupValue
     */
    private AssetDataVo fatten(String exeSql, ExeSqlQuery query, String groupValue) {
        List<List<String>> resultList = new LinkedList<List<String>>();
        AssetDataVo vo = new AssetDataVo();
        List<Map<String, Object>> result = SqlRunner.db().selectList(exeSql + " order by " + query.getTimeField() + " asc");
        for (Map<String, Object> map : result) {
            List<String> linkList = new ArrayList<String>();
            Object key = map.get("key");
            Object value = map.get("value");
            DateTime date = DateUtil.parse(key.toString(), "yyyy-MM-dd HH:mm:ss");
            linkList.add(date.getTime() / 1000 + "");
            if(Objects.isNull(value)){
                linkList.add("");
            }else{
                linkList.add(value.toString());
            }
            resultList.add(linkList);
        }

        BoardObjVo boardObjVo = new BoardObjVo();
        boardObjVo.setQueryName(query.getQueryName());
        if (StrUtil.isNotEmpty(groupValue)) {
            boardObjVo.setGroupItemName(groupValue);
        }
        vo.setValues(resultList);
        vo.setMetric(boardObjVo);

        return vo;
    }

}
