package g6shenpcare.controller.support;


import g6shenpcare.entity.*;
import g6shenpcare.models.dto.BookingHistoryDTO;
import g6shenpcare.models.dto.ChatSessionSummaryDTO;
import g6shenpcare.models.entity.ChatSession;
import g6shenpcare.models.entity.Message;
import g6shenpcare.models.entity.Ticket;
import g6shenpcare.repository.*;
import g6shenpcare.service.BookingService;
import g6shenpcare.service.ChatService;
import g6shenpcare.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/support")
public class SupportChatController {
    @Autowired
    private ChatService chatService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private ServicesRepository serviceRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PetRepository petRepository;


    @Autowired
    private BookingService bookingService;

    @GetMapping("/chat")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserAccount staff = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ChatSession> waitingSessions = chatService.getWaitingSessions();


        List<ChatSessionSummaryDTO> mySessions =
                chatService.getActiveSessionSummaries(staff.getUserId());


        List<Ticket> openTickets =
                ticketService.getMyOpenTickets(staff.getUserId());

        List<Ticket> assignedOpenTickets =
                ticketService.getAssignedOpenTickets(staff.getUserId());

        model.addAttribute("staff", staff);
        model.addAttribute("waitingSessions", waitingSessions);
        model.addAttribute("mySessions", mySessions);
        model.addAttribute("openTickets", openTickets);
        model.addAttribute("assignedOpenTickets", assignedOpenTickets);

        return "staff/chat-support";
    }

        @GetMapping("/api/bookings/customer/{customerId}")
        @ResponseBody
        public List<BookingHistoryDTO> getBookingHistoryByCustomer(
                @PathVariable Integer customerId
        ) {
            return bookingService.getBookingHistoryByCustomer(customerId);
        }

    @PostMapping("/api/sessions/{sessionId}/accept")
    @ResponseBody
    public ResponseEntity<ChatSession> acceptSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserAccount staff = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatSession session = chatService.assignStaffToSession(sessionId, staff.getUserId());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/api/sessions/{sessionId}/close")
    @ResponseBody
    public ResponseEntity<ChatSession> closeSession(@PathVariable Long sessionId) {
        ChatSession session = chatService.closeSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/api/waiting-sessions")
    @ResponseBody
    public ResponseEntity<List<ChatSession>> getWaitingSessions() {
        return ResponseEntity.ok(chatService.getWaitingSessions());
    }

    @GetMapping("/api/my-sessions")
    @ResponseBody
    public ResponseEntity<List<ChatSession>> getMySessions(@AuthenticationPrincipal UserDetails userDetails) {
        UserAccount staff = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(chatService.getActiveSessionsForStaff(staff.getUserId()));
    }

    @GetMapping("/api/sessions/{sessionId}/messages")
    @ResponseBody
    public ResponseEntity<List<Message>> getSessionMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getSessionMessages(sessionId));
    }

    @PostMapping("/api/messages/send")
    @ResponseBody
    public ResponseEntity<Message> sendMessage(
            @RequestParam Long sessionId,
            @RequestParam String messageText,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserAccount sender = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        g6shenpcare.models.entity.Message message = chatService.sendMessage(sessionId, sender.getUserId(), messageText);
        return ResponseEntity.ok(message);
    }


    @GetMapping("/api/customer/{customerId}/orders")
    @ResponseBody
    public ResponseEntity<List<Order>> getCustomerOrders(@PathVariable Integer customerId) {
        UserAccount customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return ResponseEntity.ok(orderRepository.findByCustomerId(customer.getUserId()));
    }

    @GetMapping("/api/customer/{customerId}/pets")
    @ResponseBody
    public ResponseEntity<List<Pets>> getCustomerPets(@PathVariable Integer customerId) {
        UserAccount customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return ResponseEntity.ok(petRepository.findByOwnerId(customer.getUserId()));
    }

    @GetMapping("/api/customer/{customerId}/info")
    @ResponseBody
    public ResponseEntity<UserAccount> getCustomerInfo(@PathVariable Integer customerId) {
        return userRepository.findById(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/services/all")
    @ResponseBody
    public ResponseEntity<List<Services>> getAllServices() {
        return ResponseEntity.ok(serviceRepository.findAll());
    }

    @GetMapping("/api/orders/{orderId}")
    @ResponseBody
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        return orderRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/sessions/{sessionId}/mark-read")
    @ResponseBody
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserAccount user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        chatService.markMessagesAsRead(sessionId, user.getUserId());
        return ResponseEntity.ok().build();
    }


}
