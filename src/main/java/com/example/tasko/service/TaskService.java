package com.example.tasko.service;

import com.example.tasko.model.Task;
import com.example.tasko.model.User;
import com.example.tasko.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus("PENDING");
        return taskRepository.save(task);
    }

    public List<Task> getUserTasks(User user) {
        return taskRepository.findByUserOrderByDueDateAsc(user);
    }

    public Page<Task> getUserTasksPaginated(User user, Pageable pageable) {
        return taskRepository.findByUser(user, pageable);
    }

    public Task updateTaskStatus(Long taskId, String status) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(status);
        if (status.equals("COMPLETED")) {
            task.setCompletedAt(LocalDateTime.now());
        }
        return taskRepository.save(task);
    }

    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }

    public List<Task> getOverdueTasks(User user) {
        return taskRepository.findByUserAndDueDateBeforeAndStatusNot(
            user,
            LocalDateTime.now(),
            "COMPLETED"
        );
    }

    public List<Task> getUpcomingTasks(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusDays(7);
        return taskRepository.findByUserAndDueDateBetweenAndStatusNot(
            user,
            now,
            weekLater,
            "COMPLETED"
        );
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    public Task updateTask(Long taskId, Task taskDetails) {
        Task task = getTaskById(taskId);
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setDueDate(taskDetails.getDueDate());
        return taskRepository.save(task);
    }
}