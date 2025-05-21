package com.example.studyapp.controllers;

import com.example.studyapp.entities.User;
import com.example.studyapp.repositories.UserRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private final UserRepository userRepository;

    public WebhookController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/success")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("x-signature") String signature,
            @RequestHeader("x-request-id") String requestId,
            @RequestHeader(value = "x-topic", required = false) String topic,
            @RequestBody Map<String, Object> payload
    ) {
        System.out.println("Webhook recibido - Topic: " + topic + ", Payload: " + payload);
        try {
            // --- 1. Verificar firma (x-signature) ---
            String secret = System.getenv("MERCADO_PAGO_WEBHOOK_SECRET");
            if (secret == null || secret.isEmpty()) {
                System.err.println("Error: MERCADO_PAGO_WEBHOOK_SECRET no está configurada");
                return ResponseEntity.status(500).body("Configuración de webhook no encontrada");
            }

            // Parsear el x-signature (formato: ts=<timestamp>,v1=<hash>)
            String[] signatureParts = signature.split(",");
            if (signatureParts.length != 2) {
                System.out.println("Formato de firma inválido: " + signature);
                return ResponseEntity.status(403).body("Formato de firma inválido");
            }

            String timestamp = null;
            String receivedHash = null;
            for (String part : signatureParts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    if (key.equals("ts")) {
                        timestamp = value;
                    } else if (key.equals("v1")) {
                        receivedHash = value;
                    }
                }
            }

            if (timestamp == null || receivedHash == null) {
                System.out.println("Faltan ts o v1 en la firma: " + signature);
                return ResponseEntity.status(403).body("Faltan ts o v1 en la firma");
            }

            // Obtener el payment_id del payload
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String paymentId = data.get("id").toString();

            // Construir el string para firmar según la documentación
            String dataToSign = "id:" + paymentId + ";request-id:" + requestId + ";ts:" + timestamp + ";";
            System.out.println("Datos firmados: " + dataToSign);
            String expectedHash = generateHmacSignature(dataToSign, secret);
            System.out.println("Firma esperada: " + expectedHash + ", Recibida: " + receivedHash);

            if (!expectedHash.equals(receivedHash)) {
                System.out.println("Firma inválida. Esperada: " + expectedHash + ", Recibida: " + receivedHash);
                return ResponseEntity.status(403).body("Firma inválida");
            }

            // --- 2. Obtener datos del pago desde MercadoPago ---
            String clave = System.getenv("MERCADO_PAGO_KEY");
            if (clave == null || clave.isEmpty()) {
                System.err.println("Error: MERCADO_PAGO_KEY no está configurada");
                return ResponseEntity.status(500).body("Configuración de Mercado Pago no encontrada");
            }
            MercadoPagoConfig.setAccessToken(clave);
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(paymentId));

            // --- 3. Obtener el usuario por external_reference ---
            String username = payment.getExternalReference();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                System.out.println("Usuario no encontrado para username: " + username);
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            // --- 4. Activar premium ---
            user.setPremium(true);
            userRepository.save(user);
            System.out.println("Usuario " + username + " actualizado a premium");

            return ResponseEntity.ok("Notificación procesada");
        } catch (MPApiException | MPException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error en MercadoPago API: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    private String generateHmacSignature(String data, String secret) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar la firma HMAC", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}