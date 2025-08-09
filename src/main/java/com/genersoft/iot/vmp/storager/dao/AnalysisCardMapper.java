package com.genersoft.iot.vmp.storager.dao;

import com.genersoft.iot.vmp.analysis.bean.AnalysisCard;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 分析卡片数据访问层
 * @author Claude
 */
@Mapper
@Repository
public interface AnalysisCardMapper {

    /**
     * 新增分析卡片
     */
    @Insert("INSERT INTO wvp_analysis_card (id, title, description, icon, tags, enabled, prompt, model_type, analysis_config, created_by, created_at, updated_at) " +
            "VALUES (#{id}, #{title}, #{description}, #{icon}, #{tags,typeHandler=com.genersoft.iot.vmp.utils.JsonTypeHandler}, " +
            "#{enabled}, #{prompt}, #{modelType}, #{analysisConfig,typeHandler=com.genersoft.iot.vmp.utils.JsonTypeHandler}, " +
            "#{createdBy}, #{createdAt}, #{updatedAt})")
    int insert(AnalysisCard card);

    /**
     * 更新分析卡片
     */
    @Update({" <script>" +
            "UPDATE wvp_analysis_card " +
            "SET updated_at = NOW() " +
            "<if test=\"title != null\">, title = #{title}</if>" +
            "<if test=\"description != null\">, description = #{description}</if>" +
            "<if test=\"icon != null\">, icon = #{icon}</if>" +
            "<if test=\"tags != null\">, tags = #{tags,typeHandler=com.genersoft.iot.vmp.utils.JsonTypeHandler}</if>" +
            "<if test=\"enabled != null\">, enabled = #{enabled}</if>" +
            "<if test=\"prompt != null\">, prompt = #{prompt}</if>" +
            "<if test=\"modelType != null\">, model_type = #{modelType}</if>" +
            "<if test=\"analysisConfig != null\">, analysis_config = #{analysisConfig,typeHandler=com.genersoft.iot.vmp.utils.JsonTypeHandler}</if>" +
            "WHERE id = #{id}" +
            " </script>"})
    int update(AnalysisCard card);

    /**
     * 删除分析卡片
     */
    @Delete("DELETE FROM wvp_analysis_card WHERE id = #{id}")
    int delete(@Param("id") String id);

    /**
     * 根据ID查询分析卡片
     */
    @Select("SELECT id, title, description, icon, " +
            "tags, enabled, prompt, model_type as modelType, analysis_config as analysisConfig, " +
            "created_by as createdBy, created_at as createdAt, updated_at as updatedAt " +
            "FROM wvp_analysis_card WHERE id = #{id}")
    @Results({
        @Result(property = "tags", column = "tags", typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class),
        @Result(property = "analysisConfig", column = "analysis_config", typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class)
    })
    AnalysisCard selectById(@Param("id") String id);

    /**
     * 查询所有分析卡片
     */
    @Select({" <script>" +
            "SELECT id, title, description, icon, " +
            "tags, enabled, prompt, model_type as modelType, analysis_config as analysisConfig, " +
            "created_by as createdBy, created_at as createdAt, updated_at as updatedAt " +
            "FROM wvp_analysis_card " +
            "<where>" +
            "<if test=\"enabled != null\"> AND enabled = #{enabled}</if>" +
            "<if test=\"createdBy != null\"> AND created_by = #{createdBy}</if>" +
            "<if test=\"title != null and title != ''\"> AND title LIKE CONCAT('%', #{title}, '%')</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            " </script>"})
    @Results({
        @Result(property = "tags", column = "tags", typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class),
        @Result(property = "analysisConfig", column = "analysis_config", typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class)
    })
    List<AnalysisCard> selectAll(@Param("enabled") Boolean enabled, 
                                @Param("createdBy") String createdBy,
                                @Param("title") String title);

    /**
     * 查询启用的分析卡片
     */
    @Select("SELECT id, title, description, icon, " +
            "tags, enabled, prompt, model_type as modelType, analysis_config as analysisConfig, " +
            "created_by as createdBy, created_at as createdAt, updated_at as updatedAt " +
            "FROM wvp_analysis_card WHERE enabled = true ORDER BY created_at DESC")
    @Results({
        @Result(property = "tags", column = "tags", typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class),
        @Result(property = "analysisConfig", column = "analysis_config", typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class)
    })
    List<AnalysisCard> selectEnabled();

    /**
     * 根据创建人查询分析卡片
     */
    @Select("SELECT id, title, description, icon, " +
            "tags, enabled, prompt, model_type as modelType, analysis_config as analysisConfig, " +
            "created_by as createdBy, created_at as createdAt, updated_at as updatedAt " +
            "FROM wvp_analysis_card WHERE created_by = #{createdBy} ORDER BY created_at DESC")
    @Results({
        @Result(property = "tags", column = "tags", typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class),
        @Result(property = "analysisConfig", column = "analysis_config", typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class)
    })
    List<AnalysisCard> selectByCreatedBy(@Param("createdBy") String createdBy);

    /**
     * 统计分析卡片数量
     */
    @Select({" <script>" +
            "SELECT COUNT(*) FROM wvp_analysis_card " +
            "<where>" +
            "<if test=\"enabled != null\"> AND enabled = #{enabled}</if>" +
            "<if test=\"createdBy != null\"> AND created_by = #{createdBy}</if>" +
            "</where>" +
            " </script>"})
    long count(@Param("enabled") Boolean enabled, @Param("createdBy") String createdBy);

    /**
     * 批量启用/禁用分析卡片
     */
    @Update({" <script>" +
            "UPDATE wvp_analysis_card SET enabled = #{enabled}, updated_at = NOW() " +
            "WHERE id IN " +
            "<foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">" +
            "#{id}" +
            "</foreach>" +
            " </script>"})
    int batchUpdateEnabled(@Param("ids") List<String> ids, @Param("enabled") Boolean enabled);
}