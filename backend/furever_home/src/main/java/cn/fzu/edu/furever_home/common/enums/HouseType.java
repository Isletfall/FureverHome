package cn.fzu.edu.furever_home.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HouseType {
    OWN("拥有"), RENT("租用");

    @EnumValue
    @JsonValue
    private final String value;
    HouseType(String value){this.value=value;}
    public String getValue(){return value;}
}