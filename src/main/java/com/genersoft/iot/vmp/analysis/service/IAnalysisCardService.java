package com.genersoft.iot.vmp.analysis.service;

import com.genersoft.iot.vmp.analysis.bean.AnalysisCard;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 分析卡片服务接口
 * @author Claude
 */
public interface IAnalysisCardService {

    /**
     * 创建分析卡片
     * @param card 分析卡片信息
     * @return 创建成功的卡片
     */
    AnalysisCard createCard(AnalysisCard card);

    /**
     * 更新分析卡片
     * @param card 分析卡片信息
     * @return 更新成功的卡片
     */
    AnalysisCard updateCard(AnalysisCard card);

    /**
     * 删除分析卡片
     * @param cardId 卡片ID
     * @return 是否删除成功
     */
    boolean deleteCard(String cardId);

    /**
     * 根据ID查询分析卡片
     * @param cardId 卡片ID
     * @return 分析卡片
     */
    AnalysisCard getCardById(String cardId);

    /**
     * 根据ID查询分析卡片（隐藏敏感信息）
     * @param cardId 卡片ID
     * @param hidePrompt 是否隐藏提示词
     * @return 分析卡片
     */
    AnalysisCard getCardByIdWithPermission(String cardId, boolean hidePrompt);

    /**
     * 分页查询分析卡片
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @param enabled 是否启用
     * @param createdBy 创建人
     * @param title 标题关键词
     * @return 分页结果
     */
    PageInfo<AnalysisCard> getCardPage(int pageNum, int pageSize, Boolean enabled, String createdBy, String title);

    /**
     * 查询所有启用的分析卡片
     * @return 启用的分析卡片列表
     */
    List<AnalysisCard> getEnabledCards();

    /**
     * 根据创建人查询分析卡片
     * @param createdBy 创建人
     * @return 分析卡片列表
     */
    List<AnalysisCard> getCardsByCreatedBy(String createdBy);

    /**
     * 启用/禁用分析卡片
     * @param cardId 卡片ID
     * @param enabled 是否启用
     * @return 是否操作成功
     */
    boolean enableCard(String cardId, boolean enabled);

    /**
     * 批量启用/禁用分析卡片
     * @param cardIds 卡片ID列表
     * @param enabled 是否启用
     * @return 操作成功的数量
     */
    int batchEnableCards(List<String> cardIds, boolean enabled);

    /**
     * 检查卡片是否可以删除（没有关联的任务）
     * @param cardId 卡片ID
     * @return 是否可以删除
     */
    boolean canDeleteCard(String cardId);

    /**
     * 统计分析卡片数量
     * @param enabled 是否启用
     * @param createdBy 创建人
     * @return 卡片数量
     */
    long countCards(Boolean enabled, String createdBy);

    /**
     * 复制分析卡片
     * @param cardId 源卡片ID
     * @param newTitle 新标题
     * @param createdBy 创建人
     * @return 新创建的卡片
     */
    AnalysisCard copyCard(String cardId, String newTitle, String createdBy);
}