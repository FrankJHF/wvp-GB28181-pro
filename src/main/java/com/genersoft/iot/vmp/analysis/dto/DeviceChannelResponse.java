package com.genersoft.iot.vmp.analysis.dto;

import java.util.List;

/**
 * 设备通道响应DTO
 */
public class DeviceChannelResponse {

    /**
     * 设备信息
     */
    private DeviceInfo device;

    /**
     * 通道列表
     */
    private List<ChannelInfo> channels;

    public DeviceChannelResponse() {}

    public DeviceChannelResponse(DeviceInfo device, List<ChannelInfo> channels) {
        this.device = device;
        this.channels = channels;
    }

    // Getters and Setters
    public DeviceInfo getDevice() {
        return device;
    }

    public void setDevice(DeviceInfo device) {
        this.device = device;
    }

    public List<ChannelInfo> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelInfo> channels) {
        this.channels = channels;
    }

    /**
     * 设备信息内部类
     */
    public static class DeviceInfo {
        /**
         * 设备ID
         */
        private String deviceId;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 设备状态 (ON/OFF)
         */
        private String status;

        /**
         * 最后在线时间
         */
        private String lastOnlineTime;

        /**
         * 设备类型
         */
        private String deviceType;

        public DeviceInfo() {}

        public DeviceInfo(String deviceId, String deviceName, String status) {
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.status = status;
        }

        // Getters and Setters
        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLastOnlineTime() {
            return lastOnlineTime;
        }

        public void setLastOnlineTime(String lastOnlineTime) {
            this.lastOnlineTime = lastOnlineTime;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        @Override
        public String toString() {
            return "DeviceInfo{" +
                    "deviceId='" + deviceId + '\'' +
                    ", deviceName='" + deviceName + '\'' +
                    ", status='" + status + '\'' +
                    ", lastOnlineTime='" + lastOnlineTime + '\'' +
                    ", deviceType='" + deviceType + '\'' +
                    '}';
        }
    }

    /**
     * 通道信息内部类
     */
    public static class ChannelInfo {
        /**
         * 通道ID
         */
        private String channelId;

        /**
         * 通道名称
         */
        private String channelName;

        /**
         * 通道状态 (ON/OFF)
         */
        private String status;

        /**
         * 支持的协议
         */
        private List<String> supportedProtocols;

        /**
         * 是否支持分析
         */
        private Boolean analysisSupported = true;

        public ChannelInfo() {}

        public ChannelInfo(String channelId, String channelName, String status) {
            this.channelId = channelId;
            this.channelName = channelName;
            this.status = status;
        }

        // Getters and Setters
        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        public String getChannelName() {
            return channelName;
        }

        public void setChannelName(String channelName) {
            this.channelName = channelName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<String> getSupportedProtocols() {
            return supportedProtocols;
        }

        public void setSupportedProtocols(List<String> supportedProtocols) {
            this.supportedProtocols = supportedProtocols;
        }

        public Boolean getAnalysisSupported() {
            return analysisSupported;
        }

        public void setAnalysisSupported(Boolean analysisSupported) {
            this.analysisSupported = analysisSupported;
        }

        @Override
        public String toString() {
            return "ChannelInfo{" +
                    "channelId='" + channelId + '\'' +
                    ", channelName='" + channelName + '\'' +
                    ", status='" + status + '\'' +
                    ", supportedProtocols=" + supportedProtocols +
                    ", analysisSupported=" + analysisSupported +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "DeviceChannelResponse{" +
                "device=" + device +
                ", channels=" + channels +
                '}';
    }
}