package nl.kb.dare.mail.mailer;

import nl.kb.dare.mail.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SmtpMailer implements Mailer {
    private static final Logger LOG = LoggerFactory.getLogger(Email.class);
    private final String host;
    private final String from;
    private final String to;
    private final Integer port;

    public SmtpMailer(String host, String from, String to, Integer port) {
        this.host = host;
        this.from = from;
        this.to = to;
        this.port = port;
    }

    @Override
    public void send(Email email) {
        new Thread(() -> {
            try {
                final Properties properties = new Properties();
                properties.put("mail.smtp.host", host);
                properties.put("mail.smtp.port", String.format("%d", port));

                final Session session = Session.getDefaultInstance(properties);
                final Message message = new MimeMessage(session);

                message.setFrom(new InternetAddress(from));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                message.setSubject(email.getSubject());
                message.setText(email.getBody());
                Transport.send(message);

            } catch (Exception e) {
                LOG.error("Failed to send mail via {}", System.getProperty("email.host"), e);
            }
        }).start();
    }
}
