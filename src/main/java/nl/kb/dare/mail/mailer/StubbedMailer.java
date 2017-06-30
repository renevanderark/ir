package nl.kb.dare.mail.mailer;

import nl.kb.dare.mail.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubbedMailer implements Mailer {
    private static final Logger LOG = LoggerFactory.getLogger(StubbedMailer.class);

    @Override
    public void send(Email mail) {
        LOG.warn("No mailer provided to send email: {}", mail);
    }
}
