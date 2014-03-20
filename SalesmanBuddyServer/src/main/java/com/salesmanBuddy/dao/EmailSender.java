package com.salesmanBuddy.dao;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.salesmanBuddy.model.SBEmail;

public class EmailSender {

	private static EmailSender instance= null;
	private static Object mutex= new Object();
	private LinkedBlockingQueue<SBEmail> emailQueue;
	private static String USER_NAME = "cameronmccord@salesmanbuddy.com";  // GMail user name (just the part before "@gmail.com")
	private static String PASSWORD = "27&M2rk4$k"; // GMail password
	private static long EMAIL_DELAY = 10000;// 10 seconds
//	private int count = 0;
	private EmailSender(){
		this.emailQueue = new LinkedBlockingQueue<SBEmail>();

		new Timer().schedule(new TimerTask(){
			public void run(){
//				count++;
				EmailSender.processEmails();
			}
		}, EMAIL_DELAY, EMAIL_DELAY);
	}
	
	private static EmailSender getInstance(){
		if(instance==null){
			synchronized (mutex){
				if(instance==null) instance= new EmailSender();
			}
		}
		return instance;
	}
	
	public synchronized static void sendEmail(SBEmail email){
		if(!EmailSender.getInstance().emailQueue.contains(email))// keeps duplicates from being added to the queue
			EmailSender.getInstance().emailQueue.add(email);
	}

	private synchronized static void processEmails() {
//		ArrayList<String> recipients = new ArrayList<String>();
//		recipients.add("cameronmccord2@gmail.com");
//		SBEmail s = SBEmail.newPlainTextEmail("billing@salesmanbuddy.com", recipients, "Test subject", "count: " + EmailSender.getInstance().count, false);
//		EmailSender.sendEmail(s);
		
		EmailSender sender = EmailSender.getInstance();
		ArrayList<SBEmail> emails = new ArrayList<SBEmail>();
		sender.emailQueue.drainTo(emails);
		//  the cound and emails.size() should be the same
		sender.sendFromGMail(emails);
	}


	private synchronized void sendFromGMail(ArrayList<SBEmail> emails) {
		//      Properties props = System.getProperties();
		Properties props = new Properties();
		String host = "smtp.gmail.com";
		//      props.put("mail.smtp.starttls.enable", "true");
		//      props.put("mail.smtp.host", host);
		//      props.put("mail.smtp.user", USER_NAME);
		//      props.put("mail.smtp.password", PASSWORD);
		//      props.put("mail.smtp.port", "465");
		//      props.put("mail.smtp.auth", "true");
		//      props.put("mail.smtp.debug", "true");

		//      Session session = Session.getDefaultInstance(props);
		Session session = Session.getInstance(props);


		try {
			Transport transport = session.getTransport("smtps");
			transport.connect(host, USER_NAME, PASSWORD);

			for(SBEmail e : emails){
				if(e.isIndividualEmailsToRecipients()){
					for(String to : e.getTo()) {
						MimeMessage message = new MimeMessage(session);
						message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
						message.setFrom(new InternetAddress(e.getFrom()));

						message.setSubject(e.getSubject());
						if(e.getBody().length() > 0)
							message.setText(e.getBody());
						else
							message.setText(e.getBodyHtml(), "utf-8", "html");
						transport.sendMessage(message, message.getAllRecipients());
					}
				}else{
					MimeMessage message = new MimeMessage(session);
					message.setFrom(new InternetAddress(e.getFrom()));
					for(String to : e.getTo()) {
						message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
					}
					message.setSubject(e.getSubject());
					if(e.getBody().length() > 0)
						message.setText(e.getBody());
					else
						message.setText(e.getBodyHtml(), "utf-8", "html");
					transport.sendMessage(message, message.getAllRecipients());
				}
			}
			transport.close();
		}
		catch (NoSuchProviderException e) {
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