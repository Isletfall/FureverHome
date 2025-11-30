package cn.fzu.edu.furever_home.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LivingEnvironment {
    DORM("宿舍"), APARTMENT("公寓"), VILLA("别墅"), OTHER("其他");

    @EnumValue
    @JsonValue
    private final String value;
    LivingEnvironment(String value){this.value=value;}
    public String getValue(){return value;}
}