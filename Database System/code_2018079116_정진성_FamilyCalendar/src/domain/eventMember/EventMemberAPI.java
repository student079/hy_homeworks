package domain.eventMember;

import domain.user.UserAPI;

import java.sql.*;
import java.util.ArrayList;

public class EventMemberAPI {
    public static int registerMembers(ArrayList<String> members, int eventId) {
        for (String member : members) {
            int userId = UserAPI.getUserIdByuserName(member);

            String query = "INSERT INTO eventmember (event_id, user_id) VALUES (?, ?)";

            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, eventId);
                preparedStatement.setInt(2,userId);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected <= 0) {
                    return -1;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    // 이벤트 멤버 업데이트
    public static int updateEventMember(int eventId, ArrayList<String> members) {
        // eventId인 튜플 삭제하고
        // 새로 추가
        String query = "DELETE FROM eventmember WHERE event_id = ?";
        int rowsAffected = 0;

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, eventId);

            rowsAffected = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        if (rowsAffected > 0) {
            query = "INSERT INTO eventmember (event_id, user_id) VALUES (?, ?)";
            ArrayList<Integer> memberIds = new ArrayList<>();
            for (String member : members) {
                memberIds.add(UserAPI.getUserIdByuserName(member));
            }

            for (int memberId : memberIds) {
                try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
                     PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                    preparedStatement.setInt(1, eventId);
                    preparedStatement.setInt(2, memberId);

                    int rowsAffected2 = preparedStatement.executeUpdate();

                    if (rowsAffected2 <= 0) {
                        return -1;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return -1;
                }
            }
            return memberIds.size();
        }
        return -1;
    }

    // 이벤트 멤버에서 멤버 삭제
    public static int deleteMemberByEventIdAndUserId(int eventId, int userId) {
        String query = "delete from eventmember where event_id = ? and user_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, eventId);
            preparedStatement.setInt(2, userId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }


    // userId를 멤버로가지고 있는 eventId 반환
    public static ArrayList<Integer> getEventIdListByUserId(int userId) {
        ArrayList<Integer> eventIdList = new ArrayList<>();

        String query = "SELECT distinct event_id FROM eventmember WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dbms_practice", "dbms_practice", "dbms_practice");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                eventIdList.add(resultSet.getInt("event_id"));
            }
            return eventIdList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
