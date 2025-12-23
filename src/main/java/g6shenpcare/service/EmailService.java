package g6shenpcare.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // @Async để gửi mail chạy ngầm, không làm khách phải chờ trang web load lâu
    @Async
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("ShenPCare System <noreply@shenpcare.com>");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = nội dung là HTML

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }

    // --- CÁC MẪU EMAIL (TEMPLATES) ---

    // 1. Email xác nhận khi khách vừa Đặt lịch
    public void sendBookingConfirmation(String toEmail, String customerName, String time, String serviceName) {
        String subject = "ShenPCare - Xác nhận yêu cầu đặt lịch";
        String body = """
            <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; max-width: 600px;">
                <h2 style="color: #03594D;">Cảm ơn bạn đã đặt lịch!</h2>
                <p>Xin chào <b>%s</b>,</p>
                <p>Chúng tôi đã nhận được yêu cầu của bạn:</p>
                <ul style="background-color: #f9f9f9; padding: 15px; border-radius: 5px;">
                    <li><b>Dịch vụ:</b> %s</li>
                    <li><b>Thời gian dự kiến:</b> %s</li>
                    <li><b>Trạng thái:</b> Đang chờ duyệt 🕒</li>
                </ul>
                <p>Nhân viên sẽ sớm liên hệ hoặc xác nhận lịch cho bạn.</p>
                <hr style="border: 0; border-top: 1px solid #eee;">
                <p style="font-size: 12px; color: #888;">ShenPCare System</p>
            </div>
            """.formatted(customerName, serviceName, time);
        
        sendHtmlEmail(toEmail, subject, body);
    }

    // 2. Email thông báo Lịch đã được duyệt (Admin Confirm)
    public void sendBookingApproved(String toEmail, String customerName, String time, String doctorName) {
        String subject = "ShenPCare - Lịch hẹn của bạn đã được xác nhận! ✅";
        String body = """
            <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; max-width: 600px;">
                <h2 style="color: #03594D;">Lịch hẹn đã sẵn sàng!</h2>
                <p>Xin chào <b>%s</b>,</p>
                <p>Lịch hẹn của bạn đã được xác nhận chính thức:</p>
                <div style="background-color: #e3f2fd; padding: 15px; border-radius: 8px; border-left: 5px solid #2196f3;">
                    <p style="margin: 5px 0;"><b>👨‍⚕️ Bác sĩ phụ trách:</b> %s</p>
                    <p style="margin: 5px 0;"><b>⏰ Thời gian:</b> %s</p>
                </div>
                <p>Vui lòng đến đúng giờ để được phục vụ tốt nhất.</p>
            </div>
            """.formatted(customerName, doctorName, time);

        sendHtmlEmail(toEmail, subject, body);
    }

    // 3. Email xác nhận Đơn hàng (Mua thuốc)
    public void sendOrderConfirmation(String toEmail, String customerName, Long orderId, String totalAmount) {
        String subject = "ShenPCare - Xác nhận đơn hàng #" + orderId;
        String body = """
            <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; max-width: 600px;">
                <h2 style="color: #03594D;">Đặt hàng thành công! 💊</h2>
                <p>Xin chào <b>%s</b>,</p>
                <p>Đơn hàng <b>#%d</b> của bạn đang được xử lý.</p>
                <p><b>Tổng thanh toán:</b> <span style="color: #d32f2f; font-weight: bold; font-size: 18px;">%s</span></p>
                <p>Chúng tôi sẽ giao hàng đến địa chỉ bạn đã cung cấp trong thời gian sớm nhất.</p>
                <a href="http://localhost:8080/my-account/history" style="display: inline-block; padding: 10px 20px; background-color: #03594D; color: white; text-decoration: none; border-radius: 5px; margin-top: 10px;">Xem đơn hàng</a>
            </div>
            """.formatted(customerName, orderId, totalAmount);

        sendHtmlEmail(toEmail, subject, body);
    }
}