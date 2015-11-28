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
public class MileageScheduledJob implements org.quartz.Job{
	
	private static final Log LOG = LogFactory.getLog(MileageScheduledJob.class);
	
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