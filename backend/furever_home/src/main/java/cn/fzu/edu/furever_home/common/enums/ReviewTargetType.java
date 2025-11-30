package cn.fzu.edu.furever_home.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReviewTargetType {
    ANIMAL("animal"),
    POST("post"),
    ADOPT("adopt");

    @EnumValue
    @JsonValue
    private final String value;
    ReviewTargetType(String value){this.value=value;}
    public String getValue(){return value;}
}