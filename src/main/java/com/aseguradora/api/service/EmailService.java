package com.aseguradora.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Método para enviar correos simples
    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom("aseguradoraleig@gmail.com"); // Tu correo remitente
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);

            mailSender.send(mensaje);
            System.out.println(">>> CORREO ENVIADO A: " + destinatario);
        } catch (Exception e) {
            System.err.println(">>> ERROR AL ENVIAR CORREO: " + e.getMessage());
            // No lanzamos error para no romper el flujo principal si falla el correo,
            // pero podrías manejarlo según necesidad.
        }
    }
    // --- NUEVO: Correo de Reactivación (Cuando el Admin desactiva la cuenta) ---
    public void enviarCorreoReactivacion(String destinatario, String token) {
        String asunto = "Aviso de Cuenta Desactivada - Aseguradora App";
        String enlace = "http://127.0.0.1:5500/confirmar.html?token=" + token;
        
        String mensaje = "Hola,\n\n" +
                "Un administrador ha desactivado tu cuenta temporalmente.\n" +
                "Si deseas volver a activarla inmediatamente, puedes hacerlo pulsando el siguiente enlace:\n\n" +
                enlace + "\n\n" +
                "Si no has solicitado esto, contacta con soporte.";
        
        enviarCorreo(destinatario, asunto, mensaje);
    }
}