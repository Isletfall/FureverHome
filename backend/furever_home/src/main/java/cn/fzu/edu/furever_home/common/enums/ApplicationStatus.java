package cn.fzu.edu.furever_home.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicationStatus {
    APPLYING("申请中"), SUCCESS("申请成功"), FAIL("申请失败");

    @EnumValue
    @JsonValue
    private final String value;
    ApplicationStatus(String value){this.value=value;}
    public String getValue(){return value;}
}