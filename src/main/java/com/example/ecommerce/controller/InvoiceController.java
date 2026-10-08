package com.example.ecommerce.controller;

import com.example.ecommerce.service.InvoicePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoicePdfService invoicePdfService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<byte[]> printInvoice(
            @PathVariable Long orderId,
            Authentication authentication) {

        byte[] pdf = invoicePdfService.generateInvoicePdf(
                orderId,
                authentication
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=invoice-" + orderId + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}