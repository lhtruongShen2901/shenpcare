package g6shenpcare.controller.staff;

import g6shenpcare.models.dto.ChatMessageDTO;
import g6shenpcare.models.dto.TypingDTO;
import g6shenpcare.models.entity.Message;
import g6shenpcare.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessageDTO dto) {
        if (dto.getSessionId() == null) return;

        Message savedMessage = chatService.sendMessage(
                dto.getSessionId(),
                dto.getSenderId(),
                dto.getMessageText()
        );

        messagingTemplate.convertAndSend(
                STR."/topic/session/\{dto.getSessionId()}/messages",
                savedMessage
        );
    }

    @MessageMapping("/chat.typing")
    public void userTyping(TypingDTO dto) {
        if (dto.getSessionId() == null) return;

        messagingTemplate.convertAndSend(
                STR."/topic/session/\{dto.getSessionId()}/typing",
                dto
        );
    }


    @MessageMapping("/chat.getStatus")
    public void getStatus(Long sessionId) {
        Map<String, Object> status = chatService.getSessionStatus(sessionId);

        messagingTemplate.convertAndSend(
                STR."/topic/session/\{sessionId}/status",
                status
        );
    }
}