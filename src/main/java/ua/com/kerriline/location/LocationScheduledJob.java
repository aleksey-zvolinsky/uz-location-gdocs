package ua.com.kerriline.location;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author Aleksey
 *
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class LocationScheduledJob implements org.quartz.Job{
	
	private static final Logger LOG = LoggerFactory.getLogger(SchedulerManager.class);
	
	private static final int MAX_ATTEMPTS = 10;
	// 10 minutes
	private static final int TIME_BEFORE_NEW_ATTEMPT = 10*60*1000;
	
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			LOG.info("Job started by schedule");
			LocationManager location = (LocationManager) context.getMergedJobDataMap().get("LocationManager");
			location.fullTrip();
			LOG.info("Job by schedule finished");
		} catch (Exception e) {
			LOG.error("Job by schedule failed", e);
			int attempt = incAttempt(context);
			LOG.error("It was {} attempt", attempt);
			JobExecutionException exception = new JobExecutionException(e);
			if(attempt < MAX_ATTEMPTS) {
				exception.setRefireImmediately(true);
				try {
					Thread.sleep(TIME_BEFORE_NEW_ATTEMPT);
				} catch (InterruptedException e1) {
					LOG.error("Failed to make explicit wait", e1);
				}
				throw exception;
			} else {
				LOG.error("Reached max attempts. Job will be launched as per schedule");
			}
			
		}
		
	}
	
	
	private int incAttempt(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        if(!dataMap.containsKey("count")) {
        	dataMap.put("count", 0);
        }
        int count = dataMap.getIntValue("count");
        dataMap.put("count", count++); 
        return count;
	}
}