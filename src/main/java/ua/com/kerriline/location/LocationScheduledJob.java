package ua.com.kerriline.location;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.mail.MessagingException;

import com.google.gdata.util.ServiceException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author Aleksey
 *
 */
@DisallowConcurrentExecution
public class LocationScheduledJob implements org.quartz.Job{
	
	private static final Logger LOG = LoggerFactory.getLogger(SchedulerManager.class);

	private LocationManager location;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			LOG.info("Job started by schedule");
			location = (LocationManager) context.getMergedJobDataMap().get("LocationManager");
			retryExecute(3);
			
			LOG.info("Job by schedule finished");
		} catch (Exception e) {
			LOG.error("Job by schedule failed", e);
		}
		
	}
	
	public void retryExecute(int retries) throws Exception {
		location.fullTrip();
	}
	
}