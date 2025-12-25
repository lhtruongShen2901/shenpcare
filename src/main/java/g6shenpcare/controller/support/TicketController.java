package g6shenpcare.controller.support;

import g6shenpcare.entity.UserAccount;
import g6shenpcare.models.entity.ChatSession;
import g6shenpcare.models.entity.Ticket;
import g6shenpcare.models.enums.EPriority;
import g6shenpcare.models.enums.ETicketCategory;
import g6shenpcare.models.enums.ETicketStatus;
import g6shenpcare.repository.UserAccountRepository;
import g6shenpcare.service.ChatService;
import g6shenpcare.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/support/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private ChatService chatService;

    @GetMapping("/api/managers")
    @ResponseBody
    public ResponseEntity<List<UserAccount>> getManagers() {
        List<UserAccount> users = userRepository.findByRoleIn(
                List.of("ADMIN", "SUPPORT", "DOCTOR", "GROOMER")
        );
        return ResponseEntity.ok(users);
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createTicket(
            @RequestParam Long sessionId,
            @RequestParam String category,
            @RequestParam String priority,
            @RequestParam String subject,
            @RequestParam String description,
            @RequestParam(required = false) Integer assignToId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            UserAccount staff = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ETicketCategory ticketCategory = ETicketCategory.valueOf(category);
            EPriority ticketPriority = EPriority.valueOf(priority);

            Ticket ticket = ticketService.createTicket(
                    sessionId,
                    staff.getUserId(),
                    ticketCategory,
                    ticketPriority,
                    subject,
                    description,
                    assignToId
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", ticket.getId());
            response.put("subject", ticket.getSubject());
            response.put("category", ticket.getCategory());
            response.put("priority", ticket.getPriority());
            response.put("status", ticket.getStatus());

            Map<String, Object> creatorInfo = new HashMap<>();
            creatorInfo.put("userId", ticket.getCreatedBy().getUserId());
            creatorInfo.put("username", ticket.getCreatedBy().getUsername());
            creatorInfo.put("fullName", ticket.getCreatedBy().getFullName());
            response.put("createdBy", creatorInfo);

            if (ticket.getAssignedTo() != null) {
                Map<String, Object> assigneeInfo = new HashMap<>();
                assigneeInfo.put("userId", ticket.getAssignedTo().getUserId());
                assigneeInfo.put("username", ticket.getAssignedTo().getUsername());
                assigneeInfo.put("fullName", ticket.getAssignedTo().getFullName());
                response.put("assignedTo", assigneeInfo);
            } else {
                response.put("assignedTo", null);
            }

            response.put("createdAt", ticket.getCreatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public String ticketsList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserAccount staff = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Ticket> myTickets = ticketService.getTicketsAssignedToUser(staff.getUserId());
        List<Ticket> allTickets = ticketService.getOpenTickets();

        model.addAttribute("myTickets", myTickets);
        model.addAttribute("allTickets", allTickets);
        model.addAttribute("staff", staff);

        return "staff/ticket-list";
    }

    @GetMapping("/{ticketId}")
    public String ticketDetail(@AuthenticationPrincipal UserDetails userDetails,@PathVariable Long ticketId, Model model) {
        UserAccount staff = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));


        Ticket ticket = ticketService.getTicketById(ticketId);

        Optional<ChatSession> mySession =
                chatService.getSessionWithMessages(ticket.getCreatedBy().getUserId(), ticket.getCustomer().getUserId());

        mySession.ifPresent(session -> model.addAttribute("mySession", session));
        model.addAttribute("ticket", ticket);
        model.addAttribute("staff", staff);

        return "staff/ticket-detail";
    }

    @PutMapping("/api/{ticketId}/note")
    public ResponseEntity<?> updateNote(@PathVariable Long ticketId,
                                        @RequestBody Map<String, String> payload) {
        Ticket ticket = ticketService.getTicketById(ticketId);

        String content = payload.get("content");
        ticket.setNote(content);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketService.updateNote(ticket);

        return ResponseEntity.ok(ticket);
    }


    @PutMapping("api/{ticketId}/status")
    @ResponseBody
    public ResponseEntity<Ticket> updateTicketStatus(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> body) {

        try {
            String status = body.get("status");
            ETicketStatus ticketStatus = ETicketStatus.valueOf(status);
            Ticket ticket = ticketService.updateTicketStatus(ticketId, ticketStatus);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/{ticketId}/assign")
    @ResponseBody
    public ResponseEntity<Ticket> assignTicket(
            @PathVariable Long ticketId,
            @RequestParam Integer assignToId) {

        try {
            Ticket ticket = ticketService.assignTicket(ticketId, assignToId);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}