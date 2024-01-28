package app;

import domain.user.User;
import domain.user.UserAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignupApp {
    public SignupApp() {

        JFrame jFrame = new JFrame("Family Calendar - 회원가입");
        jFrame.setSize(500, 300);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.setLayout(new BorderLayout());

        // 이름,아이디,  이메일, 패스워드, noti 채널 입력받기
        // 메인 패널
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(6,2));

        // 이름
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new GridLayout(1,2));
        JLabel nameLabel = new JLabel("이름");
        JTextField nameField = new JTextField();
        namePanel.add(nameLabel);
        namePanel.add(nameField);

        // 아이디
        JPanel idPanel = new JPanel();
        idPanel.setLayout(new GridLayout(1,2));
        JLabel idLabel = new JLabel("아이디");
        JTextField idField = new JTextField();
        idPanel.add(idLabel);
        idPanel.add(idField);

        // 이메일
        JPanel emailPanel = new JPanel();
        emailPanel.setLayout(new GridLayout(1,2));
        JLabel emailLabel = new JLabel("이메일");
        JTextField emailField = new JTextField();
        emailPanel.add(emailLabel);
        emailPanel.add(emailField);

        // 패스워드
        JPanel pwPanel = new JPanel();
        pwPanel.setLayout(new GridLayout(1,2));
        JLabel pwLabel = new JLabel("패스워드");
        JPasswordField pwField = new JPasswordField();
        pwPanel.add(pwLabel);
        pwPanel.add(pwField);

        // noti 채널
        JPanel chPanel = new JPanel();
        chPanel.setLayout(new GridLayout(1, 2));
        JLabel chLabel = new JLabel("Notification 채널");
        JRadioButton chBtn = new JRadioButton("Pop-up message");
        chPanel.add(chLabel);
        chPanel.add(chBtn);

        // 뒤로가기 버튼, 등록 버튼
        JPanel btnsPanel = new JPanel();
        btnsPanel.setLayout(new GridLayout(1, 2));
        JButton backBtn = new JButton("뒤로가기");
        JButton registerBtn = new JButton("등록");
        btnsPanel.add(backBtn);
        btnsPanel.add(registerBtn);

        mainPanel.add(namePanel);
        mainPanel.add(idPanel);
        mainPanel.add(pwPanel);
        mainPanel.add(emailPanel);
        mainPanel.add(chPanel);
        mainPanel.add(btnsPanel);
        jFrame.add(mainPanel);

        // 뒤로가기 버튼 클릭시
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jFrame.dispose();
            }
        });

        // 등록 버튼 클릭시
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // 유저 db 등록

                // 빈 문자열 체크
                if (nameField.getText().isEmpty() || idField.getText().isEmpty() ||
                        String.valueOf(pwField.getPassword()).isEmpty() || emailField.getText().isEmpty()
                || !chBtn.isSelected()) {
                    JOptionPane.showMessageDialog(jFrame, "모든 필드를 입력해주세요");
                } else {

                    String name = nameField.getText();
                    if (!UserAPI.isUserAlreadyExists(name)) {
                        User user = UserAPI.registerUser(name, idField.getText(), String.valueOf(pwField.getPassword()), emailField.getText(), 1);
                        if ( user != null) {
                            JOptionPane.showMessageDialog(jFrame, "회원가입 성공!");
                            jFrame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(jFrame, "회원가입 실패! 입력을 다시한번 확인해주세요");
                        }
                    } else {
                        // 사용자 이미 존재 메시지
                        JOptionPane.showMessageDialog(jFrame, "동일한 이름의 사용자가 존재합니다.");
                    }
                }
            }
        });

        jFrame.setVisible(true);
    }

}
