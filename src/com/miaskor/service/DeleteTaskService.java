package com.miaskor.service;

import com.miaskor.dao.TaskDaoImpl;
import com.miaskor.dto.DeleteTaskDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteTaskService {
    private static final DeleteTaskService INSTANCE = new DeleteTaskService();
    private final TaskDaoImpl taskDao = TaskDaoImpl.getInstance(false);

    public void deleteTask(DeleteTaskDto deleteTaskDto){
        int deleteTaskId = deleteTaskDto.getId();
        taskDao.delete(deleteTaskId);
    }

    public static DeleteTaskService getInstance(){
        return INSTANCE;
    }
}
