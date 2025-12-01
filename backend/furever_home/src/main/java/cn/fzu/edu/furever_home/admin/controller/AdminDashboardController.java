package cn.fzu.edu.furever_home.admin.controller;

import cn.fzu.edu.furever_home.admin.dto.DashboardStatisticsDTO;
import cn.fzu.edu.furever_home.admin.service.AdminDashboardService;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "管理后台-工作台")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/statistics")
    @Operation(summary = "获取管理后台首页统计数据")
    public Result<DashboardStatisticsDTO> statistics() {
        DashboardStatisticsDTO statistics = adminDashboardService.getStatistics();
        return Result.success(statistics);
    }
}

