package com.finanzapp.infrastructure.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:finanzapp@gmail.com}")
    private String fromEmail;

    public void enviarCodigoVerificacion(String destinatario, String codigo, String asunto) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(fromEmail);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(String.format(
                    "Tu codigo de verificacion de FinanzApp es: %s\n\n" +
                    "Este codigo expira en 10 minutos.\n\n" +
                    "Si no solicitaste este codigo, ignora este mensaje.",
                    codigo
            ));

            mailSender.send(mensaje);
            log.info("Codigo de verificacion enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar correo a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de verificacion");
        }
    }

    public void enviarNotificacion(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(fromEmail);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            log.info("Notificacion enviada a {} - {}", destinatario, asunto);
        } catch (Exception e) {
            log.error("Error al enviar notificacion a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("No se pudo enviar la notificacion");
        }
    }

    public void enviarRecuperacionPasswordHtml(String destinatario, String codigo) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject("FinanzApp - Recuperacion de contrasena");
            helper.setText(buildHtmlRecuperacion(codigo), true);
            mailSender.send(mimeMessage);
            log.info("Correo HTML de recuperacion enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar correo a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de verificacion");
        }
    }

    private String buildHtmlRecuperacion(String codigo) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>FinanzApp - Recuperacion de contrasena</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:'Segoe UI',Helvetica,Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f1f5f9;padding:48px 16px;">
                <tr>
                  <td align="center">
                    <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 8px 32px rgba(0,0,0,0.10);">

                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#1d4ed8 0%%,#1e40af 100%%);padding:44px 48px;text-align:center;">
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <td align="center">
                                <div style="background:rgba(255,255,255,0.18);display:inline-block;border-radius:18px;padding:14px 22px;margin-bottom:18px;">
                                  <span style="font-size:32px;line-height:1;">&#128024;</span>
                                </div>
                              </td>
                            </tr>
                            <tr>
                              <td align="center">
                                <h1 style="color:#ffffff;margin:0;font-size:30px;font-weight:800;letter-spacing:-0.5px;">FinanzApp</h1>
                                <p style="color:rgba(255,255,255,0.75);margin:6px 0 0;font-size:14px;letter-spacing:0.3px;">Tus finanzas personales inteligentes</p>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding:48px 48px 36px;">
                          <h2 style="color:#0f172a;font-size:22px;font-weight:700;margin:0 0 10px;line-height:1.3;">Recupera tu contrasena</h2>
                          <p style="color:#64748b;font-size:15px;line-height:1.7;margin:0 0 36px;">
                            Recibimos una solicitud para restablecer la contrasena de tu cuenta de FinanzApp.
                            Usa el siguiente codigo para continuar con el proceso.
                          </p>

                          <!-- OTP Box -->
                          <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:36px;">
                            <tr>
                              <td style="background:linear-gradient(135deg,#eff6ff 0%%,#dbeafe 100%%);border:2px solid #93c5fd;border-radius:16px;padding:32px;text-align:center;">
                                <p style="color:#3b82f6;font-size:11px;font-weight:700;letter-spacing:3px;text-transform:uppercase;margin:0 0 14px;">Codigo de verificacion</p>
                                <p style="color:#1d4ed8;font-size:52px;font-weight:900;letter-spacing:14px;margin:0;font-family:'Courier New',Courier,monospace;line-height:1;">%s</p>
                                <p style="color:#94a3b8;font-size:13px;margin:16px 0 0;">
                                  Expira en <strong style="color:#ef4444;">10 minutos</strong>
                                </p>
                              </td>
                            </tr>
                          </table>

                          <p style="color:#64748b;font-size:14px;line-height:1.7;margin:0 0 20px;">
                            Si no solicitaste este cambio, puedes ignorar este correo con tranquilidad. Tu contrasena permanecera sin cambios.
                          </p>

                          <!-- Warning box -->
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <td style="background:#fefce8;border-left:4px solid #f59e0b;border-radius:8px;padding:14px 18px;">
                                <p style="color:#92400e;font-size:13px;margin:0;line-height:1.6;">
                                  <strong>Consejo de seguridad:</strong> Nunca compartas este codigo con nadie.
                                  FinanzApp jamas te lo solicitara por telefono o chat.
                                </p>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <!-- Divider -->
                      <tr>
                        <td style="padding:0 48px;">
                          <div style="height:1px;background:#f1f5f9;"></div>
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="background:#f8fafc;padding:28px 48px;text-align:center;border-radius:0 0 20px 20px;">
                          <p style="color:#94a3b8;font-size:12px;margin:0 0 6px;line-height:1.6;">
                            Este mensaje fue generado automaticamente. Por favor, no respondas a este correo.
                          </p>
                          <p style="color:#cbd5e1;font-size:11px;margin:0;">
                            &copy; 2026 FinanzApp &mdash; Todos los derechos reservados
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(codigo);
    }
}
