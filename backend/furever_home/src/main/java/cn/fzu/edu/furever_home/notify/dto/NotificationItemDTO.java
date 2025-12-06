package cn.fzu.edu.furever_home.notify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "通知条目（含目标详情）")
public class NotificationItemDTO {
    @Schema(description = "通知ID")
    private Integer activityId;
    @Schema(description = "是否已读")
    private Boolean isRead;
    @Schema(description = "事件")
    private String event;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "目标类型：post/adopt/animal")
    private String targetType;
    @Schema(description = "目标ID")
    private Integer targetId;

    @Schema(description = "触发者用户ID")
    private Integer actorUserId;
    @Schema(description = "触发者昵称")
    private String actorUserName;
    @Schema(description = "触发者头像")
    private String actorUserAvatar;

    // 帖子详情
    @Schema(description = "帖子标题")
    private String postTitle;
    @Schema(description = "帖子作者ID")
    private Integer postAuthorId;
    @Schema(description = "帖子作者昵称")
    private String postAuthorName;
    @Schema(description = "帖子作者头像")
    private String postAuthorAvatar;

    // 领养申请详情
    @Schema(description = "领养申请状态")
    private String adoptApplicationStatus;
    @Schema(description = "领养审核状态")
    private String adoptReviewStatus;
    @Schema(description = "申请人ID")
    private Integer applicantUserId;
    @Schema(description = "申请人昵称")
    private String applicantUserName;
    @Schema(description = "申请人头像")
    private String applicantUserAvatar;
    @Schema(description = "宠物ID")
    private Integer animalId;
    @Schema(description = "宠物名称")
    private String animalName;
    @Schema(description = "宠物封面图")
    private String animalPhoto;

    // 宠物详情
    @Schema(description = "宠物物种")
    private String animalSpecies;
    @Schema(description = "宠物图片列表")
    private List<String> animalPhotoUrls;
}
