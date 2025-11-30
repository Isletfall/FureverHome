package cn.fzu.edu.furever_home.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Species {
    DOG("狗"), CAT("猫"), RABBIT("兔子"), HAMSTER("仓鼠"), BIRD("鸟类"), FISH("鱼类"), TURTLE("龟类"), OTHER("其他");

    @EnumValue
    @JsonValue
    private final String value;
    Species(String value){this.value=value;}
    public String getValue(){return value;}
}