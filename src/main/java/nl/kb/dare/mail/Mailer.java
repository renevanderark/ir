package nl.kb.dare.mail;

import nl.kb.dare.mail.mailer.Email;

public interface Mailer {

    void send(Email mail);
}
