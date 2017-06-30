package nl.kb.dare.mail.mailer;

import java.io.PrintWriter;

public class Email {
    private String recipient = "";
    private String from = "";
    private String subject = "";
    private String body = "";

    public Email withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public Email withRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public Email withFrom(String from) {
        this.from = from;
        return this;
    }

    public Email withBody(String body) {
        this.body = body;
        return this;
    }

    String getRecipient() {
        return recipient;
    }

    void send(PrintWriter out) {
        out.println(String.format("From: \"%s\" <%s>", from, from));
        out.println(String.format("To: %s", recipient));
        out.println(String.format("Subject: %s", subject));
        out.println();
        out.println(body);

        out.close();
    }


    @Override
    public String toString() {
        return "Email{" +
                "recipient='" + recipient + '\'' +
                ", from='" + from + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
