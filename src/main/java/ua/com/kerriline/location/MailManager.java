package ua.com.kerriline.location;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	public MessageBean getLast1392() {
		Properties props = new Properties();
		Message[] messages = null;
		MessageBean bean = null;
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("mail.properties"));
			Session session = Session.getDefaultInstance(props, null);

			Store store = session.getStore("imaps");
			store.connect("smtp.gmail.com", "sledline@gmail.com", "k5666031");

			Folder inbox = store.getFolder("inbox");
			inbox.open(Folder.READ_ONLY);
			int messageCount = inbox.getMessageCount();

			LOG.info("Total Messages:- " + messageCount);

			messages = inbox.getMessages();
			LOG.info("------------------------------");
			for (int i = 0; i < messageCount; i++) {
				LOG.info("Mail Subject:- " + messages[i].getSubject());
				if(messages[i].getSubject().contains("1392")){
					LOG.info("Found required mail");
					textIsHtml = false;
					bean = new MessageBean(messages[i].getSubject(), getText(messages[i]));
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

    private boolean textIsHtml = false;
    
    /**
     * Return the primary text content of the message.
     */
    private String getText(Part p) throws
                MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            textIsHtml = p.isMimeType("text/html");
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
}
