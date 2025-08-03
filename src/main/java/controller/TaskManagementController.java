package com.railse.hiring.workforcemgmt.controller;

import com.railse.hiring.workforcemgmt.common.model.response.Response;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task-mgmt")
public class TaskManagementController {

   private final TaskManagementService taskManagementService;

   public TaskManagementController(TaskManagementService taskManagementService) {
       this.taskManagementService = taskManagementService;
   }

   // Specific endpoints must come BEFORE parameterized endpoints
   @PostMapping("/create")
   public Response<List<TaskManagementDto>> createTasks(@RequestBody TaskCreateRequest request) {
       System.out.println("Received create task request: " + request);
       return new Response<>(taskManagementService.createTasks(request));
   }

   @PostMapping("/update")
   public Response<List<TaskManagementDto>> updateTasks(@RequestBody UpdateTaskRequest request) {
       return new Response<>(taskManagementService.updateTasks(request));
   }

   @PostMapping("/assign-by-ref")
   public Response<String> assignByReference(@RequestBody AssignByReferenceRequest request) {
       return new Response<>(taskManagementService.assignByReference(request));
   }

   @PostMapping("/fetch-by-date/v2")
   public Response<List<TaskManagementDto>> fetchByDate(@RequestBody TaskFetchByDateRequest request) {
       return new Response<>(taskManagementService.fetchTasksByDate(request));
   }

   // New priority management endpoints
   @PostMapping("/{taskId}/priority")
   public Response<TaskManagementDto> updateTaskPriority(
           @PathVariable Long taskId,
           @RequestBody UpdatePriorityRequest request) {
       return new Response<>(taskManagementService.updateTaskPriority(taskId, request.getPriority()));
   }

   @GetMapping("/priority/{priority}")
   public Response<List<TaskManagementDto>> getTasksByPriority(@PathVariable String priority) {
       com.railse.hiring.workforcemgmt.model.enums.Priority priorityEnum = 
           com.railse.hiring.workforcemgmt.model.enums.Priority.valueOf(priority.toUpperCase());
       return new Response<>(taskManagementService.getTasksByPriority(priorityEnum));
   }

   // New comment endpoint
   @PostMapping("/{taskId}/comment")
   public Response<TaskManagementDto> addComment(
           @PathVariable Long taskId,
           @RequestBody AddCommentRequest request) {
       return new Response<>(taskManagementService.addComment(taskId, request));
   }

   // Parameterized endpoint must come LAST
   @GetMapping("/{id}")
   public Response<TaskManagementDto> getTaskById(@PathVariable Long id) {
       return new Response<>(taskManagementService.findTaskById(id));
   }
}