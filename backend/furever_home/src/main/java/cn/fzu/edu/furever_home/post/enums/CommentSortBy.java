package cn.fzu.edu.furever_home.post.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "评论排序字段")
public enum CommentSortBy {
    TIME,
    LIKES
}