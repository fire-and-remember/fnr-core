package io.fnr.core.domain;

public class Ticket<T> {
    private final String ticketId;
    private final String jobName;

    public Ticket(String ticketId, String jobName) {
        this.ticketId = ticketId;
        this.jobName = jobName;
    }

    public String getTicketId() { return ticketId; }
    public String getJobName()  { return jobName; }
}
