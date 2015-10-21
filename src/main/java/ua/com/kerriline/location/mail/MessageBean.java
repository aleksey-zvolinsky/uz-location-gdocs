package ua.com.kerriline.location.mail;

import java.io.IOException;
import java.util.Date;

import javax.mail.MessagingException;

/**
 * 
 * @author Aleksey
 *
 */
public class MessageBean {
	
	private String subject;
	private String body;
	private Date receivedDate;

	public MessageBean() {
	}
	
	public MessageBean(String subject, String body, Date date) throws MessagingException, IOException {
		setSubject(subject);
		setBody(body);
		setReceivedDate(date);
	}

	public void setReceivedDate(Date date) {
		this.receivedDate = (Date)date.clone();
	}
	
	public Date getReceivedDate() {
		return (Date)this.receivedDate.clone();
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
