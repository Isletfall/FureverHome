package cn.fzu.edu.furever_home.admin.service;

import cn.fzu.edu.furever_home.admin.dto.DashboardStatisticsDTO;

public interface AdminDashboardService {

    /**
     * 获取管理后台首页所需的统计数据。
     */
    DashboardStatisticsDTO getStatistics();
}

