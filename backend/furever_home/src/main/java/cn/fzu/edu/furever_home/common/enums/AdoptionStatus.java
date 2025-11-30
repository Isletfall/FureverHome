package cn.fzu.edu.furever_home.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AdoptionStatus {
    SHORT_TERM("短期领养"), LONG_TERM("长期领养");

    @EnumValue
    @JsonValue
    private final String value;
    AdoptionStatus(String value){this.value=value;}
    public String getValue(){return value;}
}