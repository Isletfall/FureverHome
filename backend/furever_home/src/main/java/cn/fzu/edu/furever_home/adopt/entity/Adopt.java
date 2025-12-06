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
    private String userName;
    private String phone;
    private String email;
    private String province;
    private String city;
    private String livingLocation;
    private String adoptReason;
    private LocalDateTime createTime;
    private LocalDateTime passTime;
    private ReviewStatus reviewStatus;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private Boolean isCancelled;
    private LocalDateTime cancelledAt;
}
