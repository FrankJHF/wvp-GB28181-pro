package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.service.IAnalysisCardService;
import com.genersoft.iot.vmp.analysis.bean.AnalysisCard;
import com.genersoft.iot.vmp.storager.dao.AnalysisCardMapper;
import com.genersoft.iot.vmp.storager.dao.AnalysisTaskMapper;
import com.genersoft.iot.vmp.conf.exception.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 分析卡片服务实现类
 * @author Claude
 */
@Service
@Slf4j
public class AnalysisCardServiceImpl implements IAnalysisCardService {

    @Autowired
    private AnalysisCardMapper analysisCardMapper;

    @Autowired
    private AnalysisTaskMapper analysisTaskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisCard createCard(AnalysisCard card) throws ServiceException {
        log.info("创建分析卡片: {}", card.getTitle());
        
        // 验证必填字段
        validateCard(card, true);
        
        // 生成ID和设置时间
        if (StringUtils.isEmpty(card.getId())) {
            card.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        
        // 设置默认值
        if (card.getEnabled() == null) {
            card.setEnabled(true);
        }
        if (StringUtils.isEmpty(card.getModelType())) {
            card.setModelType("videollama3");
        }
        
        int result = analysisCardMapper.insert(card);
        if (result <= 0) {
            throw new ServiceException("创建分析卡片失败");
        }
        
        log.info("分析卡片创建成功，ID: {}", card.getId());
        return card;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisCard updateCard(AnalysisCard card) throws ServiceException {
        log.info("更新分析卡片: {}", card.getId());
        
        // 验证必填字段
        validateCard(card, false);
        
        // 检查卡片是否存在
        AnalysisCard existingCard = analysisCardMapper.selectById(card.getId());
        if (existingCard == null) {
            throw new ServiceException("分析卡片不存在，ID: " + card.getId());
        }
        
        int result = analysisCardMapper.update(card);
        if (result <= 0) {
            throw new ServiceException("更新分析卡片失败");
        }
        
        log.info("分析卡片更新成功，ID: {}", card.getId());
        return analysisCardMapper.selectById(card.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCard(String cardId) throws ServiceException {
        log.info("删除分析卡片: {}", cardId);
        
        if (StringUtils.isEmpty(cardId)) {
            throw new ServiceException("卡片ID不能为空");
        }
        
        // 检查卡片是否存在
        AnalysisCard card = analysisCardMapper.selectById(cardId);
        if (card == null) {
            throw new ServiceException("分析卡片不存在，ID: " + cardId);
        }
        
        // 检查是否有关联的任务
        if (!canDeleteCard(cardId)) {
            throw new ServiceException("该分析卡片存在关联任务，无法删除");
        }
        
        int result = analysisCardMapper.delete(cardId);
        if (result <= 0) {
            log.warn("分析卡片删除失败，ID: {}", cardId);
            return false;
        }
        
        log.info("分析卡片删除成功，ID: {}", cardId);
        return true;
    }

    @Override
    public AnalysisCard getCardById(String cardId) throws ServiceException {
        if (StringUtils.isEmpty(cardId)) {
            throw new ServiceException("卡片ID不能为空");
        }
        
        return analysisCardMapper.selectById(cardId);
    }

    @Override
    public AnalysisCard getCardByIdWithPermission(String cardId, boolean hidePrompt) throws ServiceException {
        AnalysisCard card = getCardById(cardId);
        
        if (card != null && hidePrompt) {
            // 隐藏提示词信息（非管理员用户）
            card.setPrompt(null);
        }
        
        return card;
    }

    @Override
    public PageInfo<AnalysisCard> getCardPage(int pageNum, int pageSize, Boolean enabled, String createdBy, String title) throws ServiceException {
        log.debug("分页查询分析卡片，页码: {}, 页面大小: {}", pageNum, pageSize);
        
        PageHelper.startPage(pageNum, pageSize);
        List<AnalysisCard> cards = analysisCardMapper.selectAll(enabled, createdBy, title);
        
        return new PageInfo<>(cards);
    }

    @Override
    public List<AnalysisCard> getEnabledCards() throws ServiceException {
        return analysisCardMapper.selectEnabled();
    }

    @Override
    public List<AnalysisCard> getCardsByCreatedBy(String createdBy) throws ServiceException {
        if (StringUtils.isEmpty(createdBy)) {
            throw new ServiceException("创建人不能为空");
        }
        
        return analysisCardMapper.selectByCreatedBy(createdBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableCard(String cardId, boolean enabled) throws ServiceException {
        log.info("{}分析卡片: {}", enabled ? "启用" : "禁用", cardId);
        
        if (StringUtils.isEmpty(cardId)) {
            throw new ServiceException("卡片ID不能为空");
        }
        
        // 检查卡片是否存在
        AnalysisCard card = analysisCardMapper.selectById(cardId);
        if (card == null) {
            throw new ServiceException("分析卡片不存在，ID: " + cardId);
        }
        
        card.setEnabled(enabled);
        int result = analysisCardMapper.update(card);
        
        if (result > 0) {
            log.info("分析卡片状态更新成功，ID: {}, 状态: {}", cardId, enabled);
            return true;
        }
        
        log.warn("分析卡片状态更新失败，ID: {}", cardId);
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchEnableCards(List<String> cardIds, boolean enabled) throws ServiceException {
        log.info("批量{}分析卡片，数量: {}", enabled ? "启用" : "禁用", cardIds.size());
        
        if (cardIds == null || cardIds.isEmpty()) {
            throw new ServiceException("卡片ID列表不能为空");
        }
        
        int result = analysisCardMapper.batchUpdateEnabled(cardIds, enabled);
        log.info("批量更新分析卡片状态完成，成功数量: {}", result);
        
        return result;
    }

    @Override
    public boolean canDeleteCard(String cardId) throws ServiceException {
        if (StringUtils.isEmpty(cardId)) {
            return false;
        }
        
        // 检查是否有关联的任务
        long taskCount = analysisTaskMapper.countByAnalysisCardId(cardId);
        return taskCount == 0;
    }

    @Override
    public long countCards(Boolean enabled, String createdBy) throws ServiceException {
        return analysisCardMapper.count(enabled, createdBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisCard copyCard(String cardId, String newTitle, String createdBy) throws ServiceException {
        log.info("复制分析卡片，源ID: {}, 新标题: {}", cardId, newTitle);
        
        if (StringUtils.isEmpty(cardId)) {
            throw new ServiceException("源卡片ID不能为空");
        }
        
        if (StringUtils.isEmpty(newTitle)) {
            throw new ServiceException("新标题不能为空");
        }
        
        if (StringUtils.isEmpty(createdBy)) {
            throw new ServiceException("创建人不能为空");
        }
        
        // 查询源卡片
        AnalysisCard sourceCard = analysisCardMapper.selectById(cardId);
        if (sourceCard == null) {
            throw new ServiceException("源分析卡片不存在，ID: " + cardId);
        }
        
        // 创建新卡片
        AnalysisCard newCard = new AnalysisCard();
        newCard.setId(UUID.randomUUID().toString().replace("-", ""));
        newCard.setTitle(newTitle);
        newCard.setDescription(sourceCard.getDescription());
        newCard.setIcon(sourceCard.getIcon());
        newCard.setTags(sourceCard.getTags());
        newCard.setEnabled(true); // 默认启用
        newCard.setPrompt(sourceCard.getPrompt());
        newCard.setModelType(sourceCard.getModelType());
        newCard.setAnalysisConfig(sourceCard.getAnalysisConfig());
        newCard.setCreatedBy(createdBy);
        newCard.setCreatedAt(LocalDateTime.now());
        newCard.setUpdatedAt(LocalDateTime.now());
        
        int result = analysisCardMapper.insert(newCard);
        if (result <= 0) {
            throw new ServiceException("复制分析卡片失败");
        }
        
        log.info("分析卡片复制成功，新ID: {}", newCard.getId());
        return newCard;
    }

    /**
     * 验证分析卡片数据
     * @param card 分析卡片
     * @param isCreate 是否为创建操作
     */
    private void validateCard(AnalysisCard card, boolean isCreate) throws ServiceException {
        if (card == null) {
            throw new ServiceException("分析卡片信息不能为空");
        }
        
        if (StringUtils.isEmpty(card.getTitle())) {
            throw new ServiceException("卡片标题不能为空");
        }
        
        if (card.getTitle().length() > 100) {
            throw new ServiceException("卡片标题长度不能超过100个字符");
        }
        
        if (StringUtils.isEmpty(card.getPrompt())) {
            throw new ServiceException("分析提示词不能为空");
        }
        
        if (isCreate && StringUtils.isEmpty(card.getCreatedBy())) {
            throw new ServiceException("创建人不能为空");
        }
        
        // 验证标签数量限制
        if (card.getTags() != null && card.getTags().size() > 10) {
            throw new ServiceException("标签数量不能超过10个");
        }
    }
}