/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.email;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.imap.IMAPFolder;

import de.hpi.unicorn.configuration.EapConfiguration;

/**
 * Utils for sending emails via google mail
 */
public class EmailUtils {

	public static String user = EapConfiguration.eMailPassword;
	public static String pass = EapConfiguration.eMailUser;

	public static void main(final String[] args) throws MessagingException, IOException {
		final Session session = EmailUtils.getGMailSession(EmailUtils.user, EmailUtils.pass);

		EmailUtils.printTestInbox(session);
		EmailUtils.sendTestMail(session);
	}

	private static void printTestInbox(final Session session) throws MessagingException, IOException {
		final Folder inbox = EmailUtils.openPop3InboxReadWrite(session);
		EmailUtils.printAllTextPlainMessages(inbox);
		EmailUtils.closeFolder(inbox);
	}

	public static boolean sendBP2013Mail(final String recipient, final String subject, final String message) {
		try {
			final Session session = EmailUtils.getGMailSession(EmailUtils.user, EmailUtils.pass);
			EmailUtils.postMail(session, recipient, subject, message);
			return true;
		} catch (final MessagingException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void sendTestMail(final Session session) throws MessagingException {
		EmailUtils.postMail(session, EmailUtils.user, "Test message",
				"Mail delivery of Event Processing Platform has been successfully set up!");
	}

	public static Session getGMailSession(final String user, final String pass) {
		final Properties props = new Properties();

		// Zum Empfangen
		props.setProperty("mail.store.protocol", "imaps");

		// Zum Senden
		props.setProperty("mail.smtp.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.auth", "true");
		props.setProperty("mail.smtp.port", "465");
		props.setProperty("mail.smtp.socketFactory.port", "465");
		props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.smtp.socketFactory.fallback", "false");

		return Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, pass);
			}
		});
	}

	/**
	 * opens inbox folder in read write modus returns inbox folder
	 */
	public static Folder openPop3InboxReadWrite(final Session session) throws MessagingException {
		final Store store = session.getStore("imaps");
		store.connect("imap.googlemail.com", EmailUtils.user, EmailUtils.pass);

		final IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX");
		if (!folder.isOpen()) {
			folder.open(Folder.READ_WRITE);
		}
		return folder;
	}

	/**
	 * return trash folder in read write modus
	 */
	public static Folder openPop3TrashReadWrite(final Session session) throws MessagingException {
		final Store store = session.getStore("imaps");
		store.connect("imap.googlemail.com", EmailUtils.user, EmailUtils.pass);

		final IMAPFolder folder = (IMAPFolder) store.getFolder("[Gmail]/Papierkorb");
		if (!folder.isOpen()) {
			folder.open(Folder.READ_WRITE);
		}
		return folder;
	}

	/**
	 * closes folder
	 */
	public static void closeFolder(final Folder folder) throws MessagingException {
		folder.close(false);
		folder.getStore().close();
	}

	/**
	 * sends email
	 */
	public static void postMail(final Session session, final String recipient, final String subject,
			final String message) throws MessagingException {
		final Message msg = new MimeMessage(session);

		final InternetAddress addressTo = new InternetAddress(recipient);
		msg.setRecipient(Message.RecipientType.TO, addressTo);

		msg.setSubject(subject);
		msg.setContent(message, "text/plain");
		Transport.send(msg);
	}

	public static void printAllTextPlainMessages(final Folder folder) throws MessagingException, IOException {
		for (final Message m : folder.getMessages()) {
			System.out.println("\nNachricht:");
			System.out.println("Von: " + Arrays.toString(m.getFrom()));
			System.out.println("Betreff: " + m.getSubject());
			System.out.println("Gesendet am: " + m.getSentDate());
			System.out.println("Content-Type: " + new ContentType(m.getContentType()));

			if (m.isMimeType("text/plain")) {
				System.out.println(m.getContent());
			}
		}
	}

}
