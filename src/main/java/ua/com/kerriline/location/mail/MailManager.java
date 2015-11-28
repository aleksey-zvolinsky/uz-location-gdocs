package ua.com.kerriline.location.mail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import com.google.common.base.Strings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * http://www.technicalkeeda.com/java/how-to-access-gmail-inbox-using-java-imap
 * 
 * @author Aleksey
 *
 */
@Component
public class MailManager {

	private static final Log LOG = LogFactory.getLog(MailManager.class);
	
	@Value("${report-mail.login}")
	private String email = "sledline@gmail.com";
	@Value("${report-mail.password}")
	private String pass = "k5666031";
	@Value("${report-mail.to}")
	private String autoreportAddress = "autoinform@uz.gov.ua";
	
	@Value("${result-mail.subject}")
	private String resultSubject = "Дислокация";
	@Value("${result-mail.text}")
	private String resultText = "Смотрите вложение";
	@Value("${result-mail.attachmentFileName}")
	private String attachmentFileName = "dislocation.xlsx";

	public MessageBean getLast1392() {
		Properties props = new Properties();
		Message[] messages = null;
		MessageBean bean = null;
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("mail.properties"));
			Session session = Session.getInstance(props, null);

			Store store = session.getStore("imaps");
			
			
			store.connect("smtp.gmail.com", email, pass);

			Folder inbox = store.getFolder("inbox");
			inbox.open(Folder.READ_ONLY);
			int messageCount = inbox.getMessageCount();

			LOG.info("Total Messages:- " + messageCount);

			messages = inbox.getMessages();
			LOG.info("------------------------------");
			for (int i = messageCount; i > 0; i--) {
				LOG.info("Mail Subject:- " + messages[i].getSubject());
				if(messages[i].getSubject().contains("1392")){
					LOG.info("Found required mail");
					bean = new MessageBean(messages[i].getSubject(), getText(messages[i]), messages[i].getReceivedDate());
					break;
				}
			}
			inbox.close(true);
			store.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return bean;
	}
	
	
	public List<MessageBean> search1392Messages() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -1);
		return searchMessages("1392", cal.getTime());
	}
	
	public List<MessageBean> searchMessages(String readWithSubject, Date skipBeforeDate) {
		Properties props = new Properties();
		Message[] messages = null;
		MessageBean bean = null;
		
		List<MessageBean> result = new ArrayList<MessageBean>();
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("mail.properties"));
			Session session = Session.getInstance(props, null);

			Store store = session.getStore("imaps");
			
			
			store.connect("smtp.gmail.com", email, pass);

			Folder inbox = store.getFolder("inbox");
			inbox.open(Folder.READ_WRITE);
			int messageCount = inbox.getMessageCount();

			LOG.info("Total Messages:- " + messageCount);
			
			ReceivedDateTerm receivedDateTerm = new ReceivedDateTerm(ComparisonTerm.GE, skipBeforeDate);
			SubjectTerm subjectTerm = new SubjectTerm(readWithSubject);
			FlagTerm flagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
			
			AndTerm andTerm = new AndTerm(new SearchTerm[]{flagTerm, receivedDateTerm, subjectTerm});
			//SearchTerm searchTerm = new SearchTermExtension(readWithSubject, skipBeforeDate);
			messages = inbox.search(andTerm); 
			
			LOG.info("------------------------------");
			
			for(Message message: messages){
				LOG.info("Found required mail: "+ message.getSubject() + ", received " + message.getReceivedDate());

				bean = new MessageBean(message.getSubject(), getText(message), message.getReceivedDate());
				result.add(bean);
				inbox.setFlags(new Message[] {message}, new Flags(Flags.Flag.SEEN), true);
			}

			inbox.close(true);
			store.close();
		} catch (Exception e) {
			LOG.error("Failed read mails", e);
		}
		return result;
	}
	
	
	public int getAll1392MessageCount() {
		int count = 0;
		Properties props = new Properties();
		Message[] messages = null;
		
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("mail.properties"));
			Session session = Session.getInstance(props, null);

			Store store = session.getStore("imaps");
			
			
			store.connect("smtp.gmail.com", email, pass);

			Folder inbox = store.getFolder("inbox");
			inbox.open(Folder.READ_ONLY);
			int messageCount = inbox.getMessageCount();

			LOG.info("Total Messages:- " + messageCount);
			
			messages = inbox.getMessages();
			
			LOG.info("------------------------------");
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -1);
			
			for (int i = 0; i < messageCount; i++) {
				LOG.debug("Mail Subject:- " + messages[i].getSubject() + ", received " + messages[i].getReceivedDate());
				if(messages[i].getSubject().contains("1392") && cal.getTime().before(messages[i].getReceivedDate())){
					LOG.info("Found required mail: "+ messages[i].getSubject() + ", received " + messages[i].getReceivedDate());
					count++;
				}
			}
			inbox.close(true);
			store.close();
		} catch (Exception e) {
			LOG.error("Failed read mails", e);
		}
		return count;
	}

    
    /**
     * Return the primary text content of the message.
     */
    private String getText(Part p) throws
                MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }
    
    public void sendMail(String subject, String text) throws IOException {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		final String username = email;
		final String password = pass;

		
		Session session = Session.getInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(autoreportAddress));
			message.setSubject(subject);
			message.setText(text);

			Transport.send(message);

			LOG.info("Mail was sent successfully");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Autowired
    private JavaMailSender javaMailSender;

	
	public void springSendFile(File fileToSend, String mailTo) throws IOException, MessagingException {

		MimeMessage mail = javaMailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(mail, true);
		helper.setTo(mailTo);
		helper.setReplyTo(mailTo);
		helper.setFrom(email);
		helper.setSubject(resultSubject);
		helper.setText(resultText);
		String fileName = attachmentFileName;
		if(Strings.isNullOrEmpty(attachmentFileName)) {
			fileName = fileToSend.getName();
		}
		helper.addAttachment(fileName, fileToSend);

		javaMailSender.send(mail);

		LOG.info("Mail with " + fileToSend.getName() + " file send successfully");
	}

}
