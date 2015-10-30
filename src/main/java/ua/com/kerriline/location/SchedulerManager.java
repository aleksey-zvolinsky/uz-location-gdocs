package ua.com.kerriline.location;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
/**
 * 
 * @author Aleksey
 *
 */
public class SchedulerManager {
	
	private static final Log LOG = LogFactory.getLog(SchedulerManager.class);
	
	@Value("${schedule}")
	private String cronExpression = "0 45 7,13 ? * *I";
	
	@Autowired LocationManager location;

	private Scheduler sched;
	
	@PostConstruct
	public void setup() throws SchedulerException {
		try {
			SchedulerFactory sf = new StdSchedulerFactory();

			sched = sf.getScheduler();

			JobDataMap newJobDataMap = new JobDataMap();
			newJobDataMap.put("LocationManager", location);
			
			JobDetail job = newJob(ScheduledJob.class)
					.usingJobData(newJobDataMap)
					.withIdentity("job1", "group1")
					.build();

			CronTrigger trigger = newTrigger()
					.withIdentity("trigger1", "group1")
					.withSchedule(cronSchedule(cronExpression))
					.build();

			sched.scheduleJob(job, trigger);
			sched.start();
			LOG.info("Scheduler was setup successfully with " + cronExpression + " cron schedule");
		} catch (Exception e) {
			LOG.error("Failed to setup scheduler", e);
		}
	}
}
