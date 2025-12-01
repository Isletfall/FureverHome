package cn.fzu.edu.furever_home.rating.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.common.result.Result;
import cn.fzu.edu.furever_home.rating.dto.MyRatingDTO;
import cn.fzu.edu.furever_home.rating.dto.ReceivedRatingItemDTO;
import cn.fzu.edu.furever_home.rating.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rating")
@Tag(name = "评价", description = "用户评价相关接口")
public class RatingController {
    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/received")
    @SaCheckLogin
    @Operation(summary = "获取我的评价（他人对我的评价）列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<PageResult<ReceivedRatingItemDTO>> received(@RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "20") int pageSize) {
        Integer uid = StpUtil.getLoginIdAsInt();
        PageResult<ReceivedRatingItemDTO> data = ratingService.pageReceived(uid, page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/mine/{targetUserId}")
    @SaCheckLogin
    @Operation(summary = "获取我对某个用户的评价")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<MyRatingDTO> myRating(@PathVariable Integer targetUserId) {
        Integer uid = StpUtil.getLoginIdAsInt();
        MyRatingDTO data = ratingService.getMyRatingFor(uid, targetUserId);
        return Result.success(data);
    }

    @PostMapping("/mine/{targetUserId}")
    @SaCheckLogin
    @Operation(summary = "添加我对某个用户的评价")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<Void> addMyRating(@PathVariable Integer targetUserId,
                                    @RequestParam Integer score,
                                    @RequestParam String content) {
        Integer uid = StpUtil.getLoginIdAsInt();
        ratingService.addMyRating(uid, targetUserId, score, content);
        return Result.success();
    }

    @PutMapping("/mine/{targetUserId}")
    @SaCheckLogin
    @Operation(summary = "修改我对某个用户的评价")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<Void> updateMyRating(@PathVariable Integer targetUserId,
                                       @RequestParam Integer ratingId,
                                       @RequestParam Integer score,
                                       @RequestParam String content) {
        Integer uid = StpUtil.getLoginIdAsInt();
        ratingService.updateMyRating(uid, targetUserId, ratingId, score, content);
        return Result.success();
    }
}