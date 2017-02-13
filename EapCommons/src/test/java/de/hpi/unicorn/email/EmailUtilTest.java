/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.email;

import static org.junit.Assert.assertTrue;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.junit.Test;

import de.hpi.unicorn.email.EmailUtils;

public class EmailUtilTest {

	@Test
	public void testSendingAndReceiving() throws MessagingException {
		Session session = EmailUtils.getGMailSession(EmailUtils.user, EmailUtils.pass);
		String key = (new Long(System.currentTimeMillis())).toString();
		EmailUtils.postMail(session, EmailUtils.user, "test" + key, "testmsg" + key);
		Folder inbox = EmailUtils.openPop3InboxReadWrite(session);
		Folder trash = EmailUtils.openPop3TrashReadWrite(session);
		boolean emailReceived = false;
		for (Message m : inbox.getMessages()) {
			if (m.getSubject().startsWith("test" + key)) {
				emailReceived = true;
				Message[] m_array = inbox.getMessages(m.getMessageNumber(), m.getMessageNumber());
				// delete test mail
				inbox.copyMessages(m_array, trash);
				m.setFlag(Flags.Flag.DELETED, true);
			}
		}
		EmailUtils.closeFolder(inbox);
		EmailUtils.closeFolder(trash);
		assertTrue(emailReceived);
	}

}
