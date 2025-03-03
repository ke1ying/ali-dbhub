package com.alibaba.dbhub.server.domain.support.util;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import com.alibaba.dbhub.server.domain.support.enums.CellTypeEnum;
import com.alibaba.dbhub.server.domain.support.enums.DbTypeEnum;
import com.alibaba.dbhub.server.domain.support.model.Cell;
import com.alibaba.dbhub.server.tools.base.excption.CommonErrorEnum;
import com.alibaba.dbhub.server.tools.base.excption.SystemException;
import com.alibaba.dbhub.server.tools.common.util.EasyOptionalUtils;
import com.alibaba.druid.DbType;

/**
 * jdbc工具类
 *
 * @author Jiaju Zhuang
 */
public class JdbcUtils {

    /**
     * 获取德鲁伊的的数据库类型
     *
     * @param dbType
     * @return
     */
    public static DbType parse2DruidDbType(DbTypeEnum dbType) {
        switch (dbType) {
            case H2:
                return DbType.h2;
            case MYSQL:
                return DbType.mysql;
            case ORACLE:
                return DbType.oracle;
            case SQLITE:
                return DbType.sqlite;
            default:
                throw new SystemException(CommonErrorEnum.PARAM_ERROR);
        }
    }

    /**
     * 获取一个返回值
     *
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    public static Cell getResultSetValue(ResultSet rs, int index) throws SQLException {
        Cell cell = new Cell();
        Object obj = rs.getObject(index);
        if (obj == null) {
            cell.setType(CellTypeEnum.EMPTY.getCode());
            return cell;
        }

        if (obj instanceof Blob) {
            Blob blob = (Blob)obj;
            cell.setType(CellTypeEnum.BYTE.getCode());
            cell.setByteValue(blob.getBytes(1, (int)blob.length()));
            return cell;
        }
        if (obj instanceof Clob) {
            Clob clob = (Clob)obj;
            cell.setType(CellTypeEnum.STRING.getCode());
            cell.setStringValue(clob.getSubString(1, (int)clob.length()));
            return cell;
        }
        if (obj instanceof Timestamp) {
            Timestamp timestamp = (Timestamp)obj;
            cell.setType(CellTypeEnum.DATA.getCode());
            cell.setDateValue(EasyOptionalUtils.mapTo(timestamp, Timestamp::getTime));
            return cell;
        }

        String className = obj.getClass().getName();
        if ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className)) {
            cell.setType(CellTypeEnum.DATA.getCode());
            cell.setDateValue(EasyOptionalUtils.mapTo(rs.getTimestamp(index), Timestamp::getTime));
            return cell;
        }
        if (className.startsWith("oracle.sql.DATE")) {
            String metaDataClassName = rs.getMetaData().getColumnClassName(index);
            cell.setType(CellTypeEnum.DATA.getCode());
            if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
                cell.setDateValue(EasyOptionalUtils.mapTo(rs.getTimestamp(index), Timestamp::getTime));
            } else {
                cell.setDateValue(EasyOptionalUtils.mapTo(rs.getDate(index), Date::getTime));
            }
            return cell;
        }
        if (obj instanceof java.sql.Date) {
            if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
                cell.setType(CellTypeEnum.DATA.getCode());
                cell.setDateValue(EasyOptionalUtils.mapTo(rs.getDate(index), Date::getTime));
                return cell;
            }
        }
        if (obj instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime)obj;
            cell.setType(CellTypeEnum.DATA.getCode());
            cell.setDateValue(localDateTime.toInstant(ZoneOffset.ofHours(+8)).toEpochMilli());
            return cell;
        }
        if (obj instanceof LocalDate) {
            LocalDate localDate = (LocalDate)obj;
            cell.setType(CellTypeEnum.DATA.getCode());
            cell.setDateValue(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            return cell;
        }
        if (obj instanceof Number) {
            cell.setType(CellTypeEnum.BIG_DECIMAL.getCode());
            cell.setBigDecimalValue(new BigDecimal(obj.toString()));
            return cell;
        }
        cell.setType(CellTypeEnum.STRING.getCode());
        cell.setStringValue(obj.toString());
        return cell;
    }
}
