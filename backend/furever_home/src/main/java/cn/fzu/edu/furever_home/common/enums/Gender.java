package cn.fzu.edu.furever_home.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("公"), FEMALE("母"), UNKNOWN("未知");

    @EnumValue
    @JsonValue
    private final String value;
    Gender(String value){this.value=value;}
    public String getValue(){return value;}
}