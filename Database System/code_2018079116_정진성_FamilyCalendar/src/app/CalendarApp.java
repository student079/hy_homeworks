package app;

import domain.event.Event;
import domain.event.EventAPI;
import domain.eventMember.EventMemberAPI;
import domain.reminder.Reminder;
import domain.reminder.ReminderAPI;
import domain.rsvp.HostRSVP;
import domain.rsvp.RSVP;
import domain.rsvp.RSVPAPI;
import domain.user.User;
import domain.user.UserAPI;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class CalendarApp {
    private LocalDate currentDate;
    private JLabel yearMonth;

    private static Date convertTime(Object time) {
        if (time instanceof Date) {
            return new Date((((Date)time).getTime() / 1000) * 1000);
        }
        return null;
    }

    private static void checkReminder(User user) {
        Date cTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        // 리마인더 리스트 보여줄때 처럼 리스트 만들고
        // 1문마다 시간체크하고
        // start시간이랑 겹치면 알림 보내고
        // 간격 + 해서 시작시간보다 작으면 알림 더 설정
        ArrayList<Integer> eventIdList = EventMemberAPI.getEventIdListByUserId(user.getUserId());
        ArrayList<Reminder> reminderList = new ArrayList<>();

        for (int eventId : eventIdList) {
            reminderList.add(ReminderAPI.getReminderByEventId(eventId));
        }

        // 이벤트의 시작시간 확인해서 이미 지났으면 리마인더 삭제
        for (Reminder reminder : reminderList) {
            if (reminder == null) {
                continue;
            }
            Event reminderEvent = EventAPI.getEventByEventId(reminder.getEventId());
            if (reminderEvent == null) {
                continue;
            }

            if (reminderEvent.getEventSTime().getTime() < cTime.getTime()) {
                ReminderAPI.deleteReminderByReminderId(reminder.getReminderId());
                continue;
            }

            if (sdf.format(reminder.getreminderStartTime()).equals(sdf.format(cTime))) {
                // 팝업 메시지 표시하고
                JOptionPane.showMessageDialog(null, "Lunch at " + reminderEvent.getEventName() + " is starting at " +
                        sdf.format(reminderEvent.getEventSTime()), "Event reminder",JOptionPane.INFORMATION_MESSAGE );
                // 간격+해서 이벤트 시작시간보다 작은지 비교해서
                Date calculatedTime = new Date(cTime.getTime() + 60 * 1000 * reminder.getInterval());
                int i = 0;
                while (calculatedTime.getTime() < reminderEvent.getEventSTime().getTime()) {
                    i += 1;
                    calculatedTime = new Date(calculatedTime.getTime() + 60 * 1000 * reminder.getInterval());
                    Timer timer = new Timer(60 * 1000 * reminder.getInterval() * i, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            JOptionPane.showMessageDialog(null, "Lunch at " + reminderEvent.getEventName() + " is starting at " +
                                    sdf.format(reminderEvent.getEventSTime()), "Event reminder",JOptionPane.INFORMATION_MESSAGE );
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        }
    }
    public CalendarApp(User user) {

        // 일단 리마인더 확인
        checkReminder(user);

        Timer timer = new Timer(60000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkReminder(user);
            }
        });
        timer.start();

        JFrame jFrame = new JFrame("Family Calendar");
        jFrame.setSize(1200, 1000);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.setLayout(new BorderLayout());

        currentDate = LocalDate.now();

        //메뉴바 <,>, 년도 월, 검색버튼, 라디오 버튼(월,주,일), 일정 생성 버튼, 회원정보 수정 버튼
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout());
        // 이전 달 버튼
        JButton prevBtn = new JButton("<");
        // 다음 달 버튼
        JButton nextBtn = new JButton(">");
        // 년도, 월
        yearMonth = new JLabel(String.valueOf(currentDate.getYear()) + " " + String.valueOf(currentDate.getMonth().getValue()));
        //검색 버튼
        JButton searchBtn = new JButton("검색");
        // 달력 형식 선택
        ButtonGroup group = new ButtonGroup();
        JRadioButton mBtn = new JRadioButton("월",true);
        JRadioButton wBtn = new JRadioButton("주");
        JRadioButton dBtn = new JRadioButton("일");
        group.add(mBtn);
        group.add(wBtn);
        group.add(dBtn);
        // 일정 추가 버튼
        JButton addBtn = new JButton("일정 추가");
        // 참석자 확인 버튼
        JButton checkBtn = new JButton("참석자 확인");
        // RSVP
        JButton rsvpBtn = new JButton("RSVP");
        // 회원정보 수정 버튼
        JButton updateBtn = new JButton("회원정보수정");
        // Reminder
        JButton reminderBtn = new JButton("Reminder List");
        // 새로고침
        JButton refreshBtn = new JButton("Refresh");

        headerPanel.add(prevBtn);
        headerPanel.add(nextBtn);
        headerPanel.add(yearMonth);
        headerPanel.add(searchBtn);
        headerPanel.add(mBtn);
        headerPanel.add(wBtn);
        headerPanel.add(dBtn);
        headerPanel.add(addBtn);
        headerPanel.add(checkBtn);
        headerPanel.add(rsvpBtn);
        headerPanel.add(updateBtn);
        headerPanel.add(reminderBtn);
        headerPanel.add(refreshBtn);


        //캘린더
        JPanel calendarPanel = new JPanel();
        calendarPanel.setLayout(new GridLayout(0,7));

        showMonthlyCalendar(calendarPanel, user);
        jFrame.add(headerPanel, BorderLayout.NORTH);
        jFrame.add(calendarPanel, BorderLayout.CENTER);

        // "<"버튼 클릭시
        prevBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                if (mBtn.isSelected()) {
                    currentDate = currentDate.minusMonths(1);
                    showMonthlyCalendar(calendarPanel, user);
                } else if (wBtn.isSelected()) {
                    currentDate = currentDate.minusWeeks(1);
                    showWeeklyCalendar(calendarPanel, user);
                } else if (dBtn.isSelected()) {
                    currentDate = currentDate.minusDays(1);
                    showDailyCalendar(calendarPanel, user);
                }
            }
        });

        // ">"버튼 클릭시
        nextBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (mBtn.isSelected()) {
                    currentDate = currentDate.plusMonths(1);
                    showMonthlyCalendar(calendarPanel, user);
                } else if (wBtn.isSelected()) {
                    currentDate = currentDate.plusWeeks(1);
                    showWeeklyCalendar(calendarPanel, user);
                } else if (dBtn.isSelected()) {
                    currentDate = currentDate.plusDays(1);
                    showDailyCalendar(calendarPanel, user);
                }
            }
        });

        // 달력 타입 선택
        mBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                currentDate = LocalDate.now();
                showMonthlyCalendar(calendarPanel, user);
            }
        });
        wBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                currentDate = LocalDate.now();
                showWeeklyCalendar(calendarPanel, user);
            }
        });
        dBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                currentDate = LocalDate.now();
                showDailyCalendar(calendarPanel, user);
            }
        });

        updateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                User oldUser = UserAPI.getUserByuserId(user.getUserId());

                JTextField usernameField = new JTextField(oldUser.getUsername());
                JTextField idField = new JTextField(oldUser.getId());
                JPasswordField passwordField = new JPasswordField(oldUser.getPassword());
                JTextField emailField = new JTextField(oldUser.getEmail());
                JRadioButton chBtn = new JRadioButton("Pop-up message");
                chBtn.setSelected(oldUser.getChannel() == 1);

                JPanel panel = new JPanel(new GridLayout(5, 2));
                panel.add(new JLabel("새로운 사용자 이름:"));
                panel.add(usernameField);
                panel.add(new JLabel("새로운 아이디:"));
                panel.add(idField);
                panel.add(new JLabel("새로운 비밀번호:"));
                panel.add(passwordField);
                panel.add(new JLabel("새로운 이메일:"));
                panel.add(emailField);
                panel.add(new JLabel("새로운 Notification 채널:"));
                panel.add(chBtn);

                int result = JOptionPane.showConfirmDialog(null, panel, "회원정보 수정",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String newName = usernameField.getText();
                    String newId = idField.getText();
                    String newPw = String.valueOf(passwordField.getPassword());
                    String newEmail = emailField.getText();
                    boolean newCh = chBtn.isSelected();

                    if (newName.isEmpty() || newId.isEmpty() || newPw.isEmpty()
                            || newEmail.isEmpty() || !newCh) {
                        JOptionPane.showMessageDialog(null, "모든 필드를 입력해주세요");
                    } else {
                        User newUser = UserAPI.updateUser(oldUser.getUserId(), newName, newId, newPw, newEmail, 1);
                        if (newUser != null) {
                            JOptionPane.showMessageDialog(null, "회원정보가 업데이트 되었습니다.");
                        } else {
                            JOptionPane.showMessageDialog(null, "회원정보 업데이트에 실패했습니다.",
                                    "오류", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        // 검색 버튼 클릭시
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new SearchApp(user);
            }
        });

        // 리마인더 리스트 버튼 클릭시
        reminderBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFrame reminderListFrame = new JFrame("reminderList");

                reminderListFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                reminderListFrame.setSize(500, 300);
                reminderListFrame.setLocationRelativeTo(null);
                reminderListFrame.setLayout(new BorderLayout());

                JPanel headerPanel = new JPanel(new GridLayout(0, 3));
                headerPanel.add(new JLabel("일정 이름"));
                headerPanel.add(new JLabel("reminder 시작 시간"));
                headerPanel.add(new JLabel("reminder Interval"));
                reminderListFrame.add(headerPanel, BorderLayout.NORTH);

                // 자신이 참여하는 이벤트 중 리마인더의 starttime이 현재의 1시간 이내인 것들
                JPanel reminderPanel = new JPanel(new GridLayout(0, 3));
                // 자신이 참여하는 이벤트 중
                ArrayList<Integer> eventIdList = EventMemberAPI.getEventIdListByUserId(user.getUserId());
                ArrayList<Reminder> reminderList = new ArrayList<>();
                for (int eventId : eventIdList) {
                    // 리마인더가 설정된 (리마인더 테이블까지 보고 interval이 0이 아닌) 것 중에 reminderstarttime이 현재의 1시간 이내인 리마인더
                    reminderList.add(ReminderAPI.getReminderByEventIdAndDate(eventId, new Date()));
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                for (Reminder reminder : reminderList) {
                    if (reminder != null) {
                        reminderPanel.add(new JLabel(EventAPI.getEventByEventId(reminder.getEventId()).getEventName()));
                        reminderPanel.add(new JLabel(dateFormat.format(reminder.getreminderStartTime())));
                        reminderPanel.add(new JLabel(String.valueOf(reminder.getInterval()) + "분 간격"));
                    }
                }

                JScrollPane jScrollPane = new JScrollPane(reminderPanel);
                reminderListFrame.add(jScrollPane);
                reminderListFrame.setVisible(true);

            }
        });

        //참석자 확인 버튼 클릭시
        checkBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JPanel checkPanel = new JPanel(new GridLayout(2, 2));
                checkPanel.add(new JLabel("시작 시간"));
                SpinnerModel smodel = new SpinnerDateModel();
                JSpinner sspinner = new JSpinner(smodel);
                JSpinner.DateEditor seditor = new JSpinner.DateEditor(sspinner, "yyyy-MM-dd HH:mm");
                sspinner.setEditor(seditor);
                checkPanel.add(sspinner);

                checkPanel.add(new JLabel("끝나는 시간"));
                SpinnerModel emodel = new SpinnerDateModel();
                JSpinner espinner = new JSpinner(emodel);
                JSpinner.DateEditor eeditor = new JSpinner.DateEditor(espinner, "yyyy-MM-dd HH:mm");
                espinner.setEditor(eeditor);
                checkPanel.add(espinner);

                int result = JOptionPane.showConfirmDialog(null, checkPanel, "참석자 확인",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    // 시간 겹치는 일정을 가져온다음에
                    ArrayList<Integer> eventIdList = EventAPI.getEventIdsBytime((Date)sspinner.getValue(), (Date)espinner.getValue());
                    HashSet<String> noMembers = new HashSet<>();
                    for (int eventId : eventIdList) {
                        noMembers.addAll(EventAPI.getMemberNamesByEventId(eventId));
                    }
                    ArrayList<String> yesMembers = new ArrayList<>(Arrays.asList(UserAPI.getAllUsers()));

                    for (String noUser : noMembers) {
                        yesMembers.remove(noUser);
                    }

                    JFrame checkFrame = new JFrame("참석자 확인");
                    checkFrame.setSize(500, 300);
                    checkFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    checkFrame.setLocationRelativeTo(null);
                    checkFrame.setLayout(new GridLayout(2,1));


                    JPanel yesPanel = new JPanel(new BorderLayout());
                    yesPanel.add(new JLabel("가능한 참석자"), BorderLayout.NORTH);
                    JPanel yesValuePanel = new JPanel();
                    for (String name : yesMembers) {
                        yesValuePanel.add(new JLabel(name));
                    }
                    JScrollPane scrollPane = new JScrollPane(yesValuePanel);
                    yesPanel.add(scrollPane);

                    JPanel noPanel = new JPanel(new BorderLayout());
                    noPanel.add(new JLabel("불가능한 참석자"), BorderLayout.NORTH);
                    JPanel noValuePanel = new JPanel();
                    for (String name : noMembers) {
                        noValuePanel.add(new JLabel(name));
                    }
                    JScrollPane scrollPane2 = new JScrollPane(noValuePanel);
                    noPanel.add(scrollPane2);

                    checkFrame.add(yesPanel);
                    checkFrame.add(noPanel);

                    checkFrame.setVisible(true);
                }
            }
        });

        // RSVP 버튼 클릭시
        rsvpBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFrame rsvpFrame = new JFrame("RSVP");
                rsvpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                rsvpFrame.setSize(500, 300);
                rsvpFrame.setLocationRelativeTo(null);
                rsvpFrame.setLayout(new BorderLayout());

                JPanel headerPanel = new JPanel(new GridLayout(1, 2));
                headerPanel.add(new Label("Received RSVP"));
                headerPanel.add(new Label("RSVP Requests"));
                rsvpFrame.add(headerPanel, BorderLayout.NORTH);

                JPanel bodyPanel = new JPanel(new GridLayout(1, 2));
                ArrayList<RSVP> recRSVPList = RSVPAPI.getRSVPListByGuestUserIdAndStatus(user.getUserId(), 0);
                ArrayList<RSVP> RSVPListIn10 = new ArrayList<>();
                Date cDate = new Date();
                for (RSVP rsvp : recRSVPList) {
                    // 현재 시간과의 차이가 10분 이내인지
                    if ((cDate.getTime() - rsvp.getRequestTime().getTime()) / (60 * 1000) <= 10) {
                        RSVPListIn10.add(rsvp);
                    } else {
                        // rsvp객체 상태 업데이트
                        RSVPAPI.updateStatusByrsvpId(rsvp.getRSVPId(), -1);
                    }
                }
                JList<RSVP> recRSVP = new JList<>(RSVPListIn10.toArray(new RSVP[0]));
                // 보낸거 확인할 때 일단 RSVP 다 가져오고
                // 상태가 0 인것중 현재시간차이 10분이상인건 -1로 바꿔
                ArrayList<HostRSVP> reqRSVPList = RSVPAPI.getRSVPListByHostUserId(user.getUserId());
                for (HostRSVP rsvp : reqRSVPList) {
                    // 현재 시간과의 차이가 10분 이내인지
                    if (!((cDate.getTime() - rsvp.getRequestTime().getTime()) / (60 * 1000) <= 10)) {
                        // rsvp객체 상태 업데이트
                        RSVPAPI.updateStatusByrsvpId(rsvp.getRSVPId(), -1);
                        rsvp.setStatus(-1);
                    }
                }
                JList<HostRSVP> reqRSVP = new JList<>(reqRSVPList.toArray(new HostRSVP[0]));

                recRSVP.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            JFrame checkFrame = new JFrame("RSVP");
                            checkFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            checkFrame.setSize(300, 150);
                            checkFrame.setLocationRelativeTo(null);
                            checkFrame.setLayout(new BorderLayout());

                            JPanel bodyPanel = new JPanel(new GridLayout(3, 2));
                            bodyPanel.add(new JLabel("일정 이름"));
                            bodyPanel.add(new JLabel(EventAPI.getEventByEventId(recRSVP.getSelectedValue().getEventId()).getEventName()));
                            bodyPanel.add(new JLabel("일정 시작"));
                            bodyPanel.add(new JLabel(EventAPI.getEventByEventId(recRSVP.getSelectedValue().getEventId()).getEventSTime().toString()));
                            bodyPanel.add(new JLabel("일정 끝"));
                            bodyPanel.add(new JLabel(EventAPI.getEventByEventId(recRSVP.getSelectedValue().getEventId()).getEventETime().toString()));
                            checkFrame.add(bodyPanel, BorderLayout.CENTER);

                            JPanel btns = new JPanel(new GridLayout(1, 2));
                            JButton okBtn = new JButton("수락");
                            JButton noBtn = new JButton("거절");

                            // 수락
                            okBtn.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent actionEvent) {
                                    RSVPAPI.updateStatusByrsvpId(recRSVP.getSelectedValue().getRSVPId(), 1);
                                    JOptionPane.showMessageDialog(null,"수락하였습니다.");
                                }
                            });

                            // 거절
                            noBtn.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent actionEvent) {
                                    RSVPAPI.updateStatusByrsvpId(recRSVP.getSelectedValue().getRSVPId(), -1);
                                    JOptionPane.showMessageDialog(null,"거절하였습니다.");
                                }
                            });

                            btns.add(okBtn);
                            btns.add(noBtn);
                            checkFrame.add(btns, BorderLayout.SOUTH);

                            checkFrame.setVisible(true);
                        }
                    }
                });

                reqRSVP.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            String message = "";
                            int status = RSVPAPI.getStatusByRsvpId(reqRSVP.getSelectedValue().getRSVPId());
                            if (status == 1) {
                                JOptionPane.showMessageDialog(null, "수락 상태");
                            } else if (status == -1) {
                                JOptionPane.showMessageDialog(null, "거절 상태");
                            } else if (status == 0) {
                                JOptionPane.showMessageDialog(null, "보류 상태");
                            }

                        }
                    }
                });

                JScrollPane rec = new JScrollPane(recRSVP);
                JScrollPane req = new JScrollPane(reqRSVP);
                bodyPanel.add(rec);
                bodyPanel.add(req);
                rsvpFrame.add(bodyPanel, BorderLayout.CENTER);

                rsvpFrame.setVisible(true);

            }
        });

        // 일정 추가 버튼 클릭시
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                JPanel addPanel = new JPanel(new GridLayout(7, 2));
                addPanel.add(new JLabel("일정 이름"));
                JTextField eventNameField = new JTextField();
                addPanel.add(eventNameField);

                addPanel.add(new JLabel("참석자(아무도 선택하지않으면 개인일정)"));
                String[] members = UserAPI.getAllUsers();
                ArrayList<String> memberList = new ArrayList<>();
                for (String member : members) {
                    if (!member.equals(user.getUsername())) {
                        memberList.add(member);
                    }
                }
                JList<String> jList = new JList<>(memberList.toArray(new String[0]));
                jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // 다중 선택 모드
                jList.setVisibleRowCount(4);
                JScrollPane scrollPane = new JScrollPane(jList);
                addPanel.add(scrollPane);

                addPanel.add(new JLabel("시작 시간"));
                SpinnerModel smodel = new SpinnerDateModel();
                JSpinner sspinner = new JSpinner(smodel);
                JSpinner.DateEditor seditor = new JSpinner.DateEditor(sspinner, "yyyy-MM-dd HH:mm");
                sspinner.setEditor(seditor);
                addPanel.add(sspinner);

                addPanel.add(new JLabel("끝나는 시간"));
                SpinnerModel emodel = new SpinnerDateModel();
                JSpinner espinner = new JSpinner(emodel);
                JSpinner.DateEditor eeditor = new JSpinner.DateEditor(espinner, "yyyy-MM-dd HH:mm");
                espinner.setEditor(eeditor);
                addPanel.add(espinner);

                addPanel.add(new JLabel("일정 설명"));
                JTextField eventDetailField = new JTextField();
                addPanel.add(eventDetailField);

                addPanel.add(new JLabel("Reminder Interval"));
                JPanel btnPanel = new JPanel();
                ButtonGroup intervalBtns = new ButtonGroup();
                JRadioButton btn0 = new JRadioButton("0", true);
                JRadioButton btn15 = new JRadioButton("15");
                JRadioButton btn30 = new JRadioButton("30");
                JRadioButton btn45 = new JRadioButton("45");
                JRadioButton btn60 = new JRadioButton("60");
                intervalBtns.add(btn0);
                intervalBtns.add(btn15);
                intervalBtns.add(btn30);
                intervalBtns.add(btn45);
                intervalBtns.add(btn60);
                btnPanel.add(btn0);
                btnPanel.add(btn15);
                btnPanel.add(btn30);
                btnPanel.add(btn45);
                btnPanel.add(btn60);
                addPanel.add(btnPanel);

                addPanel.add(new JLabel("Reminder Time(15~60)"));
                JTextField reminderTimeField = new JTextField();
                addPanel.add(reminderTimeField);
                if (btn0.isSelected()) {
                    reminderTimeField.setEnabled(false);
                }

                btn0.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        reminderTimeField.setEnabled(false);
                    }
                });
                btn15.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        reminderTimeField.setEnabled(true);
                    }
                });
                btn30.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        reminderTimeField.setEnabled(true);
                    }
                });
                btn45.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        reminderTimeField.setEnabled(true);
                    }
                });
                btn60.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        reminderTimeField.setEnabled(true);
                    }
                });

                int result = JOptionPane.showConfirmDialog(null, addPanel, "일정 추가",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {

                    // 시간 유효성 체크 해야함
                    // 나중시간이 더 뒤인지
                    // 참석자들 가능한지 시간
                    // reminder interval이 0이 아닌데 time이 15~60 사이의 값 맞는지

                    String eventName = eventNameField.getText();
                    if (eventName.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "일정 이름을 입력하세요\n 다시 입력해주세요");
                        return;
                    }

                    // 모든 이벤트 가져와서 중복있는지 확인
                    ArrayList<String> allEventNames = EventAPI.getAllEventNames();
                    if (allEventNames.contains(eventName)) {
                        JOptionPane.showMessageDialog(null, "이미 있는 일정 이름입니다.\n 다시 입력해주세요");
                        return;
                    }
                    ArrayList<String> eventMembers = new ArrayList<>(jList.getSelectedValuesList());
                    eventMembers.add(user.getUsername());

                    Object sTime = sspinner.getValue();
                    Date eventSTime = convertTime(sTime);
                    Object eTime = espinner.getValue();
                    Date eventETime = convertTime(eTime);
                    if (eventETime.getTime() < eventSTime.getTime()) {
                        JOptionPane.showMessageDialog(null, "날짜 및 시간 선택이 올바르지 않습니다\n 다시 입력해주세요");
                        return;
                    }

                    // 참석자들 중에 현재 설정된 시간에 이벤트가 있는지 체크
                    for (String member : eventMembers) {
                        for (Event event : UserAPI.getEventListByuserId(UserAPI.getUserIdByuserName(member))) {
                            Date mEventStartTime = event.getEventSTime();
                            Date mEventEndTime = event.getEventETime();

                            if ((eventSTime.getTime() <= mEventEndTime.getTime() && eventSTime.getTime() >= mEventStartTime.getTime()
                            ) ||( eventETime.getTime() <= mEventEndTime.getTime() && eventETime.getTime() >= mEventStartTime.getTime())) {
                                JOptionPane.showMessageDialog(null, member + "의 " + event.getEventName() + "일정으로 인해 설정된 시간이 불가합니다");
                                return;
                            }
                        }
                    }

                    String eventDetail = eventDetailField.getText();
                    int reminderInterval = 0;
                    int reminderTime = 0;
                    if (btn0.isSelected()) {
                        reminderInterval = 0;
                        reminderTime = 0;
                    } else {
                        reminderTime = Integer.parseInt(reminderTimeField.getText());
                        if (reminderTime < 15 || reminderTime > 60) {
                            JOptionPane.showMessageDialog(null, "reminder time의 입력이 올바르지 않습니다\n 15~60의 값으로 다시 입력해주세요");
                            return ;
                        }
                        if (btn15.isSelected()) {
                            reminderInterval = 15;
                        } else if (btn30.isSelected()) {
                            reminderInterval = 30;
                        } else if (btn45.isSelected()) {
                            reminderInterval = 45;
                        } else {
                            reminderInterval = 60;
                        }
                    }
                    // db 등록
                    // event 추가
                    Event newEvent = EventAPI.registerEvent(eventName, eventSTime, eventETime, eventDetail, user.getUserId());
                    if (newEvent != null) {
                        JOptionPane.showMessageDialog(null, newEvent.getEventName() + "이 추가되었습니다");
                        showMonthlyCalendar(calendarPanel, user);

                        // eventmember추가
                        EventMemberAPI.registerMembers(eventMembers, newEvent.getEventId());

                        if (reminderInterval != 0) {
                            // reminderList에 추가
                            Date startTime = newEvent.getEventSTime();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(startTime);
                            calendar.add(Calendar.MINUTE, reminderTime * -1);
                            Reminder newReminder = ReminderAPI.registerReminder(newEvent.getEventId(), calendar.getTime(), reminderInterval);
                            if (newReminder != null) {
                                JOptionPane.showMessageDialog(null, newEvent.getEventName() + " reminder가 추가되었습니다");
                            }
                            else {
                                JOptionPane.showMessageDialog(null, "Reminder 등록 실패\n다시 시도해 주세요");
                            }
                        }
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "이벤트 등록 실패\n다시 시도해 주세요");
                    }
                }
            }
        });

        // refresh 버튼
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (mBtn.isSelected()) {
                    showMonthlyCalendar(calendarPanel, user);
                } else if (wBtn.isSelected()) {
                    showWeeklyCalendar(calendarPanel, user);
                } else if (dBtn.isSelected()) {
                    showDailyCalendar(calendarPanel, user);
                }
            }
        });

        jFrame.setVisible(true);
    }

    private void showMonthlyCalendar(JPanel calendarPanel, User user) {
        calendarPanel.removeAll();
        calendarPanel.setLayout(new GridLayout(0,7));

        String[] daysOfWeek = {"일", "월", "화", "수", "목", "금", "토"};
        for (String day : daysOfWeek) {
            calendarPanel.add(new JLabel(day, SwingConstants.CENTER));
        }

        LocalDate date = currentDate.withDayOfMonth(1);
        int offset = date.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < offset; i++) {
            JLabel emptyLabel = new JLabel("");
            emptyLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            calendarPanel.add(emptyLabel);
        }

        while (date.getMonth().equals(currentDate.getMonth())) {
            JPanel packPanel = new JPanel();
            // 제목, 일정 최대 3개, 더 있으면 ...으로
            packPanel.setLayout(new GridLayout(5,0));
            packPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            JLabel dayLabel = new JLabel(Integer.toString(date.getDayOfMonth()), SwingConstants.CENTER);
            packPanel.add(dayLabel);

            // 날짜 받아서 날짜에 대한 일정 제목 만 최대 3개 추가 4개 째는 ...넣고 무시
            ArrayList<String> eventNameList = EventAPI.getEventNamesByDateAndUserId(java.sql.Date.valueOf(date), user.getUserId());
            int i = 0;
            for (String eventName : eventNameList) {
                i += 1;
                if (i > 3) {
                    JLabel event = new JLabel("...", SwingConstants.CENTER);
                    packPanel.add(event);
                    break;
                }
                JLabel event = new JLabel(eventName, SwingConstants.CENTER);
                packPanel.add(event);
            }

            // 날짜 클릭시
            LocalDate clickedDate = date;
            packPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JFrame eventFrame = new JFrame(clickedDate + " 일정 목록");
                    eventFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    eventFrame.setSize(400, 300);
                    eventFrame.setLocationRelativeTo(null);

                    // 일정 이름을 담은 ArrayList
                    // 참석자에 userId가 포함된 이벤트만
                    ArrayList<String> events = EventAPI.getEventNamesByDateAndUserId(java.sql.Date.valueOf(clickedDate), user.getUserId());

                    // JList에 일정 목록 표시
                    JList<String> eventList = new JList<>(events.toArray(new String[0]));
                    eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    eventList.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            String eventName = eventList.getSelectedValue();
                            if (e.getClickCount() == 2) {
                                // 이벤트 이름으로 이벤트 찾아 넣기
                                new EventDetailApp(eventName, user);
                            }
                        }
                    });

                    JScrollPane scrollPane = new JScrollPane(eventList);

                    eventFrame.add(scrollPane, BorderLayout.CENTER);
                    eventFrame.setVisible(true);
                }
            });

            calendarPanel.add(packPanel);
            date = date.plusDays(1);
        }

        yearMonth.setText(String.valueOf(currentDate.getYear()) + " " + String.valueOf(currentDate.getMonth().getValue()));

        calendarPanel.revalidate();
        calendarPanel.repaint();

    }

    private void showWeeklyCalendar(JPanel calendarPanel, User user) {
        calendarPanel.removeAll();
        calendarPanel.setLayout(new BorderLayout());

        String[] daysOfWeek = {"일", "월", "화", "수", "목", "금", "토"};
        JPanel headerPanel = new JPanel(new GridLayout(0,7));
        for (String day : daysOfWeek) {
            headerPanel.add(new JLabel(day, SwingConstants.CENTER));
        }
        LocalDate startDate = currentDate.with(DayOfWeek.MONDAY).minusDays(1);
        for (int i = 0; i < 7; i++) {
            headerPanel.add(new JLabel(Integer.toString(startDate.getDayOfMonth()), SwingConstants.CENTER));
            startDate = startDate.plusDays(1);
        }
        calendarPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel eventPanel = new JPanel(new GridLayout(0, 7));

        startDate = startDate.minusDays(7);
        // 레이블 세로로 넣기
        for (int i = 0; i < 7; i++) {
            JPanel eventDetailPanel = new JPanel();

            JPanel eventsPanel = new JPanel(new GridLayout(10, 0));
            // 현재 날짜에 해당하는 이벤트 리스트 가져오고
            ArrayList<String> eventList = EventAPI.getEventNamesByDateAndUserId(java.sql.Date.valueOf(startDate.plusDays(i)), user.getUserId());
            for (String event : eventList) {
                JLabel eventLabel = new JLabel(event);
                eventsPanel.add(eventLabel);

                eventLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        new EventDetailApp(event, user);
                    }
                });
            }
            JScrollPane scrollPane = new JScrollPane(eventsPanel);
            eventDetailPanel.add(scrollPane);

            eventsPanel.addMouseListener(new MouseAdapter() {
            });

            eventPanel.add(eventDetailPanel);
            eventPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        }
        calendarPanel.add(eventPanel);

        yearMonth.setText(String.valueOf(currentDate.getYear()) + " " + String.valueOf(currentDate.getMonth().getValue()));

        calendarPanel.revalidate();
        calendarPanel.repaint();

    }

    private void showDailyCalendar(JPanel calendarPanel, User user) {
        calendarPanel.removeAll();
        calendarPanel.setLayout(new BorderLayout());

        JLabel currentDay = new JLabel(String.valueOf(currentDate.getMonth()) + " " + String.valueOf(currentDate.getDayOfMonth()) + " " + String.valueOf(currentDate.getDayOfWeek()), SwingConstants.CENTER);
        calendarPanel.add(currentDay, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel(new GridLayout(10,0));
        ArrayList<String> events = EventAPI.getEventNamesByDateAndUserId(java.sql.Date.valueOf(currentDate), user.getUserId());
        for (String event : events) {
            JLabel name = new JLabel(event, SwingConstants.CENTER);
            bodyPanel.add(name);

            // 클릭이벤트
            name.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    new EventDetailApp(event, user);
                }
            });
        }
        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        calendarPanel.add(scrollPane);

        yearMonth.setText(String.valueOf(currentDate.getYear()) + " " + String.valueOf(currentDate.getMonth().getValue()));

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }
}
