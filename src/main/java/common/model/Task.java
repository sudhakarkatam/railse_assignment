package com.railse.hiring.workforcemgmt.common.model;


import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private String id;
    private String title;
    private String description;
    private String assignee;
    private LocalDate startDate;
    private LocalDate endDate;
    private TaskStatus status;
}
