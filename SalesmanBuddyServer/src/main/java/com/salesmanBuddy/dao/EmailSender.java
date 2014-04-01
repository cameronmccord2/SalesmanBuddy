package com.salesmanBuddy.dao;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.salesmanBuddy.exceptions.MalformedSBEmailException;
import com.salesmanBuddy.model.SBEmail;

public class EmailSender{

	private static EmailSender instance= null;
	private static Object mutex= new Object();
	private LinkedBlockingQueue<SBEmail> emailQueue;
	private static final String EMAIL_USER_NAME = "cameronmccord@salesmanbuddy.com";  // GMail user name (just the part before "@gmail.com")
    private static final String EMAIL_PASSWORD = "27&M2rk4$k"; // GMail password
	private static final long EMAIL_DELAY = 10000;// 10 seconds
	protected DataSource dataSource;
	protected Timer timer;
	
	private EmailSender(){
		this.emailQueue = new LinkedBlockingQueue<SBEmail>();
		System.out.println("started Emil sender");
		try{
			Context initContext = new InitialContext();
			Context envContext = (Context)initContext.lookup("java:/comp/env");
			dataSource = (DataSource)envContext.lookup("jdbc/SalesmanBuddyDB");
		}catch(NamingException ne){
			throw new RuntimeException(ne);
		}

		this.timer = new Timer();
		this.timer.schedule(new TimerTask(){
			public void run(){
//				count++;
				EmailSender.processEmails();
			}
		}, EMAIL_DELAY, EMAIL_DELAY);
	}
	
	private static EmailSender getInstance(){
		if(instance==null){
			synchronized (mutex){
				if(instance==null){
					instance= new EmailSender();
					System.out.println("EmailSender instance created");
				}
			}
		}
		return instance;
	}
	
	public synchronized static void sendEmail(SBEmail email) throws MalformedSBEmailException{
		if((email.getBody() == null || email.getBody().length() == 0) && (email.getBodyHtml() == null || email.getBodyHtml().length() == 0))
			throw new MalformedSBEmailException(new StringBuilder().append("The body and bodyHtml cannot be null or of length zero, email: ").append(email.toString()).toString());
		if(email.getSubject() == null || email.getSubject().length() == 0)
			throw new MalformedSBEmailException(new StringBuilder().append("The subject cannot be null or of length zero, email: ").append(email.toString()).toString());
		if(email.getFrom() == null || email.getFrom().length() == 0)
			throw new MalformedSBEmailException(new StringBuilder().append("The from cannot be null or of length zero, email: ").append(email.toString()).toString());
		if(email.getTo() == null || email.getTo().size() == 0)
			throw new MalformedSBEmailException(new StringBuilder().append("The to cannot be null or of length zero, email: ").append(email.toString()).toString());
		for(String to : email.getTo()){
			if(to == null || to.length() == 0)
				throw new MalformedSBEmailException(new StringBuilder().append("The to emails must all be not null and not of length zero, email: ").append(email.toString()).toString());
		}

		if(!EmailSender.getInstance().emailQueue.contains(email)){// keeps duplicates from being added to the queue, during this period
			email.setId(EmailSender.getInstance().saveEmailToDatabase(email));
			EmailSender.getInstance().emailQueue.add(email);
		}
	}
	
	public static void sendEmails(ArrayList<SBEmail> emails) throws MalformedSBEmailException {
		for(SBEmail e : emails)
			EmailSender.sendEmail(e);
	}
	
	public static void destroy(){
		EmailSender.getInstance().timer.cancel();
		System.out.println("email sender destroyed");
	}
	
	public static void create(){
		EmailSender.getInstance();
		System.out.println("email sender created");
	}

	private int saveEmailToDatabase(SBEmail email) {
//		String sql = "INSERT INTO licenses (longitude, latitude, userId, stateId) VALUES (?, ?, ?, ?)";
//		int id = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setFloat(1, license.getLongitude());
//			statement.setFloat(2, license.getLatitude());
//			statement.setInt(3, license.getUserId());
//			statement.setInt(4, license.getStateId());
//			statement.execute();
//			id = this.parseFirstInt(statement.getGeneratedKeys(), "id");
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return id;
		return 0;
	}
	
	private void markMessageAsSentInDatabase(Integer id){
		
	}

	private synchronized static void processEmails() {
//		ArrayList<String> recipients = new ArrayList<String>();
//		recipients.add("cameronmccord2@gmail.com");
//		SBEmail s = SBEmail.newPlainTextEmail("billing@salesmanbuddy.com", recipients, "Test subject", "count: " + EmailSender.getInstance().count, false);
//		EmailSender.sendEmail(s);
		
		EmailSender sender = EmailSender.getInstance();
		ArrayList<SBEmail> emails = new ArrayList<SBEmail>();
		sender.emailQueue.drainTo(emails);
		//  the count and emails.size() should be the same
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
			transport.connect(host, EMAIL_USER_NAME, EMAIL_PASSWORD);

			for(SBEmail e : emails){
				System.out.println("sending email");
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
						this.markMessageAsSentInDatabase(e.getId());
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
					this.markMessageAsSentInDatabase(e.getId());
				}
			}
			transport.close();
		}
		catch(AuthenticationFailedException e){
			e.printStackTrace();
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
	}
}


































