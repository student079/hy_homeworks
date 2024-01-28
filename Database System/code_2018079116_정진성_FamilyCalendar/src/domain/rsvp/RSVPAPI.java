package domain.rsvp;

import domain.reminder.Reminder;

import java.sql.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;

public class RSVPAPI {
    public static RSVP registerRSVP(int eventId, int hostUserId, int guestUserId, Date requestTime) {
        String query = "INSERT INTO rsvp (event_id, host_user_id, guest_user_id, request_time, rsvp_status) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, eventId);
            preparedStatement.setInt(2, hostUserId);
            preparedStatement.setInt(3, guestUserId);
            preparedStatement.setTimestamp(4, Timestamp.from(requestTime.toInstant().truncatedTo(ChronoUnit.MINUTES)));
            preparedStatement.setInt(5, 0);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int RSVPId = generatedKeys.getInt(1);
                    return new RSVP(RSVPId, eventId, hostUserId, guestUserId, requestTime, 0);
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

    // guest_id가 user_id이고 status가 0인 RSVP 객체들 찾기
    public static ArrayList<RSVP> getRSVPListByGuestUserIdAndStatus(int guestUserId, int status) {
        ArrayList<RSVP> RSVPList = new ArrayList<>();
        String query = "SELECT * FROM rsvp WHERE guest_user_id = ? and rsvp_status = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, guestUserId);
            preparedStatement.setInt(2, status);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                RSVPList.add(new RSVP(resultSet.getInt("rsvp_id"),resultSet.getInt("event_id"), resultSet.getInt("host_user_id"), resultSet.getInt("guest_user_id"),
                        new Date(resultSet.getTimestamp("request_time").getTime()), resultSet.getInt("rsvp_status")));
            }
            return RSVPList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // host_id가 user_id인 RSVP 객체들 찾기
    public static ArrayList<HostRSVP> getRSVPListByHostUserId(int hostUserId) {
        ArrayList<HostRSVP> RSVPList = new ArrayList<>();
        String query = "SELECT * FROM rsvp WHERE host_user_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, hostUserId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                RSVPList.add(new HostRSVP(resultSet.getInt("rsvp_id"),resultSet.getInt("event_id"), resultSet.getInt("host_user_id"), resultSet.getInt("guest_user_id"),
                        new Date(resultSet.getTimestamp("request_time").getTime()), resultSet.getInt("rsvp_status")));
            }
            return RSVPList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // rsvp_id로 rsvp 릴레이션 삭제
    public static int deleteRSVPByRSVPId(int rsvpId) {
        String query = "delete from rsvp where rsvp_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, rsvpId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // rsvp 상태 업데이트
    public static void updateStatusByrsvpId(int rsvpId, int status) {
        String query = "UPDATE rsvp SET rsvp_status = ? WHERE rsvp_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, status);
            preparedStatement.setInt(2, rsvpId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //rsvp 상태 반환
    public static int getStatusByRsvpId(int rsvpId) {
        String query = "select rsvp_status from rsvp where rsvp_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, rsvpId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("rsvp_status");
            }
            return -2;
        } catch (SQLException e) {
            e.printStackTrace();
            return -2;
        }
    }
}
