package domain.reminder;

import java.util.Date;

public class Reminder {
    private int reminderId;
    private int eventId;

    private Date reminderStartTime;
    private int interval;

    public int getReminderId() {
        return reminderId;
    }

    public int getEventId() {
        return eventId;
    }

    public Date getreminderStartTime() {
        return reminderStartTime;
    }

    public int getInterval() {
        return interval;
    }

    public Reminder(int reminderId, int eventId, Date reminderStartTime, int interval) {
        this.reminderId = reminderId;
        this.eventId = eventId;
        this.reminderStartTime = reminderStartTime;
        this.interval = interval;
    }
}
