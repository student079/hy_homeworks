package domain.user;

import domain.event.Event;
import domain.event.EventAPI;

import java.sql.*;
import java.util.ArrayList;

public class UserAPI {

    // 사용자 이름이 이미 데이터베이스에 존재하는지 확인
    public static boolean isUserAlreadyExists(String username) {

        String checkUserQuery = "SELECT * FROM \"user\" WHERE username = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(checkUserQuery)) {

            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // 결과가 있으면 이미 존재하는 사용자
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    // 사용자 이름, 아이디, 패스워드가 데이터베이스에 일치하는 튜플이 있는지 확인
    public static User isUserExists(String username, String id, String password) {
        String checkUserQuery = "SELECT * FROM \"user\" WHERE username = ? AND id = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(checkUserQuery)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, id);
            preparedStatement.setString(3, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(resultSet.getInt("user_id"), resultSet.getString("username"),
                        resultSet.getString("id"), resultSet.getString("password"),
                        resultSet.getString("email"), resultSet.getInt("channel"));
            } else{
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 사용자를 데이터베이스에 등록
    public static User registerUser(String username, String id, String password, String email, int channel) {
        String signupQuery = "INSERT INTO \"user\" (username, id, password, email, channel) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(signupQuery, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, id);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, email);
            preparedStatement.setInt(5, channel);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    return new User(userId, username, id, password, email, channel);
                }
                return null;
            }
            else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // userId로 user찾기
    public static User getUserByuserId(int userId) {
        String query = "SELECT * FROM \"user\" WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(resultSet.getInt("user_id"), resultSet.getString("username"),
                        resultSet.getString("id"), resultSet.getString("password"),
                        resultSet.getString("email"), resultSet.getInt("channel"));
            } else{
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // userId에 해당하는 user 정보 업데이트
    public static User updateUser(int oldUserId, String newName, String newId, String newPw, String newEmail, int newCh) {
        String query = "UPDATE \"user\" SET username = ?, id = ?, password = ?, email = ?, channel = ? WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, newName);
            preparedStatement.setString(2, newId);
            preparedStatement.setString(3, newPw);
            preparedStatement.setString(4, newEmail);
            preparedStatement.setInt(5, newCh);
            preparedStatement.setInt(6,oldUserId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return getUserByuserId(oldUserId);
            } else{
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 모든 유저 받아오기
    public static String[] getAllUsers() {

        ArrayList<String> userList = new ArrayList<>();

        String query = "SELECT * from \"user\"";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                 String newUser = resultSet.getString("username");
                userList.add(newUser);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return userList.toArray(new String[0]);
    }

    // username으로 userid찾기
    public static int getUserIdByuserName(String userName) {
        String query = "SELECT user_id FROM \"user\" WHERE username = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, userName);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            } else{
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    //userId로 username찾기
    public static String getUserNameByUserId(int userId) {
        String query = "SELECT username FROM \"user\" WHERE user_id = ?";
        String userName = null;

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                userName = resultSet.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userName;
    }

    // user가 참석할 event 목록 가져오기
    public static ArrayList<Event> getEventListByuserId(int userId) {
        String query = "SELECT event_id FROM eventmember WHERE user_id = ?";
        ArrayList<Event> eventList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                eventList.add(EventAPI.getEventByEventId(resultSet.getInt("event_id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return eventList;
    }
}
