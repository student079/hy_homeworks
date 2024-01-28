package app;

import domain.event.Event;
import domain.event.EventAPI;
import domain.eventMember.EventMemberAPI;
import domain.rsvp.RSVP;
import domain.rsvp.RSVPAPI;
import domain.user.User;
import domain.user.UserAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class EventDetailApp {
    public EventDetailApp(String eventName, User user) {
        Event event = EventAPI.getEventByEventName(eventName);
        JFrame eventDetailFrame = new JFrame(eventName + "상세");
        if (event != null) {
            eventDetailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            eventDetailFrame.setSize(500, 300);
            eventDetailFrame.setLocationRelativeTo(null);
            eventDetailFrame.setLayout(new BorderLayout());

            // 수정, 삭제, rsvp 보내기 버튼
            JPanel headerPanel = new JPanel();
            JButton updateBtn = new JButton("일정 수정");
            JButton deleteBtn = new JButton("일정 삭제");
            JButton rsvpRequestBtn = new JButton("RSVP 요청");
            headerPanel.add(deleteBtn);
            // rsvp, 수정은 일정 주최자만 보이도록
            if (event.getEventCreatorId() == user.getUserId()) {
                headerPanel.add(updateBtn);
                headerPanel.add(rsvpRequestBtn);
            }

            // 일정삭제 버튼
            deleteBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // 주최자면 이벤트 아예 삭제
                    // 주최자 아니면 이벤트 멤버에서만 삭제
                    if (user.getUserId() == event.getEventCreatorId()) {
                        if (EventAPI.deleteEventByEventId(event.getEventId()) > 0) {
                            JOptionPane.showMessageDialog(null,"모든 캘린더에서 일정이 삭제되었습니다. 새로고침 해보세요");
                            eventDetailFrame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(null,"일정 삭제 실패","오류", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    else {
                        if (EventMemberAPI.deleteMemberByEventIdAndUserId(event.getEventId(), user.getUserId()) > 0) {
                            JOptionPane.showMessageDialog(null,"나의 캘린더에서 일정이 삭제되었습니다. 새로고침 해보세요");
                        } else {
                            JOptionPane.showMessageDialog(null,"일정 삭제 실패","오류", JOptionPane.ERROR_MESSAGE);
                            eventDetailFrame.dispose();
                        }
                    }
                }
            });

            JPanel bodyPanel = new JPanel(new GridLayout(6,2));
            bodyPanel.add(new JLabel("일정 이름"));
            bodyPanel.add(new JLabel(event.getEventName()));
            bodyPanel.add(new JLabel("주최자"));
            bodyPanel.add(new JLabel(UserAPI.getUserNameByUserId(event.getEventCreatorId())));
            bodyPanel.add(new JLabel("참석자"));
            // event참석자 가져오기
            bodyPanel.add(new JLabel(EventAPI.getMemberNamesByEventId(event.getEventId()).toString()));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            bodyPanel.add(new JLabel("일정 시작 시간"));
            bodyPanel.add(new JLabel(sdf.format(event.getEventSTime())));
            bodyPanel.add(new JLabel("일정 끝나는 시간"));
            bodyPanel.add(new JLabel(sdf.format(event.getEventETime())));
            bodyPanel.add(new JLabel("일정 설명"));
            String detail = event.getEventDetail();
            if (detail.isEmpty()) {
                detail = "(없음)";
            }
            bodyPanel.add(new JLabel(detail));

            eventDetailFrame.add(bodyPanel, BorderLayout.CENTER);
            eventDetailFrame.add(headerPanel, BorderLayout.NORTH);
            eventDetailFrame.setVisible(true);

            // 일정 수정 버튼
            updateBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                    JTextField eventNameField = new JTextField(event.getEventName());

                    SpinnerModel smodel = new SpinnerDateModel(new Date(event.getEventSTime().getTime()), null, null, Calendar.HOUR_OF_DAY);
                    JSpinner sspinner = new JSpinner(smodel);
                    JSpinner.DateEditor seditor = new JSpinner.DateEditor(sspinner, "yyyy-MM-dd HH:mm");
                    sspinner.setEditor(seditor);

                    SpinnerModel emodel = new SpinnerDateModel(new Date(event.getEventETime().getTime()), null, null, Calendar.HOUR_OF_DAY);
                    JSpinner espinner = new JSpinner(emodel);
                    JSpinner.DateEditor eeditor = new JSpinner.DateEditor(espinner, "yyyy-MM-dd HH:mm");
                    espinner.setEditor(eeditor);

                    // 모든 유저들을 리스트에 넣고 참석이었던 애들은 선택되어있는 것으로
                    String[] allUsers = UserAPI.getAllUsers();
                    JList members = new JList(allUsers);
                    members.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                    ArrayList<String> yesMembers = EventAPI.getMemberNamesByEventId(event.getEventId());
                    for (int i = 0; i < allUsers.length; i++) {
                        if (yesMembers.contains(allUsers[i])) {
                            members.setSelectedIndex(i);
                        }
                    }
                    JScrollPane scrollPane = new JScrollPane(members);

                    JTextField eventDetailField = new JTextField(event.getEventDetail());
                    JPanel updatePanel = new JPanel(new GridLayout(5,2));
                    updatePanel.add(new JLabel("일정 이름"));
                    updatePanel.add(eventNameField);
                    updatePanel.add(new JLabel("참석자"));
                    updatePanel.add(scrollPane);
                    updatePanel.add(new JLabel("시작 시간"));
                    updatePanel.add(sspinner);
                    updatePanel.add(new JLabel("끝나는 시간"));
                    updatePanel.add(espinner);
                    updatePanel.add(new JLabel("일정 설명"));
                    updatePanel.add(eventDetailField);

                    int result = JOptionPane.showConfirmDialog(null, updatePanel, "일정 수정",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                    if (result == JOptionPane.OK_OPTION) {
                        // 디비 연동하고
                        int r1 = EventAPI.updateEvent(event.getEventId(), eventNameField.getText(), (Date) sspinner.getValue(), (Date) espinner.getValue(), eventDetailField.getText());
                        ArrayList<String> eventMembers = new ArrayList<>(members.getSelectedValuesList());
                        int r2 = EventMemberAPI.updateEventMember(event.getEventId(), eventMembers);
                        if (r1 > 0 && r2 > 0) {
                            JOptionPane.showMessageDialog(null, "일정 수정 성공");

                        } else {
                            JOptionPane.showMessageDialog(null, "일정 수정 실패");
                        }
                    }
                }
            });

            // RSVP 요청버튼
            rsvpRequestBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    JPanel requestRSVPPanel = new JPanel(new GridLayout(2, 2));
                    requestRSVPPanel.add(new JLabel("일정 이름"));
                    requestRSVPPanel.add(new JLabel(event.getEventName()));
                    requestRSVPPanel.add(new JLabel("보낼 유저"));

                    String[] allUsers = UserAPI.getAllUsers();
                    ArrayList<String> userList = new ArrayList<>(Arrays.asList(allUsers));

                    // 멤버들가져와서 포함되어 있으면 뺴자
                    ArrayList<String> members = EventAPI.getMemberNamesByEventId(event.getEventId());
                    userList.removeAll(members);

                    JList<String> users = new JList<>(userList.toArray(new String[0]));
                    users.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                    requestRSVPPanel.add(users);

                    int result = JOptionPane.showConfirmDialog(null, requestRSVPPanel, "RSVP 요청",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                    if (result == JOptionPane.OK_OPTION) {
                        ArrayList<String> selectedUsers = new ArrayList<>(users.getSelectedValuesList());

                        for (String selectedUser : selectedUsers) {
                            //RSVP 만들기
                            int selectedUserId = UserAPI.getUserIdByuserName(selectedUser);
                            RSVP newRSVP = RSVPAPI.registerRSVP(event.getEventId(), user.getUserId(), selectedUserId, new Date());
                            if (newRSVP == null) {
                                JOptionPane.showMessageDialog(null, "rsvp 요청에 실패했습니다.",
                                        "오류", JOptionPane.ERROR_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(null, selectedUser + "에게 RSVP 요청을 전송했습니다.");

                            }
                        }

                    }
                }
            });


        } else {
            JOptionPane.showMessageDialog(null, "이벤트 불러오기 실패\n다시 시도해 주세요");
            eventDetailFrame.dispose();
        }
    }
}
