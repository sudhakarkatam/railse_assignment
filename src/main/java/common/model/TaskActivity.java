package com.railse.hiring.workforcemgmt.common.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskActivity {
    private Long id;
    private Long taskId;
    private String activityType; // CREATED, UPDATED, PRIORITY_CHANGED, STATUS_CHANGED, COMMENT_ADDED
    private String description;
    private Long userId;
    private LocalDateTime timestamp;
    private String comment; // For comment activities
}