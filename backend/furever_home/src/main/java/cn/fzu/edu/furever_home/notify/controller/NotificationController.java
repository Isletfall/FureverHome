package cn.fzu.edu.furever_home.notify.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.common.result.Result;
import cn.fzu.edu.furever_home.notify.dto.NotificationItemDTO;
import cn.fzu.edu.furever_home.notify.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
@Tag(name = "通知", description = "通知列表与查看")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/list")
    @Operation(summary = "查看我的通知列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<PageResult<NotificationItemDTO>> list(@RequestParam(defaultValue = "false") boolean onlyUnread,
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Integer uid = StpUtil.getLoginIdAsInt();
        PageResult<NotificationItemDTO> data = notificationService.pageMyActivities(uid, onlyUnread, targetType, page,
                pageSize);
        return Result.success(data);
    }

    @PutMapping("/read/{id}")
    @Operation(summary = "标记单条通知为已读")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<Void> markRead(@PathVariable("id") Integer id) {
        Integer uid = StpUtil.getLoginIdAsInt();
        notificationService.markRead(uid, id);
        return Result.success();
    }

    @PutMapping("/read/all")
    @Operation(summary = "标记所有未读通知为已读")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<Void> markAllRead() {
        Integer uid = StpUtil.getLoginIdAsInt();
        notificationService.markAllRead(uid);
        return Result.success();
    }
}
