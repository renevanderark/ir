package nl.kb.dare.mail.mailer;

import nl.kb.dare.mail.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class SmtpMailer implements Mailer {
    private static final Logger LOG = LoggerFactory.getLogger(Email.class);
    private final String host;
    private final String from;
    private final String to;

    public SmtpMailer(String host, String from, String to) {
        this.host = host;
        this.from = from;
        this.to = to;
    }

    @Override
    public void send(Email mail) {
        try {
            System.setProperty("email.host", host);
            mail.withRecipient(to).withFrom(from);

            final URL url = new URL(String.format("mailto:%s", mail.getRecipient()));
            final URLConnection urlConnection = url.openConnection();
            urlConnection.connect();

            final PrintWriter out = new PrintWriter(new OutputStreamWriter(urlConnection.getOutputStream()));

            mail.send(out);


        } catch (Exception e) {
            LOG.error("Failed to send mail via {}", System.getProperty("mail.host"), e);
        }
    }
}
