package com.genersoft.iot.vmp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genersoft.iot.vmp.analysis.bean.VideoWindowInfo;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * VideoWindowInfo JSON类型处理器
 * @author Claude
 */
@MappedTypes({VideoWindowInfo.class})
@MappedJdbcTypes({JdbcType.VARCHAR})
public class VideoWindowInfoTypeHandler extends BaseTypeHandler<VideoWindowInfo> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, VideoWindowInfo parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Error converting VideoWindowInfo to JSON string", e);
        }
    }

    @Override
    public VideoWindowInfo getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public VideoWindowInfo getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public VideoWindowInfo getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private VideoWindowInfo parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(json, VideoWindowInfo.class);
        } catch (Exception e) {
            // 如果解析失败，返回null而不是抛出异常
            return null;
        }
    }
}