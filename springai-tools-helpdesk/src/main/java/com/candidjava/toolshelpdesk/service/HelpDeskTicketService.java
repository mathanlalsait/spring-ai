package com.candidjava.toolshelpdesk.service;

import com.candidjava.toolshelpdesk.entity.HelpDeskTicket;
import com.candidjava.toolshelpdesk.model.TicketRequest;
import com.candidjava.toolshelpdesk.repository.HelpDeskTicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HelpDeskTicketService {

    private final HelpDeskTicketRepository helpDeskTicketRepository;

    public HelpDeskTicketService(HelpDeskTicketRepository helpDeskTicketRepository) {
        this.helpDeskTicketRepository = helpDeskTicketRepository;
    }

    public HelpDeskTicket createTicket(TicketRequest ticketInput, String username) {
        HelpDeskTicket ticket = new HelpDeskTicket(
                username,
                ticketInput.issue(),
                "OPEN",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7));
        return helpDeskTicketRepository.save(ticket);
    }

    public List<HelpDeskTicket> getTicketsByUsername(String username) {
        return helpDeskTicketRepository.findByUsername(username);
    }
}
