package cn.fzu.edu.furever_home.animal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import cn.fzu.edu.furever_home.common.enums.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "animal", autoResultMap = true)
public class Animal {
    @TableId(value = "animal_id", type = IdType.AUTO)
    private Integer animalId;
    private Integer userId;
    private String animalName;
    @TableField(value = "photo_urls", typeHandler = JacksonTypeHandler.class)
    private List<String> photoUrls;
    private Species species;
    private String breed;
    private Gender gender;
    private Integer animalAge;
    private String healthStatus;
    private SterilizedStatus isSterilized;
    private AdoptionStatus adoptionStatus;
    private String shortDescription;
    private ReviewStatus reviewStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}