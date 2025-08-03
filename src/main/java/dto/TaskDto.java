package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskDto {
    private String title;
    private String description;
    private String assignee;
    private LocalDate startDate;
    private LocalDate endDate;
}
