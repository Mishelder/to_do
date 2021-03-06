package com.miaskor.dao;

import com.miaskor.cache.TasksCacheLRU;
import com.miaskor.database.ConnectionManager;
import com.miaskor.entity.Task;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskDaoImpl implements TaskDao<Integer, Task> {

    private static final TaskDaoImpl INSTANCE = new TaskDaoImpl();

    private static final String CREATE_TASK = """
            INSERT INTO to_do_list_repository.public.task
            (client_id, task_name, done, date,index_in_form) VALUES (?,?,?,?,?)
            """;
    private static final String READ_TASK = """
            SELECT id, client_id, task_name, done, date, index_in_form
            FROM to_do_list_repository.public.task
            WHERE id = ?
            """;
    private static final String READ_TASK_BY_DATE = """
            SELECT id, client_id, task_name, done, date, index_in_form
            FROM to_do_list_repository.public.task
            WHERE date = ? AND client_id = ?
            """;
    private static final String UPDATE_TASK = """
            UPDATE to_do_list_repository.public.task
            SET client_id=?, task_name=?, done=?, date=?, index_in_form =?
            WHERE id=?
            """;
    private static final String DELETE_TASK = """
            DELETE FROM to_do_list_repository.public.task
            WHERE id=?
            """;
    private static final String DELETE_TASK_BY_DATE_AND_CLIENT_ID = """
            DELETE FROM to_do_list_repository.public.task
            WHERE date=? AND client_id=?
            """;
    private static final String READ_ALL_TASK = """
            SELECT id, client_id, task_name, done, date, index_in_form
            FROM to_do_list_repository.public.task
            """;


    public static TaskDaoImpl getInstance() {
        return INSTANCE;
    }


    /*
     * clientDao.read(resultSet.getObject(2,Integer.class)).get()
     * won't throw NoSuchElementException because cell in database cannot be null
     *
     * maybe fetch all clients by one request will enhance performance
     * */
    @Override
    @SneakyThrows
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(READ_ALL_TASK)) {
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                tasks.add(buildTask(resultSet));
            }
        }
        return tasks;
    }

    @Override
    @SneakyThrows
    public Task create(Task object) {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(CREATE_TASK)) {
            statement.setObject(1, object.getClientId());
            statement.setObject(2, object.getTaskName());
            statement.setObject(3, object.getDone());
            statement.setObject(4, Date.valueOf(object.getDate()));
            statement.setObject(5, object.getIndexInForm());
            statement.execute();
        }
        return Task.builder().build();
    }

    @Override
    @SneakyThrows
    public void createTasks(List<Task> objects) {
        for (Task task : objects) {
            create(task);
        }
    }

    @Override
    @SneakyThrows
    public Optional<Task> read(Integer index) {
        Optional<Task> task = Optional.empty();
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(READ_TASK)) {
            preparedStatement.setInt(1, index);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                task = Optional.ofNullable(buildTask(resultSet));
            }
        }
        return task;
    }

    @Override
    @SneakyThrows
    public List<Task> readByDate(LocalDate day,Integer clientIndex) {
        List<Task> tasks = new ArrayList<>();
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(READ_TASK_BY_DATE)) {
            preparedStatement.setDate(1, Date.valueOf(day));
            preparedStatement.setInt(2, clientIndex);
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                tasks.add(buildTask(resultSet));
            }
        }
        return tasks;
    }

    @Override
    @SneakyThrows
    public Map<LocalDate, List<Task>> readAllTaskByPeriod(LocalDate from, LocalDate to,Integer clientIndex) {
        Map<LocalDate, List<Task>> tasks = new HashMap<>();
        to = to.plusDays(1);
        while (!from.equals(to)) {
            var tasksByDay = readByDate(from, clientIndex);
            if(!tasksByDay.isEmpty())
                tasks.put(from, tasksByDay);
            from = from.plusDays(1);
        }
        return tasks;
    }

    @Override
    public void update(Task object) {

    }

    @Override
    @SneakyThrows
    public boolean delete(Integer index) {
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(DELETE_TASK)) {
            preparedStatement.setInt(1, index);
            return preparedStatement.execute();
        }
    }

    @Override
    @SneakyThrows
    public void deleteTaskByDateAndClientId(LocalDate day,Integer clientId) {
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(DELETE_TASK_BY_DATE_AND_CLIENT_ID)) {
            preparedStatement.setDate(1, Date.valueOf(day));
            preparedStatement.setInt(2,clientId);
            preparedStatement.execute();
        }
    }

    private Task buildTask(ResultSet resultSet) throws java.sql.SQLException {
        return Task.builder()
                .id(resultSet.getObject(1, Integer.class))
                .clientId(resultSet.getObject(2, Integer.class))
                .taskName(resultSet.getObject(3, String.class))
                .done(resultSet.getObject(4, Boolean.class))
                .date(resultSet.getObject(5, LocalDate.class))
                .indexInForm(resultSet.getObject(6, Integer.class)).build();
    }
}
