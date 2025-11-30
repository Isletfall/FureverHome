package cn.fzu.edu.furever_home.animal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.fzu.edu.furever_home.common.enums.*;

import java.time.LocalDateTime;

@Data
@TableName("animal")
public class Animal {
    @TableId(value = "animal_id", type = IdType.AUTO)
    private Integer animalId;
    private Integer userId;
    private String animalName;
    private String photoUrls;
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