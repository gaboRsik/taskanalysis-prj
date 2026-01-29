package com.taskanalysis.dto.task;

import com.taskanalysis.dto.subtask.SubtaskResponse;
import com.taskanalysis.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Integer subtaskCount;
    private Task.TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SubtaskResponse> subtasks;

}
