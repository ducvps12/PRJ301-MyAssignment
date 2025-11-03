// src/main/java/com/acme/leavemgmt/model/SysSetting.java
package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class SysSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String settingKey;
    private String settingValue;
    private String dataType;    // string|int|bool|json|mail...
    private String groupName;   // System|Mail|Leave|Security...
    private String description;
    private boolean active;
    private Integer updatedBy;  // user id, có thể null
    private LocalDateTime updatedAt;

    public SysSetting() {}

    public SysSetting(String settingKey, String settingValue) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    // ===== Getters / Setters =====
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ===== Convenience parsers for settingValue =====
    public int asInt(int def) {
        if (settingValue == null) return def;
        try { return Integer.parseInt(settingValue.trim()); }
        catch (NumberFormatException ex) { return def; }
    }

    public long asLong(long def) {
        if (settingValue == null) return def;
        try { return Long.parseLong(settingValue.trim()); }
        catch (NumberFormatException ex) { return def; }
    }

    public double asDouble(double def) {
        if (settingValue == null) return def;
        try { return Double.parseDouble(settingValue.trim()); }
        catch (NumberFormatException ex) { return def; }
    }

    public boolean asBool(boolean def) {
        if (settingValue == null) return def;
        String v = settingValue.trim().toLowerCase();
        if (v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("on")) return true;
        if (v.equals("false") || v.equals("0") || v.equals("no") || v.equals("off")) return false;
        return def;
    }

    public String[] asCsv() {
        return settingValue == null ? new String[0] : settingValue.split("\\s*,\\s*");
    }

    // ===== equals/hashCode theo settingKey để dùng làm key map() =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SysSetting)) return false;
        SysSetting that = (SysSetting) o;
        return Objects.equals(settingKey, that.settingKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settingKey);
    }

    @Override
    public String toString() {
        return "SysSetting{" +
                "id=" + id +
                ", settingKey='" + settingKey + '\'' +
                ", settingValue='" + settingValue + '\'' +
                ", dataType='" + dataType + '\'' +
                ", groupName='" + groupName + '\'' +
                ", active=" + active +
                ", updatedBy=" + updatedBy +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
