package cn.fzu.edu.furever_home.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReviewStatus {
    PENDING("待审核"),
    APPROVED("通过"),
    REJECTED("拒绝");

    @EnumValue
    @JsonValue
    private final String value;

    ReviewStatus(String value) { this.value = value; }
    public String getValue() { return value; }
}