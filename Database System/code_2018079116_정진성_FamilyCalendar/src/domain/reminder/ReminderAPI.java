package domain.reminder;

import java.sql.*;
import java.util.Calendar;
import java.util.Date;

public class ReminderAPI {
    public static Reminder registerReminder(int eventId, Date reminderStartTime, int interval) {
        String query = "INSERT INTO reminder (event_id, reminder_start_time, interval) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, eventId);
            preparedStatement.setTimestamp(2, new Timestamp(reminderStartTime.getTime()));
            preparedStatement.setInt(3, interval);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int reminderId = generatedKeys.getInt(1);
                    return new Reminder(reminderId, eventId, reminderStartTime, interval);
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

    // event에 대한 리마인더 date와 비교해서 출력
    public static Reminder getReminderByEventIdAndDate(int eventId, Date date) {

        String query = "select * from reminder join event on event.event_id = reminder.event_id where event.event_id = ? and " +
                "reminder.reminder_start_time <= ? and" +
                " reminder.reminder_start_time >= ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, 60);
            Date newDate = calendar.getTime();
            preparedStatement.setInt(1, eventId);
            preparedStatement.setTimestamp(2, new Timestamp(newDate.getTime()));
            preparedStatement.setTimestamp(3, new Timestamp(date.getTime()));

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Reminder(resultSet.getInt("reminder_id"), resultSet.getInt("event_id"),
                        new Date(resultSet.getTimestamp("reminder_start_time").getTime()), resultSet.getInt("interval"));
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // event에 대한 리마인더 가져오기
    public static Reminder getReminderByEventId(int eventId) {

        String query = "select * from reminder join event on event.event_id = reminder.event_id where event.event_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, eventId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Reminder(resultSet.getInt("reminder_id"), resultSet.getInt("event_id"),
                        new Date(resultSet.getTimestamp("reminder_start_time").getTime()), resultSet.getInt("interval"));
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // reminderId로 reminder삭제
    public static int deleteReminderByReminderId(int reminderId) {
        String query = "delete from reminder where reminder_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, reminderId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }


}
