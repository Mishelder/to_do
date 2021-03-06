package com.miaskor.dao;

import com.miaskor.database.ConnectionManager;
import com.miaskor.entity.Client;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access= AccessLevel.PRIVATE)
public class ClientDaoImpl implements ClientDao<Integer, Client> {

    private static final ClientDaoImpl INSTANCE = new ClientDaoImpl();

    private static final String CREATE_CLIENT = """ 
            INSERT INTO to_do_list_repository.public.client
            (login, email, password) VALUES (?,?,?)""";
    private static final String READ_CLIENT = """
            SELECT id,login,email,password FROM to_do_list_repository.public.client""";
    private static final String UPDATE_CLIENT = """
            UPDATE to_do_list_repository.public.client
            SET login = ?,email=?,password=? WHERE id ?""" ;
    private static final String DELETE_CLIENT_BY_ID = """
            DELETE FROM to_do_list_repository.public.client WHERE id = ?""";
    private static final String READ_ALL_CLIENT = """
            SELECT id,login,email,password FROM to_do_list_repository.public.client""";
    private static final String READ_BY_LOGIN = """
            SELECT id,login,email,password FROM to_do_list_repository.public.client WHERE login = ?""";
    private static final String READ_BY_EMAIL = """
            SELECT id,login,email,password FROM to_do_list_repository.public.client WHERE email = ?""";

    public static ClientDaoImpl getInstance(){
        return INSTANCE;
    }

    @Override
    @SneakyThrows
    public Client create(Client object) {
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(CREATE_CLIENT)) {
            preparedStatement.setString(1, object.getLogin());
            preparedStatement.setString(2, object.getEmail());
            preparedStatement.setString(3, object.getPassword());
            preparedStatement.execute();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if(generatedKeys.next())
                object.setId(generatedKeys.getObject(1,Integer.class));
        }
        return object;
    }

    @Override
    @SneakyThrows
    public Optional<Client> read(Integer index) {
        Client client = null;
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(READ_CLIENT)) {
            preparedStatement.setInt(1, index);
            var resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                 client = buildClient(resultSet);
            }
        }
        return Optional.ofNullable(client);
    }

    @Override
    @SneakyThrows
    public void update(Client object) {
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(UPDATE_CLIENT)) {
            preparedStatement.setString(1, object.getLogin());
            preparedStatement.setString(2, object.getEmail());
            preparedStatement.setString(3, object.getPassword());
            preparedStatement.setInt(4, object.getId());
            preparedStatement.execute();
        }
    }

    @Override
    @SneakyThrows
    public boolean delete(Integer index) {
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(DELETE_CLIENT_BY_ID)) {
            preparedStatement.setInt(1, index);
            return preparedStatement.execute();
        }
    }

    @Override
    @SneakyThrows
    public List<Client> findAll(){
        List<Client> clients = new ArrayList<>();
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(READ_ALL_CLIENT)) {
            var resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                clients.add(buildClient(resultSet));
            }
        }
        return clients;
    }

    @Override
    @SneakyThrows
    public Optional<Client> readByLogin(String login) {
        Client client = null;
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(READ_BY_LOGIN)) {
            preparedStatement.setString(1, login);
            var resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                client = buildClient(resultSet);
            }
        }
        return Optional.ofNullable(client);
    }

    @Override
    @SneakyThrows
    public Optional<Client> readByEmail(String email) {
        Client client = null;
        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(READ_BY_EMAIL)) {
            preparedStatement.setString(1, email);
            var resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                client = buildClient(resultSet);
            }
        }
        return Optional.ofNullable(client);
    }

    private Client buildClient(ResultSet resultSet) throws java.sql.SQLException {
        return Client.builder().id(resultSet.getObject(1,Integer.class))
                .login(resultSet.getObject(2,String.class))
                .email(resultSet.getObject(3,String.class))
                .password(resultSet.getObject(4,String.class))
                .build();
    }
}
