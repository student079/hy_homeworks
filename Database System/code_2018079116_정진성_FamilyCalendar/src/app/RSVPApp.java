package app;

import domain.event.Event;
import domain.event.EventAPI;
import domain.rsvp.RSVP;
import domain.rsvp.RSVPAPI;
import domain.user.User;
import domain.user.UserAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

public class RSVPApp {
    public RSVPApp(Event event, User user, RSVP rsvp) {
        JFrame eventDetailFrame = new JFrame(event.getEventName() + "RSVP");

        eventDetailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        eventDetailFrame.setSize(500, 300);
        eventDetailFrame.setLocationRelativeTo(null);
        eventDetailFrame.setLayout(new BorderLayout());

        // 수락, 거절 버튼
        JPanel floorPanel = new JPanel(new GridLayout(1,2));
        JButton okBtn = new JButton("수락");
        JButton noBtn = new JButton("거절");
        floorPanel.add(okBtn);
        floorPanel.add(noBtn);

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

        // 수락버튼
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // RSVP true reply
                RSVPAPI.updateStatusByrsvpId(rsvp.getRSVPId(), 1);
                JOptionPane.showMessageDialog(null,UserAPI.getUserNameByUserId(rsvp.getHostUserId())+"님의 "+
                        event.getEventName() + " 일정이 수락되었습니다.");

            }
        });
        // 거절버튼
        noBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //rsvp 삭제
                // 삭제말고 -1
                RSVPAPI.deleteRSVPByRSVPId(rsvp.getRSVPId());
                JOptionPane.showMessageDialog(null,UserAPI.getUserNameByUserId(rsvp.getHostUserId())+"님의 "+
                        event.getEventName() + " 일정이 거절되었습니다.");
            }
        });

        eventDetailFrame.add(bodyPanel, BorderLayout.CENTER);
        eventDetailFrame.add(floorPanel, BorderLayout.SOUTH);
        eventDetailFrame.setVisible(true);

    }
}
