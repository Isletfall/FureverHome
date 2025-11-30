package cn.fzu.edu.furever_home.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import cn.fzu.edu.furever_home.common.enums.*;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;
    private String userName;
    private Integer userAge;
    private String passwordHash;
    private String email;
    private String avatarUrl;
    private Sex sex;
    private String location;
    private String proofText;
    private String proofPhoto;
    private Double creditScore;
    private Integer creditScoreCount;
    private UserStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}