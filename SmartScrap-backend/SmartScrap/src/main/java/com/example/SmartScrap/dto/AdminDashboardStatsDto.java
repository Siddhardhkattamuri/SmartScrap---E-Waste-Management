package com.example.SmartScrap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDashboardStatsDto {
    private long totalUsers;
    private long totalRequests;
    private long pendingRequests;
    private long completedRequests;
}