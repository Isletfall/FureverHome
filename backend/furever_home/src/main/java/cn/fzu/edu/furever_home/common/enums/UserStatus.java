package cn.fzu.edu.furever_home.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserStatus {
    NORMAL("正常"), BANNED("禁用");

    @EnumValue
    @JsonValue
    private final String value;
    UserStatus(String value){this.value=value;}
    public String getValue(){return value;}
}