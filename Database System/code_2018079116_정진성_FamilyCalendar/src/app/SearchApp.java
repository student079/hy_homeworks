package app;

import domain.event.Event;
import domain.event.EventAPI;
import domain.user.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class SearchApp {
    public SearchApp(User user) {
        JFrame searchFrame = new JFrame("검색");
        searchFrame.setLayout(new GridLayout(3, 2));
        searchFrame.setLocationRelativeTo(null);
        searchFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        searchFrame.setSize(500, 300);

        searchFrame.add(new JLabel("일정이름"));
        JTextField eventNameField = new JTextField();
        searchFrame.add(eventNameField);

        searchFrame.add(new JLabel("일정 설명"));
        JTextField eventDetailField = new JTextField();
        searchFrame.add(eventDetailField);

        JButton searchBtn = new JButton("검색");
        JButton cancelBtn = new JButton("취소");

        searchFrame.add(searchBtn);
        searchFrame.add(cancelBtn);

        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JPanel resultPanel = new JPanel();

                ArrayList<Event> eventArrayList = EventAPI.getEventListByEventNameOrEventDetail(eventNameField.getText(),
                        eventDetailField.getText(), user.getUserId());

                if (eventArrayList == null) {
                    JOptionPane.showMessageDialog(null, "일치하는 일정이 없습니다.", "결과", JOptionPane.ERROR_MESSAGE);

                } else {
                    JList<Event> jList = new JList<>(eventArrayList.toArray(new Event[0]));
                    resultPanel.add(jList);
                    JScrollPane jScrollPane = new JScrollPane(resultPanel);

                    jList.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 2) {
                                new EventDetailApp(jList.getSelectedValue().getEventName(), user);
                            }
                        }
                    });

                    JOptionPane.showMessageDialog(null, jScrollPane, "결과", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                searchFrame.dispose();
            }
        });

        searchFrame.setVisible(true);


    }
}
