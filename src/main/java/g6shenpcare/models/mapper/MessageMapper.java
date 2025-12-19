package g6shenpcare.models.mapper;

import g6shenpcare.entity.UserAccount;
import g6shenpcare.models.dto.MessageDTO;
import g6shenpcare.models.dto.SessionDTO;
import g6shenpcare.models.dto.UserDTO;
import g6shenpcare.models.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageDTO toDto(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setMessageText(message.getMessageText());
        dto.setSentAt(message.getSentAt());

        // session
        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setId(message.getSession().getId());
        dto.setSession(sessionDTO);

        // sender
        UserAccount sender = message.getSender();
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(sender.getUserId());
        userDTO.setUsername(sender.getUsername());
        userDTO.setFullName(sender.getFullName());
        userDTO.setRole(sender.getRole());

        dto.setSender(userDTO);

        return dto;
    }
}
