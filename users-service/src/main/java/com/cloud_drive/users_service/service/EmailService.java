
package com.cloud_drive.users_service.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender posiljalac;

    public EmailService(JavaMailSender posiljalac) {
        this.posiljalac = posiljalac;
    }

    public void posaljiKod(String email, String kod) {

        SimpleMailMessage poruka = new SimpleMailMessage();

        poruka.setTo(email);
        poruka.setSubject("Kod za prijavu");
        poruka.setText("Vas kod za prijavu je: " + kod
                + "\nKod vazi 5 minuta.");

        posiljalac.send(poruka);
    }

public void posaljiLinkZaReset(String email, String link) {

    SimpleMailMessage poruka = new SimpleMailMessage();

    poruka.setTo(email);
    poruka.setSubject("Resetovanje lozinke");

    poruka.setText(
        "Zatrazili ste resetovanje lozinke.\n\n"
        + "Kliknite na sledeci link:\n"
        + link
        + "\n\nLink vazi 15 minuta."
        + "\nAko niste zatrazili resetovanje, ignorisite poruku."
    );

    posiljalac.send(poruka);
}

}
