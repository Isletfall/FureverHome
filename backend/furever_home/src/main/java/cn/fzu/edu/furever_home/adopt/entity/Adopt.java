package cn.fzu.edu.furever_home.adopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.fzu.edu.furever_home.common.enums.*;

import java.time.LocalDateTime;

@Data
@TableName("adopt")
public class Adopt {
    @TableId(value = "adopt_id", type = IdType.AUTO)
    private Integer adoptId;
    private Integer animalId;
    private Integer userId;
    private ApplicationStatus applicationStatus;
    private LivingEnvironment livingEnvironment;
    private HouseType houseType;
    private Boolean hasOtherPets;
    private Integer familyMemberCount;
    private Boolean hasChild;
    private String adoptReason;
    private Integer monthSalary;
    private LocalDateTime createTime;
    private LocalDateTime passTime;
    private ReviewStatus reviewStatus;
}