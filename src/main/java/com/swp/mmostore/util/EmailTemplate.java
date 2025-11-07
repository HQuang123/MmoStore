package com.swp.mmostore.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
public class EmailTemplate {

    @Autowired
    private TemplateEngine templateEngine;

    public String verificationEmail(String code) {
        Context context = new Context();
        context.setVariable("code", code);

        // Process the new template file
        return templateEngine.process("emails/verification-email", context);
    }

    public String withdrawalRequestEmail(String userName, String amount, String bankInfo, String requestDate) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("amount", amount);
        context.setVariable("bankInfo", bankInfo);
        context.setVariable("requestDate", requestDate);

        // Process the new template file
        return templateEngine.process("emails/withdrawal-request", context);
    }

    public String withdrawalApprovedEmail(String userName, String amount, String bankInfo, String approveDate, String proofFile) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("amount", amount);
        context.setVariable("bankInfo", bankInfo);
        context.setVariable("approveDate", approveDate);
        context.setVariable("proofFile", proofFile); // Pass the link to the template

        // Process the new template file
        return templateEngine.process("emails/withdrawal-approved", context);
    }

    public String withdrawalRejectedEmail(String userName, String amount, String bankInfo, String rejectDate, String reason) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("amount", amount);
        context.setVariable("bankInfo", bankInfo);
        context.setVariable("rejectDate", rejectDate);
        context.setVariable("reason", reason);
        // 2. Process the template file into a single HTML string
        return templateEngine.process("emails/withdrawal-rejected", context);
    }

    public String sellerRegistrationSuccessEmail(String userName) {
        return "<div style=\"font-family:'Inter',Arial,sans-serif;background:#f7f7f9;padding:32px;\">" +
                "<div style=\"max-width:480px;margin:auto;background:#fff;border-radius:16px;box-shadow:0 4px 24px rgba(0,0,0,0.08);overflow:hidden;\">" +
                "<div style=\"background:linear-gradient(90deg,#ef4444 0,#f59e42 100%);padding:24px 0;text-align:center;border-radius:16px 16px 0 0;\">" +
                "<h2 style=\"color:#fff;font-size:24px;font-weight:700;margin:0;letter-spacing:1px;\">Đăng ký Seller thành công</h2>" +
                "</div>" +
                "<div style=\"padding:32px 24px 24px 24px;\">" +
                "<p style=\"font-size:17px;color:#222;margin-bottom:18px;\">Xin chào <b>" + userName + "</b>,</p>" +
                "<p style=\"font-size:16px;color:#444;margin-bottom:18px;\">Chúc mừng bạn đã đăng ký thành công tài khoản Seller tại <b>MMOMarket</b>! Vui lòng chờ quản trị viên xác minh thông tin của bạn.</p>" +
                "<p style=\"font-size:15px;color:#666;margin-bottom:24px;\">Bạn sẽ nhận được thông báo qua email khi tài khoản được duyệt hoặc có yêu cầu bổ sung thông tin.</p>" +
                "</div>" +
                "<div style=\"background:#f7f7f9;color:#aaa;font-size:13px;text-align:center;padding:16px 8px;border-radius:0 0 16px 16px;\">&copy; 2024 MMOMarket. Mọi quyền được bảo lưu.</div>" +
                "</div>" +
                "</div>";
    }

    public String sellerVerificationEmail(String userName, String verifyLink) {
        return "<div style=\"font-family:'Inter',Arial,sans-serif;background:#f7f7f9;padding:32px;\">" +
                "<div style=\"max-width:480px;margin:auto;background:#fff;border-radius:16px;box-shadow:0 4px 24px rgba(0,0,0,0.08);overflow:hidden;\">" +
                "<div style=\"background:linear-gradient(90deg,#ef4444 0,#f59e42 100%);padding:24px 0;text-align:center;border-radius:16px 16px 0 0;\">" +
                "<h2 style=\"color:#fff;font-size:24px;font-weight:700;margin:0;letter-spacing:1px;\">Xác minh tài khoản Seller</h2>" +
                "</div>" +
                "<div style=\"padding:32px 24px 24px 24px;\">" +
                "<p style=\"font-size:17px;color:#222;margin-bottom:18px;\">Xin chào <b>" + userName + "</b>,</p>" +
                "<p style=\"font-size:16px;color:#444;margin-bottom:18px;\">Vui lòng nhấn vào nút bên dưới để xác minh tài khoản Seller của bạn:</p>" +
                "<div style=\"text-align:center;margin-bottom:28px;\">" +
                "<a href='" + verifyLink + "' style='display:inline-block;background:#ef4444;color:#fff;font-weight:600;padding:12px 36px;border-radius:8px;text-decoration:none;font-size:16px;box-shadow:0 2px 8px rgba(239,68,68,0.10);transition:background 0.2s;'>Xác minh ngay</a>" +
                "</div>" +
                "<p style=\"font-size:15px;color:#666;margin-bottom:24px;text-align:center;\">Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này hoặc liên hệ hỗ trợ.</p>" +
                "</div>" +
                "<div style=\"background:#f7f7f9;color:#aaa;font-size:13px;text-align:center;padding:16px 8px;border-radius:0 0 16px 16px;\">&copy; 2024 MMOMarket. Mọi quyền được bảo lưu.</div>" +
                "</div>" +
                "</div>";
    }

    public  String sellerStatusEmail(String userName, String status, String note) {
        String statusColor = status.equalsIgnoreCase("approved") ? "#22c55e" : status.equalsIgnoreCase("rejected") ? "#ef4444" : "#f59e42";
        String statusText = status.equalsIgnoreCase("approved") ? "Đã duyệt" : status.equalsIgnoreCase("rejected") ? "Từ chối" : "Đang chờ duyệt";
        return "<div style=\"font-family:'Inter',Arial,sans-serif;background:#f7f7f9;padding:32px;\">" +
                "<div style=\"max-width:480px;margin:auto;background:#fff;border-radius:16px;box-shadow:0 4px 24px rgba(0,0,0,0.08);overflow:hidden;\">" +
                "<div style=\"background:linear-gradient(90deg,#ef4444 0,#f59e42 100%);padding:24px 0;text-align:center;border-radius:16px 16px 0 0;\">" +
                "<h2 style=\"color:#fff;font-size:24px;font-weight:700;margin:0;letter-spacing:1px;\">Trạng thái tài khoản Seller</h2>" +
                "</div>" +
                "<div style=\"padding:32px 24px 24px 24px;\">" +
                "<p style=\"font-size:17px;color:#222;margin-bottom:18px;\">Xin chào <b>" + userName + "</b>,</p>" +
                "<p style=\"font-size:16px;color:#444;margin-bottom:18px;\">Trạng thái tài khoản Seller của bạn: <span style='color:" + statusColor + ";font-weight:600;'>" + statusText + "</span></p>" +
                (note != null && !note.isEmpty() ? "<p style=\"font-size:15px;color:#666;margin-bottom:18px;\"><b>Ghi chú:</b> " + note + "</p>" : "") +
                "<p style=\"font-size:15px;color:#666;margin-bottom:24px;\">Nếu có thắc mắc, vui lòng liên hệ bộ phận hỗ trợ.</p>" +
                "</div>" +
                "<div style=\"background:#f7f7f9;color:#aaa;font-size:13px;text-align:center;padding:16px 8px;border-radius:0 0 16px 16px;\">&copy; 2024 MMOMarket. Mọi quyền được bảo lưu.</div>" +
                "</div>" +
                "</div>";
    }
}
