package ua.com.kerriline.location;

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
public class MileageScheduledJob implements org.quartz.Job{
	
	private static final Logger LOG = LoggerFactory.getLogger(MileageScheduledJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			LOG.info("Job started by schedule");
			MileageManager manager = (MileageManager) context.getMergedJobDataMap().get("MileageManager");
			manager.fullTrip();
			LOG.info("Job by schedule finished");
		} catch (Exception e) {
			LOG.error("Job by schedule failed", e);
		}
		
	}
	
}