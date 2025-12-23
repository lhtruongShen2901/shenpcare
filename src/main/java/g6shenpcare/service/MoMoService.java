package g6shenpcare.service;

import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;

@Service
public class MoMoService {

    // CẤU HÌNH SANDBOX (TEST)
    private static final String PARTNER_CODE = "MOMO";
    private static final String ACCESS_KEY = "F8BBA842ECF85";
    private static final String SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    private static final String ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create";
    
    // URL trả về khi thanh toán xong (Bạn cần sửa port nếu khác 8080)
    private static final String RETURN_URL = "http://localhost:8080/payment/momo-return";
    private static final String IPN_URL = "http://localhost:8080/payment/momo-ipn"; // Dùng cho server thật

    // 1. Hàm tạo link thanh toán
    public String createPaymentRequest(String orderId, String amount, String orderInfo) throws Exception {
        String requestId = String.valueOf(System.currentTimeMillis());
        String requestType = "captureWallet";
        String extraData = ""; // Pass empty if none

        // Chuỗi dữ liệu cần ký (Signature Format của MoMo)
        // format: accessKey=$accessKey&amount=$amount&extraData=$extraData&ipnUrl=$ipnUrl&orderId=$orderId&orderInfo=$orderInfo&partnerCode=$partnerCode&redirectUrl=$redirectUrl&requestId=$requestId&requestType=$requestType
        String rawHash = "accessKey=" + ACCESS_KEY +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + IPN_URL +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + PARTNER_CODE +
                "&redirectUrl=" + RETURN_URL +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = hmacSHA256(rawHash, SECRET_KEY);

        // Tạo JSON Body thủ công để tránh lỗi thư viện
        String jsonBody = String.format("{"
                + "\"partnerCode\": \"%s\","
                + "\"partnerName\": \"ShenPCare\","
                + "\"storeId\": \"MomoTestStore\","
                + "\"requestId\": \"%s\","
                + "\"amount\": %s,"
                + "\"orderId\": \"%s\","
                + "\"orderInfo\": \"%s\","
                + "\"redirectUrl\": \"%s\","
                + "\"ipnUrl\": \"%s\","
                + "\"lang\": \"vi\","
                + "\"extraData\": \"%s\","
                + "\"requestType\": \"%s\","
                + "\"signature\": \"%s\""
                + "}", 
                PARTNER_CODE, requestId, amount, orderId, orderInfo, RETURN_URL, IPN_URL, extraData, requestType, signature);

        // Gửi Request POST
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Phân tích JSON trả về để lấy payUrl (Cách thô sơ dùng String split để không cần thư viện JSON phức tạp)
        String resBody = response.body();
        if (resBody.contains("\"payUrl\":")) {
            // Cắt chuỗi lấy URL (Nếu dùng Jackson/Gson thì đẹp hơn, nhưng cách này chạy ngay không cần dependency)
            int startIndex = resBody.indexOf("\"payUrl\":\"") + 10;
            int endIndex = resBody.indexOf("\"", startIndex);
            return resBody.substring(startIndex, endIndex);
        } else {
            throw new RuntimeException("Lỗi MoMo: " + resBody);
        }
    }

    // Hàm mã hóa HMAC SHA256
    private String hmacSHA256(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return toHexString(bytes);
    }

    private String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}