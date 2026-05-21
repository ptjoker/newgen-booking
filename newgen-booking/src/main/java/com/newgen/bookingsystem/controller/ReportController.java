package com.newgen.bookingsystem.controller;

import com.newgen.bookingsystem.entity.*;
import com.newgen.bookingsystem.repository.*;
import com.newgen.bookingsystem.service.AdminActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AdminActionService adminActionService;

    @GetMapping
    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByGeneratedDateDesc();
    }

    @GetMapping("/admin/{adminId}")
    public List<Report> getReportsByAdmin(@PathVariable Integer adminId) {
        return reportRepository.findByAdmin_UserIdOrderByGeneratedDateDesc(adminId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Integer id) {
        Optional<Report> report = reportRepository.findById(id);
        return report.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody Report report) {
        Report savedReport = reportRepository.save(report);
        return ResponseEntity.ok(savedReport);
    }

    @GetMapping("/bookings")
    public void downloadBookingsReport(
            @RequestParam(required = false) String format,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer adminId,
            HttpServletResponse response) throws IOException {

        List<Booking> bookings;

        if (startDate != null && endDate != null) {
            bookings = bookingRepository.findByBookingDateBetween(startDate, endDate);
        } else if (status != null && !status.isEmpty()) {
            try {
                BookingStatus bookingStatus = BookingStatus.valueOf(status);
                bookings = bookingRepository.findByStatus(bookingStatus);
            } catch (Exception e) {
                bookings = bookingRepository.findAll();
            }
        } else {
            bookings = bookingRepository.findAll();
        }

        // Log to audit log if adminId is provided
        if (adminId != null) {
            Optional<User> admin = userRepository.findById(adminId);
            if (admin.isPresent() && "ADMIN".equalsIgnoreCase(admin.get().getRole())) {
                String dateRange = (startDate != null && endDate != null) ?
                        " (" + startDate + " to " + endDate + ")" : "";
                adminActionService.logAction(
                        admin.get(),
                        "REPORT_GENERATE",
                        "REPORT",
                        null,
                        "Bookings Report",
                        "Generated bookings report" + dateRange + " with " + bookings.size() + " records"
                );
            }
        }

        String filename = "bookings_report_" + System.currentTimeMillis();

        if ("csv".equalsIgnoreCase(format)) {
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".csv\"");

            PrintWriter writer = response.getWriter();
            writer.write("\uFEFF");
            writer.write("Booking ID,Reference,User Name,User Email,Provider Name,Booking Type,Booking Date,Time Slot,Guests,Total Amount,Status\n");

            for (Booking booking : bookings) {
                String userName = booking.getUser().getFirstName() + " " + booking.getUser().getLastName();
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%d,%.2f,%s\n",
                        booking.getBookingId(),
                        booking.getBookingReference(),
                        userName,
                        booking.getUser().getEmail(),
                        booking.getProvider().getBusinessName(),
                        booking.getBookingType(),
                        booking.getBookingDate(),
                        booking.getTimeSlot() != null ? booking.getTimeSlot().toString() : "",
                        booking.getNumberOfGuests(),
                        booking.getTotalAmount(),
                        booking.getStatus()
                );
            }
            writer.flush();
        } else {
            response.setContentType("text/html");
            response.setHeader("Content-Disposition", "inline; filename=\"" + filename + ".html\"");

            PrintWriter writer = response.getWriter();
            writer.println("<!DOCTYPE html>");
            writer.println("<html><head><meta charset='UTF-8'><title>Bookings Report</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial, sans-serif; margin: 40px; }");
            writer.println("h1 { color: #0b4f6c; }");
            writer.println("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            writer.println("th { background-color: #0b4f6c; color: white; padding: 12px; text-align: left; }");
            writer.println("td { padding: 10px; border-bottom: 1px solid #ddd; }");
            writer.println("tr:hover { background-color: #f5f5f5; }");
            writer.println(".footer { margin-top: 30px; text-align: center; font-size: 12px; color: #666; }");
            writer.println("@media print { body { margin: 0; } }");
            writer.println("</style>");
            writer.println("</head><body>");
            writer.println("<h1>NewGen Booking System - Bookings Report</h1>");
            writer.println("<p><strong>Generated on:</strong> " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");
            writer.println("<p><strong>Total Records:</strong> " + bookings.size() + "</p>");
            writer.println("<table border='1'>");
            writer.println("<thead>");
            writer.println("<tr>");
            writer.println("<th>Booking ID</th>");
            writer.println("<th>Reference</th>");
            writer.println("<th>User Name</th>");
            writer.println("<th>Provider</th>");
            writer.println("<th>Type</th>");
            writer.println("<th>Date</th>");
            writer.println("<th>Amount</th>");
            writer.println("<th>Status</th>");
            writer.println("</tr>");
            writer.println("</thead>");
            writer.println("<tbody>");

            for (Booking booking : bookings) {
                String userName = booking.getUser().getFirstName() + " " + booking.getUser().getLastName();
                writer.printf("烟世家%d络工作%s络工作%s络工作%s络工作%s络工作%s络工作R%.2f络工作%s络工作%n",
                        booking.getBookingId(),
                        booking.getBookingReference(),
                        userName,
                        booking.getProvider().getBusinessName(),
                        booking.getBookingType(),
                        booking.getBookingDate(),
                        booking.getTotalAmount(),
                        booking.getStatus()
                );
            }

            writer.println("</tbody>");
            writer.println("验算");
            writer.println("<div class='footer'>");
            writer.println("<p>NewGen Booking System - Confidential Report</p>");
            writer.println("</div>");
            writer.println("</body></html>");
            writer.flush();
        }
    }

    @GetMapping("/payments")
    public void downloadPaymentsReport(
            @RequestParam(required = false) String format,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer adminId,
            HttpServletResponse response) throws IOException {

        List<Payment> payments;

        if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            payments = paymentRepository.findByPaymentDateBetween(startDateTime, endDateTime);
        } else if (status != null && !status.isEmpty()) {
            try {
                PaymentStatus paymentStatus = PaymentStatus.valueOf(status);
                payments = paymentRepository.findByStatus(paymentStatus);
            } catch (Exception e) {
                payments = paymentRepository.findAll();
            }
        } else {
            payments = paymentRepository.findAll();
        }

        // Log to audit log if adminId is provided
        if (adminId != null) {
            Optional<User> admin = userRepository.findById(adminId);
            if (admin.isPresent() && "ADMIN".equalsIgnoreCase(admin.get().getRole())) {
                adminActionService.logAction(
                        admin.get(),
                        "REPORT_GENERATE",
                        "REPORT",
                        null,
                        "Payments Report",
                        "Generated payments report with " + payments.size() + " records"
                );
            }
        }

        String filename = "payments_report_" + System.currentTimeMillis();

        if ("csv".equalsIgnoreCase(format)) {
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".csv\"");

            PrintWriter writer = response.getWriter();
            writer.write("\uFEFF");
            writer.write("Payment ID,Booking ID,Amount,Payment Method,Transaction ID,Payment Date,Status\n");

            for (Payment payment : payments) {
                writer.printf("%d,%d,%.2f,%s,%s,%s,%s\n",
                        payment.getPaymentId(),
                        payment.getBooking().getBookingId(),
                        payment.getAmount(),
                        payment.getPaymentMethod(),
                        payment.getTransactionId() != null ? payment.getTransactionId() : "",
                        payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "",
                        payment.getStatus()
                );
            }
            writer.flush();
        } else {
            response.setContentType("text/html");
            response.setHeader("Content-Disposition", "inline; filename=\"" + filename + ".html\"");

            PrintWriter writer = response.getWriter();
            writer.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Payments Report</title>");
            writer.println("<style>body{font-family:Arial;margin:40px;}th{background:#0b4f6c;color:white;padding:10px;}td{padding:8px;border-bottom:1px solid #ddd;}</style>");
            writer.println("</head><body>");
            writer.println("<h1>NewGen Booking System - Payments Report</h1>");
            writer.println("<p>Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");
            writer.println("<p>Total Records: " + payments.size() + "</p>");
            writer.println("<table border='1'><thead><tr><th>Payment ID</th><th>Booking ID</th><th>Amount</th><th>Method</th><th>Status</th></tr></thead><tbody>");

            for (Payment payment : payments) {
                writer.printf("烟世家%d络工作%d络工作R%.2f络工作%s络工作%s络工作%n",
                        payment.getPaymentId(),
                        payment.getBooking().getBookingId(),
                        payment.getAmount(),
                        payment.getPaymentMethod(),
                        payment.getStatus()
                );
            }
            writer.println("</tbody>验算</body></html>");
            writer.flush();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteReport(@PathVariable Integer id) {
        try {
            if (reportRepository.existsById(id)) {
                reportRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/download")
    public ResponseEntity<?> incrementDownloadCount(@PathVariable Integer id) {
        Optional<Report> reportOpt = reportRepository.findById(id);
        if (!reportOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Report report = reportOpt.get();
        report.setDownloadCount(report.getDownloadCount() + 1);
        Report updatedReport = reportRepository.save(report);
        return ResponseEntity.ok(updatedReport);
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getReportStatistics() {
        try {
            long totalReports = reportRepository.count();
            long completedReports = reportRepository.countByStatus(Report.ReportStatus.completed);
            long generatingReports = reportRepository.countByStatus(Report.ReportStatus.generating);
            long failedReports = reportRepository.countByStatus(Report.ReportStatus.failed);

            return ResponseEntity.ok(Map.of(
                    "totalReports", totalReports,
                    "completedReports", completedReports,
                    "generatingReports", generatingReports,
                    "failedReports", failedReports
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching statistics: " + e.getMessage());
        }
    }

    private String getDayName(Byte dayOfWeek) {
        if (dayOfWeek == null) return "Unknown";
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return days[dayOfWeek];
    }
}