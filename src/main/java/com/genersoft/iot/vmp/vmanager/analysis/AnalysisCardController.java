package com.genersoft.iot.vmp.vmanager.analysis;

import com.genersoft.iot.vmp.analysis.bean.AnalysisCard;
import com.genersoft.iot.vmp.analysis.service.IAnalysisCardService;
import com.genersoft.iot.vmp.conf.exception.ControllerException;
import com.genersoft.iot.vmp.conf.security.JwtUtils;
import com.genersoft.iot.vmp.conf.security.SecurityUtils;
import com.genersoft.iot.vmp.vmanager.bean.ErrorCode;
import com.genersoft.iot.vmp.vmanager.bean.WVPResult;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

/**
 * 分析卡片管理控制器
 * @author Claude
 */
@Tag(name = "智能分析 - 分析卡片管理")
@RestController
@RequestMapping("/api/vmanager/analysis/cards")
@Slf4j
public class AnalysisCardController {

    @Autowired
    private IAnalysisCardService analysisCardService;

    /**
     * 分页查询分析卡片
     */
    @GetMapping
    @Operation(summary = "分页查询分析卡片", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<PageInfo<AnalysisCard>> getCards(
            @Parameter(name = "page", description = "页码，默认1") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(name = "count", description = "每页数量，默认20") 
            @RequestParam(defaultValue = "20") int count,
            @Parameter(name = "title", description = "卡片标题关键词") 
            @RequestParam(required = false) String title,
            @Parameter(name = "enabled", description = "是否启用") 
            @RequestParam(required = false) Boolean enabled,
            @Parameter(name = "modelType", description = "模型类型") 
            @RequestParam(required = false) String modelType,
            @Parameter(name = "createdBy", description = "创建人") 
            @RequestParam(required = false) String createdBy) {
        
        try {
            PageInfo<AnalysisCard> pageResult = analysisCardService.getCardPage(
                    page, count, enabled, createdBy, title);
            
            // 根据用户权限过滤敏感信息
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            boolean isAdmin = SecurityUtils.getUserInfo() != null; // 简化为只检查是否已登录
            
            if (!isAdmin) {
                // 非管理员用户隐藏 prompt字段
                pageResult.getList().forEach(card -> card.setPrompt(""));
            }
            
            return WVPResult.success(pageResult, "查询成功");
        } catch (Exception e) {
            log.error("查询分析卡片失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "查询分析卡片失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询分析卡片
     */
    @GetMapping("/{cardId}")
    @Operation(summary = "根据ID查询分析卡片", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<AnalysisCard> getCard(
            @Parameter(name = "cardId", description = "卡片ID", required = true) 
            @PathVariable String cardId) {
        
        if (StringUtils.isEmpty(cardId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "卡片ID不能为空");
        }
        
        try {
            AnalysisCard card = analysisCardService.getCardById(cardId);
            if (card == null) {
                throw new ControllerException(ErrorCode.ERROR404.getCode(), "分析卡片不存在");
            }
            
            // 根据用户权限过滤敏感信息
            boolean isAdmin = SecurityUtils.getUserInfo() != null; // 简化为只检查是否已登录
            if (!isAdmin) {
                card.setPrompt("");
            }
            
            return WVPResult.success(card, "查询成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询分析卡片失败，ID: {}", cardId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "查询分析卡片失败: " + e.getMessage());
        }
    }

    /**
     * 创建分析卡片
     */
    @PostMapping
    @Operation(summary = "创建分析卡片", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<AnalysisCard> createCard(@RequestBody AnalysisCard card) {
        
        // 验证权限 - 只有已登录用户可以创建卡片
        if (SecurityUtils.getUserInfo() == null) {
            throw new ControllerException(ErrorCode.ERROR403.getCode(), "用户未登录");
        }
        
        try {
            // 设置创建人
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            card.setCreatedBy(currentUser);
            
            AnalysisCard result = analysisCardService.createCard(card);
            
            log.info("用户 {} 创建了分析卡片: {}", currentUser, result.getTitle());
            
            return WVPResult.success(result, "创建成功");
        } catch (Exception e) {
            log.error("创建分析卡片失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "创建分析卡片失败: " + e.getMessage());
        }
    }

    /**
     * 更新分析卡片
     */
    @PutMapping("/{cardId}")
    @Operation(summary = "更新分析卡片", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<AnalysisCard> updateCard(
            @Parameter(name = "cardId", description = "卡片ID", required = true) 
            @PathVariable String cardId,
            @RequestBody AnalysisCard card) {
        
        if (StringUtils.isEmpty(cardId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "卡片ID不能为空");
        }
        
        // 验证权限 - 只有已登录用户可以更新卡片
        if (SecurityUtils.getUserInfo() == null) {
            throw new ControllerException(ErrorCode.ERROR403.getCode(), "用户未登录");
        }
        
        try {
            // 检查卡片是否存在
            AnalysisCard existingCard = analysisCardService.getCardById(cardId);
            if (existingCard == null) {
                throw new ControllerException(ErrorCode.ERROR404.getCode(), "分析卡片不存在");
            }
            
            // 设置ID和更新人
            card.setId(cardId);
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            card.setUpdatedBy(currentUser);
            
            AnalysisCard result = analysisCardService.updateCard(card);
            
            log.info("用户 {} 更新了分析卡片: {}", currentUser, result.getTitle());
            
            return WVPResult.success(result, "更新成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新分析卡片失败，ID: {}", cardId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "更新分析卡片失败: " + e.getMessage());
        }
    }

    /**
     * 删除分析卡片
     */
    @DeleteMapping("/{cardId}")
    @Operation(summary = "删除分析卡片", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Void> deleteCard(
            @Parameter(name = "cardId", description = "卡片ID", required = true) 
            @PathVariable String cardId) {
        
        if (StringUtils.isEmpty(cardId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "卡片ID不能为空");
        }
        
        // 验证权限 - 只有已登录用户可以删除卡片
        if (SecurityUtils.getUserInfo() == null) {
            throw new ControllerException(ErrorCode.ERROR403.getCode(), "用户未登录");
        }
        
        try {
            // 检查卡片是否存在
            AnalysisCard existingCard = analysisCardService.getCardById(cardId);
            if (existingCard == null) {
                throw new ControllerException(ErrorCode.ERROR404.getCode(), "分析卡片不存在");
            }
            
            boolean success = analysisCardService.deleteCard(cardId);
            if (!success) {
                throw new ControllerException(ErrorCode.ERROR500.getCode(), "删除分析卡片失败");
            }
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} 删除了分析卡片: {}", currentUser, existingCard.getTitle());
            
            return WVPResult.success(null, "删除成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除分析卡片失败，ID: {}", cardId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "删除分析卡片失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用分析卡片
     */
    @PostMapping("/{cardId}/toggle")
    @Operation(summary = "启用/禁用分析卡片", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Void> toggleCard(
            @Parameter(name = "cardId", description = "卡片ID", required = true) 
            @PathVariable String cardId,
            @Parameter(name = "enabled", description = "是否启用", required = true) 
            @RequestParam boolean enabled) {
        
        if (StringUtils.isEmpty(cardId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "卡片ID不能为空");
        }
        
        // 验证权限 - 只有已登录用户可以操作卡片
        if (SecurityUtils.getUserInfo() == null) {
            throw new ControllerException(ErrorCode.ERROR403.getCode(), "用户未登录");
        }
        
        try {
            boolean success = analysisCardService.enableCard(cardId, enabled);
            if (!success) {
                throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                        enabled ? "启用分析卡片失败" : "禁用分析卡片失败");
            }
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} {} 了分析卡片: {}", currentUser, enabled ? "启用" : "禁用", cardId);
            
            return WVPResult.success(null, enabled ? "启用成功" : "禁用成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("{}分析卡片失败，ID: {}", enabled ? "启用" : "禁用", cardId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    (enabled ? "启用" : "禁用") + "分析卡片失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用的分析卡片列表（供任务创建时选择）
     */
    @GetMapping("/available")
    @Operation(summary = "获取可用的分析卡片列表", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<PageInfo<AnalysisCard>> getAvailableCards(
            @Parameter(name = "page", description = "页码，默认1") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(name = "count", description = "每页数量，默认50") 
            @RequestParam(defaultValue = "50") int count) {
        
        try {
            // 只查询启用的卡片
            PageInfo<AnalysisCard> pageResult = analysisCardService.getCardPage(
                    page, count, true, null, null);
            
            // 对所有用户隐藏敏感信息
            pageResult.getList().forEach(card -> {
                card.setPrompt("");
                card.setAnalysisConfig(null);
            });
            
            return WVPResult.success(pageResult, "查询成功");
        } catch (Exception e) {
            log.error("查询可用分析卡片失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "查询可用分析卡片失败: " + e.getMessage());
        }
    }

    /**
     * 统计分析卡片数量
     */
    @GetMapping("/count")
    @Operation(summary = "统计分析卡片数量", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Long> countCards(
            @Parameter(name = "enabled", description = "是否启用") 
            @RequestParam(required = false) Boolean enabled,
            @Parameter(name = "createdBy", description = "创建人") 
            @RequestParam(required = false) String createdBy) {
        
        try {
            long count = analysisCardService.countCards(enabled, createdBy);
            return WVPResult.success(count, "统计成功");
        } catch (Exception e) {
            log.error("统计分析卡片数量失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "统计分析卡片数量失败: " + e.getMessage());
        }
    }
}