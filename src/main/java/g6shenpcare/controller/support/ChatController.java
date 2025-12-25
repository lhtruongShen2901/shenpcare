package g6shenpcare.controller.support;

import g6shenpcare.entity.Order;
import g6shenpcare.entity.Pets;
import g6shenpcare.entity.Services;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.models.entity.ChatSession;
import g6shenpcare.models.entity.Message;
import g6shenpcare.repository.*;
import g6shenpcare.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private ServicesRepository serviceRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PetRepository petRepository;



    @GetMapping("/chat")
    public String customerChat(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserAccount customer = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<ChatSession> session = chatService.findActiveChatSession(customer.getUserId());

        model.addAttribute("customer", customer);
        if(session.isPresent()){
            model.addAttribute("chatSession", session.get());
            model.addAttribute("messages", chatService.getSessionMessages(session.get().getId()));
        }
        return "index";
    }

    @PostMapping("/api/sessions/start")
    @ResponseBody
    public ResponseEntity<ChatSession> startSession(@AuthenticationPrincipal UserDetails userDetails) {
        UserAccount customer = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatSession session = chatService.startChatSession(customer.getUserId());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/api/messages/send")
    @ResponseBody
    public ResponseEntity<Message> sendMessage(
            @RequestParam Long sessionId,
            @RequestParam String messageText,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserAccount sender = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = chatService.sendMessage(sessionId, sender.getUserId(), messageText);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/api/sessions/{sessionId}/messages")
    @ResponseBody
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long sessionId) {
        List<Message> messages = chatService.getSessionMessages(sessionId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/api/services")
    @ResponseBody
    public ResponseEntity<List<Services>> getServices() {
        return ResponseEntity.ok(serviceRepository.findByActive(true));
    }

    @GetMapping("/api/orders/my")
    @ResponseBody
    public ResponseEntity<List<Order>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        UserAccount customer = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(orderRepository.findByCustomerId(customer.getUserId()));
    }

    @GetMapping("/api/orders/{orderNumber}")
    @ResponseBody
    public ResponseEntity<Order> getOrderByNumber(@PathVariable Long orderNumber) {
        return orderRepository.findByOrderId(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/pets/my")
    @ResponseBody
    public ResponseEntity<List<Pets>> getMyPets(@AuthenticationPrincipal UserDetails userDetails) {
        UserAccount owner = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(petRepository.findByOwnerId(owner.getUserId()));
    }
}
