package app;

import domain.user.User;
import domain.user.UserAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginApp{
    public LoginApp () {

        JFrame jFrame = new JFrame("Family Calendar - 로그인");
        jFrame.setSize(500, 300);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.setLayout(new BorderLayout());

        // 패스워드, 이름, 아이디 입력받기
        // 메인 패널
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(4,2));

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

        // 패스워드
        JPanel pwPanel = new JPanel();
        pwPanel.setLayout(new GridLayout(1,2));
        JLabel pwLabel = new JLabel("패스워드");
        JPasswordField pwField = new JPasswordField();
        pwPanel.add(pwLabel);
        pwPanel.add(pwField);

        // 회원가입, 로그인 버튼
        JPanel btnsPanel = new JPanel();
        btnsPanel.setLayout(new GridLayout(1, 2));
        JButton signupBtn = new JButton("회원가입");
        JButton loginBtn = new JButton("로그인");
        btnsPanel.add(signupBtn);
        btnsPanel.add(loginBtn);

        mainPanel.add(namePanel);
        mainPanel.add(idPanel);
        mainPanel.add(pwPanel);
        mainPanel.add(btnsPanel);
        jFrame.add(mainPanel);

        // 회원가입 버튼 클릭시
        signupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new SignupApp();
            }
        });

        // 로그인 버튼 클릭시
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // 사용자 인증
                // 성공시 로그인 창 닫고 캘린더 앱(monthly) 실행
                User user = UserAPI.isUserExists(nameField.getText(), idField.getText(), String.valueOf(pwField.getPassword()));
                if (user != null) {
                    JOptionPane.showMessageDialog(jFrame,"로그인 성공!");
                    jFrame.dispose();
                    new CalendarApp(user);
                } else {
                    JOptionPane.showMessageDialog(jFrame,"로그인 실패!\n 다시 입력해 주세요");
                }

            }
        });

        jFrame.setVisible(true);
    }

}
