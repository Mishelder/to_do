package com.miaskor.dao;

import com.miaskor.database.ConnectionManager;
import com.miaskor.database.MainConnectionManager;
import com.miaskor.database.TestConnectionManager;
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
    private static ConnectionManager connectionManager = MainConnectionManager.getInstance();

    private static final String CREATE_TASK = """
            INSERT INTO task
            (client_id, task_name, done, date) VALUES (?,?,?,?)
            """;
    private static final String READ_TASK = """
            SELECT id, client_id, task_name, done, date
            FROM task
            WHERE id = ?
            """;
    private static final String READ_TASK_BY_DATE = """
            SELECT id, client_id, task_name, done, date
            FROM task
            WHERE date = ? AND client_id = ? ORDER BY id
            """;
    private static final String UPDATE_TASK = """
            UPDATE task
            SET task_name=?
            WHERE id=?
            """;
    private static final String UPDATE_DONE_TASK = """
            UPDATE task
            SET done=?
            WHERE id=?
            """;
    private static final String DELETE_TASK = """
            DELETE FROM task
            WHERE id=?
            """;
    private static final String READ_ALL_TASK = """
            SELECT id, client_id, task_name, done, date
            FROM task ORDER BY id
            """;


    public static TaskDaoImpl getInstance(boolean isTest) {
        if(isTest)
            connectionManager = TestConnectionManager.getInstance();
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
        try (var connection = connectionManager.getConnection();
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
        try (var connection = connectionManager.getConnection();
             var statement = connection.prepareStatement(CREATE_TASK,PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, object.getClientId());
            statement.setObject(2, object.getTaskName());
            statement.setObject(3, object.getDone());
            statement.setObject(4, Date.valueOf(object.getDate()));
            statement.executeUpdate();
            var generatedKeys = statement.getGeneratedKeys();
            if(generatedKeys.next()){
                object.setId(generatedKeys.getObject(1,Integer.class));
            }
        }
        return object;
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
        Task task = null;
        try (var connection = connectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(READ_TASK)) {
            preparedStatement.setInt(1, index);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                task = buildTask(resultSet);
            }
        }
        return Optional.ofNullable(task);
    }

    @Override
    @SneakyThrows
    public List<Task> readByDate(LocalDate day,Integer clientIndex) {
        List<Task> tasks = new ArrayList<>();
        try (var connection = connectionManager.getConnection();
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
    /*
        from - start day included
        to - end day included
     */
    public Map<LocalDate, List<Task>> readAllTaskByPeriod(LocalDate from, LocalDate to,Integer clientIndex) {
        if(to.toString().compareTo(from.toString())<0)
            throw new IllegalArgumentException("Date from cannot be more than date to");
        Map<LocalDate, List<Task>> tasks = new HashMap<>();
        while (!from.equals(to.plusDays(1))) {
            var tasksByDay = readByDate(from, clientIndex);
            if(!tasksByDay.isEmpty())
                tasks.put(from, tasksByDay);
            from = from.plusDays(1);
        }
        return tasks;
    }

    @Override
    @SneakyThrows
    public boolean updateTaskDone(Task task) {
        try (var connection = connectionManager.getConnection();
             var statement = connection.prepareStatement(UPDATE_DONE_TASK)) {
            statement.setBoolean(1,task.getDone());
            statement.setInt(2,task.getId());
            return statement.executeUpdate()>0;
        }
    }

    @Override
    @SneakyThrows
    public boolean update(Task task) {
        try (var connection = connectionManager.getConnection();
             var statement = connection.prepareStatement(UPDATE_TASK)) {
            statement.setString(1,task.getTaskName());
            statement.setInt(2,task.getId());
            return statement.executeUpdate()>0;
        }
    }

    @Override
    @SneakyThrows
    public boolean delete(Integer index) {
        try (var connection = connectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(DELETE_TASK)) {
            preparedStatement.setInt(1, index);
            return preparedStatement.executeUpdate()>0;
        }
    }

    private Task buildTask(ResultSet resultSet) throws SQLException {
        return Task.builder()
                .id(resultSet.getObject(1, Integer.class))
                .clientId(resultSet.getObject(2, Integer.class))
                .taskName(resultSet.getObject(3, String.class))
                .done(resultSet.getObject(4, Boolean.class))
                .date(resultSet.getObject(5, LocalDate.class)).build();
    }
}
