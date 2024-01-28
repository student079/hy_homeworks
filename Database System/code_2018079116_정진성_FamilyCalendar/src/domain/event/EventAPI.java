package domain.event;

import domain.user.UserAPI;

import java.sql.*;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Date;

public class EventAPI {
    public static Event registerEvent(String eventName, Date sTime, Date eTime, String detail, int eventCreatorId) {
        String query = "INSERT INTO event (event_name, event_stime, event_etime, event_detail,  event_creator_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, eventName);
            preparedStatement.setTimestamp(2, new Timestamp(sTime.getTime()));
            preparedStatement.setTimestamp(3, new Timestamp(eTime.getTime()));
            preparedStatement.setString(4, detail);
            preparedStatement.setInt(5, eventCreatorId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int eventId = generatedKeys.getInt(1);
                    return new Event(eventId, eventName, sTime, eTime, detail, eventCreatorId);
                }
                return null;
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 해당 시간term에 겹치는 eventIds 반환
    public static ArrayList<Integer> getEventIdsBytime(Date sTime, Date eTime) {
        ArrayList<Integer> eventList = new ArrayList<>();

        String query = "SELECT event_id FROM event WHERE (event_stime <= ? and event_etime >= ?) or (event_stime <= ? and event_etime >= ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setTimestamp(1, new Timestamp(sTime.getTime()));
            preparedStatement.setTimestamp(2, new Timestamp(sTime.getTime()));
            preparedStatement.setTimestamp(3, new Timestamp(eTime.getTime()));
            preparedStatement.setTimestamp(4, new Timestamp(eTime.getTime()));

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                eventList.add(resultSet.getInt("event_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return eventList;
    }

    // event에 포함된 usernames 반환
    public static ArrayList<String> getMemberNamesByEventId(int eventId) {
        ArrayList<Integer> userIds = new ArrayList<>();
        ArrayList<String> userNames = new ArrayList<>();

        String query = "SELECT user_id FROM eventmember WHERE event_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, eventId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                userIds.add(resultSet.getInt("user_id"));
            }

            for (int userId : userIds) {
                String userName = UserAPI.getUserNameByUserId(userId);
                if (userName != null) {
                    userNames.add(userName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return userNames;
    }

    // eventId로 event 찾기
    public static Event getEventByEventId(int eventId) {
        String query = "SELECT * FROM event WHERE event_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, eventId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Event(eventId, resultSet.getString("event_name"), resultSet.getTimestamp("event_stime"),
                        resultSet.getTimestamp("event_etime"), resultSet.getString("event_detail"), resultSet.getInt("event_creator_id"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // date, userId로 eventname목록 반환
    public static ArrayList<String> getEventNamesByDateAndUserId(java.sql.Date date, int userId) {

        ArrayList<String> eventNames = new ArrayList<>();
        String query = "select distinct event.event_name from event join eventmember on event.event_id = eventmember.event_id " +
                "where eventmember.user_id = ? and (DATE(event.event_stime) <= ? AND DATE(event.event_etime) >= ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setDate(2, date);
            preparedStatement.setDate(3, date);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                eventNames.add(resultSet.getString("event_name"));
            }
            return eventNames;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // eventName으로 Event객체 반환
    public static Event getEventByEventName(String eventName) {
        String query = "SELECT * FROM event WHERE event_name = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, eventName);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Event(resultSet.getInt("event_id"), eventName, resultSet.getTimestamp("event_stime"),
                        resultSet.getTimestamp("event_etime"), resultSet.getString("event_detail"),
                        resultSet.getInt("event_creator_id"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // eventId에 해당하는 event 정보 업데이트
    public static int updateEvent(int eventId, String eventName, Date eventSTime, Date eventETime, String eventDetail) {
        String query = "UPDATE event SET event_name = ?, event_stime = ?, event_etime = ?, event_detail = ? WHERE event_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, eventName);
            preparedStatement.setTimestamp(2, new Timestamp(eventSTime.getTime()));
            preparedStatement.setTimestamp(3, new Timestamp(eventETime.getTime()));
            preparedStatement.setString(4, eventDetail);
            preparedStatement.setInt(5, eventId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // eventId로 이벤트 삭제
    public static int deleteEventByEventId(int eventId) {
        String query = "delete from event where event_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, eventId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // 이벤트 검색
    public static ArrayList<Event> getEventListByEventNameOrEventDetail(String eventName, String eventDetail, int userId) {
        ArrayList<Event> eventArrayList = new ArrayList<>();

        if (eventName.isEmpty() && !eventDetail.isEmpty()) {
            String query = "select * from event join eventmember on event.event_id = eventmember.event_id where eventmember.user_id = ? and " +
                    "event.event_detail like ?";

            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, "%" + eventDetail + "%");

                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    eventArrayList.add(new Event(resultSet.getInt("event_id"), resultSet.getString("event_name"), resultSet.getTimestamp("event_stime"),
                            resultSet.getTimestamp("event_etime"), resultSet.getString("event_detail"),
                            resultSet.getInt("event_creator_id")));
                }
                return eventArrayList;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        } else if (!eventName.isEmpty() && eventDetail.isEmpty()) {
            String query = "select * from event join eventmember on event.event_id = eventmember.event_id where eventmember.user_id = ? and " +
                    "event.event_name like ?";

            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, "%" + eventName + "%");

                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    eventArrayList.add(new Event(resultSet.getInt("event_id"), resultSet.getString("event_name"), resultSet.getTimestamp("event_stime"),
                            resultSet.getTimestamp("event_etime"), resultSet.getString("event_detail"),
                            resultSet.getInt("event_creator_id")));
                }
                return eventArrayList;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else if (!eventName.isEmpty() && !eventDetail.isEmpty()) {
            String query = "select * from event join eventmember on event.event_id = eventmember.event_id where eventmember.user_id = ? and " +
                    "event.event_name like ? and event.event_datail like ?";

            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, "%" + eventName + "%");
                preparedStatement.setString(3, "%" + eventDetail + "%");

                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    eventArrayList.add(new Event(resultSet.getInt("event_id"), resultSet.getString("event_name"), resultSet.getTimestamp("event_stime"),
                            resultSet.getTimestamp("event_etime"), resultSet.getString("event_detail"),
                            resultSet.getInt("event_creator_id")));
                }
                return eventArrayList;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    // 모든 이벤트 가져오기
    public static ArrayList<String> getAllEventNames() {
        ArrayList<String> eventNames = new ArrayList<>();

        String query = "SELECT event_name FROM event";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                eventNames.add(resultSet.getString("event_name"));
            }
            return eventNames;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
