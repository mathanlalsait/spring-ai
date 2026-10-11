package com.candidjava.toolshelpdesk.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "helpdesk_tickets")
public class HelpDeskTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String issue;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime eta;

    public HelpDeskTicket() {
    }

    public HelpDeskTicket(String username, String issue, String status, LocalDateTime createdAt, LocalDateTime eta) {
        this.username = username;
        this.issue = issue;
        this.status = status;
        this.createdAt = createdAt;
        this.eta = eta;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getEta() {
        return eta;
    }

    public void setEta(LocalDateTime eta) {
        this.eta = eta;
    }

    @Override
    public String toString() {
        return "HelpDeskTicket{id=" + id + ", username='" + username + "', issue='" + issue
                + "', status='" + status + "', eta=" + eta + '}';
    }
}
