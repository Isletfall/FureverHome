package cn.fzu.edu.furever_home.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SterilizedStatus {
    YES("是"), NO("否"), UNKNOWN("未知");

    @EnumValue
    @JsonValue
    private final String value;
    SterilizedStatus(String value){this.value=value;}
    public String getValue(){return value;}
}