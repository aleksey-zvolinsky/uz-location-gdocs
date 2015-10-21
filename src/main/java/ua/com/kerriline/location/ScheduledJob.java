package ua.com.kerriline.location;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
/**
 * 
 * @author Aleksey
 *
 */
@DisallowConcurrentExecution
public class ScheduledJob implements org.quartz.Job{
	
	private static final Log LOG = LogFactory.getLog(SchedulerManager.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			LOG.info("Job started by schedule");
			LocationManager location = (LocationManager) context.getMergedJobDataMap().get("LocationManager");
			location.fulltrip();
			LOG.info("Job by schedule finished");
		} catch (Exception e) {
			LOG.error("Job by schedule failed", e);
		}
		
	}
	
}