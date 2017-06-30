package nl.kb.dare.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.mail.Mailer;
import nl.kb.dare.mail.mailer.SmtpMailer;
import nl.kb.dare.mail.mailer.StubbedMailer;

public class MailerFactory {
    @JsonProperty
    private String to;

    @JsonProperty
    private String from;

    @JsonProperty
    private String host;

    @JsonProperty
    private String type;

    public Mailer getMailer() {
        if (type.equalsIgnoreCase("smtp")) {
            return new SmtpMailer(host, from, to);
        }

        return new StubbedMailer();
    }
}
