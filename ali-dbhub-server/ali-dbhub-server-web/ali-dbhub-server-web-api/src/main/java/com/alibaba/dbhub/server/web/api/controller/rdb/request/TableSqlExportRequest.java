package com.alibaba.dbhub.server.web.api.controller.rdb.request;

import javax.validation.constraints.NotNull;

import com.alibaba.dbhub.server.web.api.controller.data.source.request.DataSourceBaseRequest;
import com.alibaba.dbhub.server.web.api.controller.data.source.request.DataSourceBaseRequest;

import lombok.Data;

/**
 * @author moji
 * @version ConnectionQueryRequest.java, v 0.1 2022年09月16日 14:23 moji Exp $
 * @date 2022/09/16
 */
@Data
public class TableSqlExportRequest extends DataSourceBaseRequest {

    /**
     * 表名称
     */
    @NotNull
    private String tableName;

    /**
     * 控制台id
     */
    @NotNull
    private Long consoleId;

}
