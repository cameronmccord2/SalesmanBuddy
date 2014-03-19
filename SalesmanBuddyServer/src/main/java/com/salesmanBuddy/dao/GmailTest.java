package com.salesmanBuddy.dao;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;

public class GmailTest {

    private static String USER_NAME = "cameronmccord@salesmanbuddy.com";  // GMail user name (just the part before "@gmail.com")
    private static String PASSWORD = "27&M2rk4$k"; // GMail password
    private static String RECIPIENT = "cameronmccord2@gmail.com";

    public static void main(String[] args) {
    	System.out.println("starting");
        String from = "billing@salesmanbuddy.com";
        String[] to = { RECIPIENT, "cameron@salesmanbuddy.com", "joeallcam2@hotmail.com" }; // list of recipient email addresses
        String subject = "Java send mail example, something new";
        String body = "Welcome to JavaMail!";

        sendFromGMail(from, to, subject, body);
    }

    private static void sendFromGMail(String from, String[] to, String subject, String body) {
//        Properties props = System.getProperties();
        Properties props = new Properties();
        String host = "smtp.gmail.com";
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.host", host);
//        props.put("mail.smtp.user", USER_NAME);
//        props.put("mail.smtp.password", PASSWORD);
//        props.put("mail.smtp.port", "465");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.debug", "true");

//        Session session = Session.getDefaultInstance(props);
        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        
        try {
            message.setFrom(new InternetAddress(from));
            
//            InternetAddress[] toAddress = new InternetAddress[to.length];
            
            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
            	message.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
//                toAddress[i] = new InternetAddress(to[i]);
            }

//            for( int i = 0; i < toAddress.length; i++) {
//                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
//            }
            
            message.setSubject(subject);
            message.setText(body);
//            message.setText("<html></html>", "utf-8", "html");
//            message.setContent("<html></html>", "text/html; charset=utf-8");
            
            Transport transport = session.getTransport("smtps");
            System.out.println("connecting");
            transport.connect(host, USER_NAME, PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
        System.out.println("sent");
    }
    
}