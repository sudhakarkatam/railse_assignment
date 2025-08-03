package com.railse.hiring.workforcemgmt.mapper;

import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import com.railse.hiring.workforcemgmt.common.model.TaskManagement;
import com.railse.hiring.workforcemgmt.common.model.TaskActivity;
import com.railse.hiring.workforcemgmt.dto.TaskActivityDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ITaskManagementMapper {
   ITaskManagementMapper INSTANCE = Mappers.getMapper(ITaskManagementMapper.class);

   TaskManagementDto modelToDto(TaskManagement model);

   @Mapping(target = "activities", ignore = true) // Ignore activities when converting DTO to model
   TaskManagement dtoToModel(TaskManagementDto dto);

   List<TaskManagementDto> modelListToDtoList(List<TaskManagement> models);
   
   // Add mapping for activities
   TaskActivityDto activityToDto(TaskActivity activity);
   List<TaskActivityDto> activityListToDtoList(List<TaskActivity> activities); // Fixed: was List<TaskActivityDto>
} 