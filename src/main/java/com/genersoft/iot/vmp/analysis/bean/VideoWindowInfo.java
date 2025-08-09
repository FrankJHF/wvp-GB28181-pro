package com.genersoft.iot.vmp.analysis.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 视频窗口信息
 * @author Claude
 */
@Data
@Schema(description = "视频窗口信息")
public class VideoWindowInfo {

    @Schema(description = "视频窗口开始时间戳")
    private Double windowStartPts;

    @Schema(description = "视频窗口结束时间戳")
    private Double windowEndPts;

    @Schema(description = "视频窗口开始UTC时间")
    private String windowStartUtc;

    @Schema(description = "视频窗口结束UTC时间")
    private String windowEndUtc;

    @Schema(description = "窗口持续时长（秒）")
    private Double windowDurationSeconds;

    @Schema(description = "帧率")
    private Double frameRate;

    @Schema(description = "总帧数")
    private Long totalFrames;
}