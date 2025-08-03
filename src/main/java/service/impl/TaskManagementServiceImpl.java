package com.railse.hiring.workforcemgmt.service.impl;

import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.common.model.TaskManagement;
import com.railse.hiring.workforcemgmt.common.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class TaskManagementServiceImpl implements TaskManagementService {

   private final TaskRepository taskRepository;
   private final ITaskManagementMapper taskMapper;

   public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper) {
       this.taskRepository = taskRepository;
       this.taskMapper = taskMapper;
   }

   @Override
   public TaskManagementDto findTaskById(Long id) {
       TaskManagement task = taskRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
       return taskMapper.modelToDto(task);
   }

   @Override
   public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
       if (createRequest == null || createRequest.getRequests() == null || createRequest.getRequests().isEmpty()) {
           throw new IllegalArgumentException("Task creation request cannot be null or empty");
       }
       
       List<TaskManagement> createdTasks = new ArrayList<>();
       for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
           if (item == null) {
               continue; // Skip null items
           }
           
           TaskManagement newTask = new TaskManagement();
           newTask.setReferenceId(item.getReferenceId());
           newTask.setReferenceType(item.getReferenceType());
           newTask.setTask(item.getTask());
           newTask.setAssigneeId(item.getAssigneeId());
           newTask.setPriority(item.getPriority());
           newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
           newTask.setStatus(TaskStatus.ASSIGNED);
           newTask.setDescription("New task created.");
           newTask.setCreatedAt(LocalDateTime.now());
           
           // Save the task first to get the ID
           TaskManagement savedTask = taskRepository.save(newTask);
           
           // Add activity for task creation after saving
           addActivityAfterSave(savedTask, "CREATED", "Task created", item.getAssigneeId());
           
           // Save again to persist the activity
           createdTasks.add(taskRepository.save(savedTask));
       }
       return taskMapper.modelListToDtoList(createdTasks);
   }

   // Update the updateTasks method to handle null assigneeId

@Override
public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
    if (updateRequest == null || updateRequest.getRequests() == null || updateRequest.getRequests().isEmpty()) {
        throw new IllegalArgumentException("Task update request cannot be null or empty");
    }
    
    List<TaskManagement> updatedTasks = new ArrayList<>();
    for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
        if (item == null) {
            continue; // Skip null items
        }
        
        TaskManagement task = taskRepository.findById(item.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));

        if (item.getTaskStatus() != null) {
            task.setStatus(item.getTaskStatus());
            // Use task's assigneeId if item's assigneeId is null
            Long userId = item.getAssigneeId() != null ? item.getAssigneeId() : task.getAssigneeId();
            addActivity(task, "STATUS_CHANGED", "Status changed to " + item.getTaskStatus(), userId);
        }
        if (item.getDescription() != null) {
            task.setDescription(item.getDescription());
            // Use task's assigneeId if item's assigneeId is null
            Long userId = item.getAssigneeId() != null ? item.getAssigneeId() : task.getAssigneeId();
            addActivity(task, "UPDATED", "Description updated", userId);
        }
        updatedTasks.add(taskRepository.save(task));
    }
    return taskMapper.modelListToDtoList(updatedTasks);
}

   @Override
   public String assignByReference(AssignByReferenceRequest request) {
       List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
       List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(request.getReferenceId(), request.getReferenceType());

       for (Task taskType : applicableTasks) {
           List<TaskManagement> tasksOfType = existingTasks.stream()
                   .filter(t -> t.getTask() == taskType && t.getStatus() != TaskStatus.COMPLETED)
                   .collect(Collectors.toList());

           if (!tasksOfType.isEmpty()) {
               // Assign the first task
               TaskManagement firstTask = tasksOfType.get(0);
               firstTask.setAssigneeId(request.getAssigneeId());
               firstTask.setStatus(TaskStatus.ASSIGNED);
               firstTask.setCreatedAt(LocalDateTime.now());
               addActivity(firstTask, "ASSIGNED", "Task assigned to user " + request.getAssigneeId(), request.getAssigneeId());
               taskRepository.save(firstTask);

               // Cancel the rest
               for (int i = 1; i < tasksOfType.size(); i++) {
                   TaskManagement cancelledTask = tasksOfType.get(i);
                   cancelledTask.setStatus(TaskStatus.CANCELLED);
                   cancelledTask.setAssigneeId(null);
                   addActivity(cancelledTask, "CANCELLED", "Task cancelled due to reassignment", request.getAssigneeId());
                   taskRepository.save(cancelledTask);
               }

           } else {
               // create a new task if no existing tasks are found
               TaskManagement newTask = new TaskManagement();
               newTask.setReferenceId(request.getReferenceId());
               newTask.setReferenceType(request.getReferenceType());
               newTask.setTask(taskType);
               newTask.setAssigneeId(request.getAssigneeId());
               newTask.setStatus(TaskStatus.ASSIGNED);
               newTask.setCreatedAt(LocalDateTime.now());
               
               // Save the task first to get the ID
               TaskManagement savedTask = taskRepository.save(newTask);
               
               // Add activity for task creation after saving
               addActivityAfterSave(savedTask, "CREATED", "Task created and assigned", request.getAssigneeId());
               
               // Save again to persist the activity
               taskRepository.save(savedTask);
           }
       }

       return "Tasks assigned successfully for reference " + request.getReferenceId();
   }

   @Override
   public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
       LocalDate startDate = request.getStartDate();
       LocalDate endDate = request.getEndDate();

       List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());

       List<TaskManagement> filteredTasks = tasks.stream()
           .filter(task -> task.getStatus() != TaskStatus.CANCELLED)
           .filter(task -> {
               if (task.getCreatedAt() == null) {
                   return false; // Skip tasks without createdAt
               }
               
               LocalDate taskCreatedDate = task.getCreatedAt().toLocalDate();
               
               // Smart daily task view logic
               if (startDate != null && endDate != null) {
                   // Case 1: Task was created within the date range
                   boolean createdInRange = !taskCreatedDate.isBefore(startDate) 
                       && !taskCreatedDate.isAfter(endDate);
                   
                   // Case 2: Task was created before the range but is still active and not completed
                   boolean createdBeforeButStillActive = taskCreatedDate.isBefore(startDate) 
                       && task.getStatus() == TaskStatus.ASSIGNED;
                   
                   return createdInRange || createdBeforeButStillActive;
                   
               } else if (startDate != null) {
                   // Only start date provided - include tasks created on or after start date
                   // PLUS tasks created before but still active
                   boolean createdOnOrAfterStart = !taskCreatedDate.isBefore(startDate);
                   boolean createdBeforeButStillActive = taskCreatedDate.isBefore(startDate) 
                       && task.getStatus() == TaskStatus.ASSIGNED;
                   
                   return createdOnOrAfterStart || createdBeforeButStillActive;
                   
               } else if (endDate != null) {
                   // Only end date provided - include tasks created on or before end date
                   // PLUS tasks created before but still active
                   boolean createdOnOrBeforeEnd = !taskCreatedDate.isAfter(endDate);
                   boolean createdBeforeButStillActive = taskCreatedDate.isBefore(endDate) 
                       && task.getStatus() == TaskStatus.ASSIGNED;
                   
                   return createdOnOrBeforeEnd || createdBeforeButStillActive;
                   
               } else {
                   // No date range provided - return all active tasks
                   return task.getStatus() == TaskStatus.ASSIGNED;
               }
           })
           .collect(Collectors.toList());

       return taskMapper.modelListToDtoList(filteredTasks);
   }

   // New priority management methods
   @Override
   public TaskManagementDto updateTaskPriority(Long taskId, Priority priority) {
       TaskManagement task = taskRepository.findById(taskId)
               .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
       
       Priority oldPriority = task.getPriority();
       task.setPriority(priority);
       addActivity(task, "PRIORITY_CHANGED", "Priority changed from " + oldPriority + " to " + priority, task.getAssigneeId());
       
       TaskManagement savedTask = taskRepository.save(task);
       return taskMapper.modelToDto(savedTask);
   }

   @Override
   public List<TaskManagementDto> getTasksByPriority(Priority priority) {
       List<TaskManagement> tasks = taskRepository.findAll().stream()
               .filter(task -> priority.equals(task.getPriority()))
               .collect(Collectors.toList());
       
       return taskMapper.modelListToDtoList(tasks);
   }

   // New comment method
   @Override
   public TaskManagementDto addComment(Long taskId, AddCommentRequest request) {
       TaskManagement task = taskRepository.findById(taskId)
               .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
       
       addActivity(task, "COMMENT_ADDED", "Comment added by user " + request.getUserId(), request.getUserId());
       
       // Add the comment to the last activity
       TaskActivity lastActivity = task.getActivities().get(task.getActivities().size() - 1);
       lastActivity.setComment(request.getComment());
       
       TaskManagement savedTask = taskRepository.save(task);
       return taskMapper.modelToDto(savedTask);
   }

   // Helper method for adding activities
   private void addActivity(TaskManagement task, String activityType, String description, Long userId) {
       TaskActivity activity = new TaskActivity();
       activity.setTaskId(task.getId());
       activity.setActivityType(activityType);
       activity.setDescription(description);
       activity.setUserId(userId);
       activity.setTimestamp(LocalDateTime.now());
       
       task.getActivities().add(activity);
   }
   
   // Helper method for adding activities after task is saved (for new tasks)
   private void addActivityAfterSave(TaskManagement task, String activityType, String description, Long userId) {
       TaskActivity activity = new TaskActivity();
       activity.setTaskId(task.getId()); // Now task.getId() will have the actual ID
       activity.setActivityType(activityType);
       activity.setDescription(description);
       activity.setUserId(userId);
       activity.setTimestamp(LocalDateTime.now());
       
       task.getActivities().add(activity);
   }
}