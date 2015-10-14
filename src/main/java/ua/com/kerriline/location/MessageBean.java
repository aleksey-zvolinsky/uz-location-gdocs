package ua.com.kerriline.location;

import java.io.IOException;

import javax.mail.MessagingException;

/**
 * 
 * @author Aleksey
 *
 */
public class MessageBean {
	
	private String subject;
	private String body;

	public MessageBean() {
	}
	
	public MessageBean(String subject, String body) throws MessagingException, IOException {
		setSubject(subject);
		setBody(body);
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
