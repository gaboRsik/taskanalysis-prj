package com.taskanalysis.dto.timer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimerResponse {

    private Long timeEntryId;
    private Long subtaskId;
    private Integer subtaskNumber;
    private String taskTitle;
    private String subtaskTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationSeconds;
    private boolean isRunning;

}
