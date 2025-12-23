package g6shenpcare.controller.client;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.Order;
import g6shenpcare.repository.BookingRepository;
import g6shenpcare.repository.OrderRepository;
import g6shenpcare.service.MoMoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PaymentController {

    @Autowired private MoMoService momoService;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private OrderRepository orderRepo;

    // 1. KÍCH HOẠT THANH TOÁN
    @GetMapping("/payment/create")
    public String createPayment(@RequestParam("type") String type, // "BOOKING" hoặc "ORDER"
                                @RequestParam("id") Long id,
                                RedirectAttributes ra) {
        try {
            String orderInfo = "";
            long amount = 0;
            String uniqueOrderId = ""; 

            if ("BOOKING".equals(type)) {
                // Booking ID là Integer trong DB nên cần intValue() (Dựa theo code cũ của bạn)
                Booking b = bookingRepo.findById(id.intValue()).orElseThrow();
                
                amount = (b.getService() != null && b.getService().getFixedPrice() != null) 
                        ? b.getService().getFixedPrice().longValue() : 100000; 
                orderInfo = "Thanh toan lich hen #" + id;
                uniqueOrderId = "BOOKING_" + id + "_" + System.currentTimeMillis();
            } 
            else if ("ORDER".equals(type)) {
                // [SỬA LỖI]: Order ID là Long, không dùng .intValue()
                Order o = orderRepo.findById(id).orElseThrow();
                
                amount = o.getTotalAmount().longValue();
                orderInfo = "Thanh toan don thuoc #" + id;
                uniqueOrderId = "ORDER_" + id + "_" + System.currentTimeMillis();
            }

            // Gọi MoMo
            String payUrl = momoService.createPaymentRequest(uniqueOrderId, String.valueOf(amount), orderInfo);
            return "redirect:" + payUrl; 

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi tạo thanh toán: " + e.getMessage());
            return "redirect:/my-account/history";
        }
    }

    // 2. XỬ LÝ KẾT QUẢ TRẢ VỀ TỪ MOMO
    @GetMapping("/payment/momo-return")
    public String momoReturn(@RequestParam("orderId") String uniqueOrderId,
                             @RequestParam("resultCode") int resultCode,
                             RedirectAttributes ra) {
        
        // resultCode = 0 là thành công
        if (resultCode == 0) {
            try {
                // Phân tích uniqueOrderId
                String[] parts = uniqueOrderId.split("_");
                String type = parts[0];
                Long id = Long.parseLong(parts[1]);

                if ("BOOKING".equals(type)) {
                    Booking b = bookingRepo.findById(id.intValue()).orElseThrow();
                    b.setPaymentStatus("PAID");
                    b.setPaymentMethod("MOMO_ONLINE"); 
                    bookingRepo.save(b);
                } 
                else if ("ORDER".equals(type)) {
                    // [SỬA LỖI]: Order ID là Long
                    Order o = orderRepo.findById(id).orElseThrow();
                    
                    o.setNotes((o.getNotes() != null ? o.getNotes() : "") + " [Đã thanh toán MoMo]");
                    o.setStatus("CONFIRMED"); 
                    o.setPaymentMethod("MOMO_ONLINE"); // Giờ đã có Setter để gọi
                    orderRepo.save(o);
                }
                ra.addFlashAttribute("message", "Thanh toán MoMo thành công!");
            } catch (Exception e) {
                ra.addFlashAttribute("error", "Lỗi cập nhật đơn hàng: " + e.getMessage());
            }
        } else {
            ra.addFlashAttribute("error", "Giao dịch thất bại hoặc bị hủy (Mã lỗi: " + resultCode + ")");
        }

        return "redirect:/my-account/history";
    }
}