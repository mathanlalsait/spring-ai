package com.candidjava.toolshelpdesk.tools;

import com.candidjava.toolshelpdesk.entity.HelpDeskTicket;
import com.candidjava.toolshelpdesk.model.TicketRequest;
import com.candidjava.toolshelpdesk.service.HelpDeskTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Advanced tools lesson: ToolContext + returnDirect + JPA side effects.
 */
@Component
public class HelpDeskTools {

    private static final Logger log = LoggerFactory.getLogger(HelpDeskTools.class);

    private final HelpDeskTicketService service;

    public HelpDeskTools(HelpDeskTicketService service) {
        this.service = service;
    }

    @Tool(name = "createTicket", description = "Create a support ticket for the current user", returnDirect = true)
    String createTicket(
            @ToolParam(description = "Details required to create a support ticket") TicketRequest ticketRequest,
            ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");
        log.info("TOOL createTicket user={} request={}", username, ticketRequest);
        HelpDeskTicket saved = service.createTicket(ticketRequest, username);
        String result = "Ticket #" + saved.getId() + " created successfully for user " + saved.getUsername()
                + " (status=" + saved.getStatus() + ", eta=" + saved.getEta() + ")";
        log.info("TOOL createTicket result={}", result);
        return result;
    }

    @Tool(name = "getTicketStatus", description = "Fetch the status of tickets for the current user")
    List<HelpDeskTicket> getTicketStatus(ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");
        log.info("TOOL getTicketStatus user={}", username);
        List<HelpDeskTicket> tickets = service.getTicketsByUsername(username);
        log.info("TOOL getTicketStatus found {} tickets for {}", tickets.size(), username);
        return tickets;
    }
}
