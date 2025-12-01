package cn.fzu.edu.furever_home.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Sex {
    MALE("男"), FEMALE("女"), SECRET("保密");

    @EnumValue
    @JsonValue
    private final String value;
    Sex(String value){this.value=value;}
    public String getValue(){return value;}
}