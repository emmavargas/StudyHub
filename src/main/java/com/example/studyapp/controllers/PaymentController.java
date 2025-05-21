package com.example.studyapp.controllers;

import com.example.studyapp.entities.User;
import com.example.studyapp.repositories.UserRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final UserRepository userRepository;

    public PaymentController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String paymentMercadoPago() throws MPException, MPApiException {
        System.out.println("Iniciando creación de preferencia de pago");
        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        String clave = System.getenv("MERCADO_PAGO_KEY");
        if (clave == null || clave.isEmpty()) {
            System.err.println("Error: MERCADO_PAGO_KEY no está configurada");
            throw new IllegalStateException("MERCADO_PAGO_KEY no está configurada");
        }
        System.out.println("Access Token cargado: " + clave.substring(0, 5) + "..."); // Solo primeros 5 caracteres por seguridad
        MercadoPagoConfig.setAccessToken(clave);

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("https://studyhub.emmanueldev.com.ar")
                .failure("https://studyhub.emmanueldev.com.ar/failure")
                .pending("https://studyhub.emmanueldev.com.ar/pending")
                .build();

        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .id("1234")
                .title("StudyHub")
                .description("StudyHub version Premium")
                .pictureUrl("https://via.placeholder.com/150")
                .categoryId("Education")
                .quantity(1)
                .currencyId("ARS")
                .unitPrice(new BigDecimal("5000"))
                .build();
        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(itemRequest);

        PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                .excludedPaymentTypes(List.of(PreferencePaymentTypeRequest.builder().id("ticket").build()))
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(user.getUsername())
                .paymentMethods(paymentMethods)
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);
        String sandboxInitPoint = preference.getSandboxInitPoint();
        System.out.println("Sandbox Init Point generado: " + sandboxInitPoint);
        return sandboxInitPoint;
    }

    private String getUsernameContextSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }
}