package com.taskanalysis.controller;

import com.taskanalysis.dto.export.DeliveryMethod;
import com.taskanalysis.dto.export.ExportFormat;
import com.taskanalysis.dto.export.ExportRequestDto;
import com.taskanalysis.dto.export.ExportResponseDto;
import com.taskanalysis.entity.Task;
import com.taskanalysis.entity.User;
import com.taskanalysis.repository.UserRepository;
import com.taskanalysis.security.CurrentUser;
import com.taskanalysis.service.EmailService;
import com.taskanalysis.service.ExportService;
import com.taskanalysis.service.TaskService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/export")
@Slf4j
public class ExportController {

    @Autowired
    private ExportService exportService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private UserRepository userRepository;

    /**
     * Export task data
     * POST /api/export/task/{taskId}
     *
     * @param taskId Task ID to export
     * @param request Export request (format, delivery method)
     * @return Export response or file download
     */
    @PostMapping("/task/{taskId}")
    public ResponseEntity<?> exportTask(
            @PathVariable Long taskId,
            @Valid @RequestBody ExportRequestDto request
    ) {
        log.info("Export request for task {} with format {} and delivery {}", 
                taskId, request.getFormat(), request.getDelivery());

        try {
            Long userId = getCurrentUserId();
            // Get task with authorization check
            Task task = taskService.getTaskEntityById(taskId, userId);
            User user = task.getUser();

            // Generate export file
            byte[] fileBytes;
            String fileName;
            String fileExtension;

            if (request.getFormat() == ExportFormat.XLSX) {
                fileBytes = exportService.generateExcelExport(task);
                fileExtension = "xlsx";
                fileName = generateFileName(task.getName(), fileExtension);
            } else {
                // PDF export - TODO: implement later
                return ResponseEntity.badRequest()
                        .body(ExportResponseDto.error("PDF export not yet implemented"));
            }

            // Handle delivery method
            if (request.getDelivery() == DeliveryMethod.EMAIL) {
                // Send via email
                emailService.sendExportEmail(
                        user.getEmail(),
                        user.getName(),
                        task.getName(),
                        task.getCategory() != null ? task.getCategory().getName() : "Nincs kategória",
                        fileBytes,
                        fileName,
                        fileExtension
                );

                String message = String.format("Export elküldve email-ben! Ellenőrizd: %s", user.getEmail());
                return ResponseEntity.ok(ExportResponseDto.success(DeliveryMethod.EMAIL, fileName, message));

            } else {
                // Direct download
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(getMediaType(request.getFormat()));
                headers.setContentDispositionFormData("attachment", fileName);
                headers.setContentLength(fileBytes.length);

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(new ByteArrayResource(fileBytes));
            }

        } catch (IOException e) {
            log.error("Failed to generate export file", e);
            return ResponseEntity.internalServerError()
                    .body(ExportResponseDto.error("Export fájl generálása sikertelen: " + e.getMessage()));

        } catch (MessagingException e) {
            log.error("Failed to send export email", e);
            return ResponseEntity.internalServerError()
                    .body(ExportResponseDto.error("Email küldése sikertelen: " + e.getMessage()));

        } catch (Exception e) {
            log.error("Export failed", e);
            return ResponseEntity.internalServerError()
                    .body(ExportResponseDto.error("Export sikertelen: " + e.getMessage()));
        }
    }

    /**
     * Generate file name with timestamp
     */
    private String generateFileName(String taskName, String extension) {
        String sanitized = taskName.replaceAll("[^a-zA-Z0-9]", "_");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("taskanalysis_%s_%s.%s", sanitized, timestamp, extension);
    }

    /**
     * Get media type for export format
     */
    private MediaType getMediaType(ExportFormat format) {
        return switch (format) {
            case XLSX -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case PDF -> MediaType.APPLICATION_PDF;
        };
    }

    private Long getCurrentUserId() {
        String email = currentUser.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
