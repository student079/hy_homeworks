package domain.event;

import java.util.Date;

public class Event {
    private int eventId;
    private String eventName;
    private Date eventSTime;
    private Date eventETime;
    private String eventDetail;
    private int eventCreatorId;

    public int getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public Date getEventSTime() {
        return eventSTime;
    }

    public Date getEventETime() {
        return eventETime;
    }

    public String getEventDetail() {
        return eventDetail;
    }

    public int getEventCreatorId() {
        return eventCreatorId;
    }

    public Event(int eventId, String eventName, Date eventSTime, Date eventETime, String eventDetail, int eventCreatorId) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventSTime = eventSTime;
        this.eventETime = eventETime;
        this.eventDetail = eventDetail;
        this.eventCreatorId = eventCreatorId;
    }

    @Override
    public String toString() {
        return eventName;
    }
}
