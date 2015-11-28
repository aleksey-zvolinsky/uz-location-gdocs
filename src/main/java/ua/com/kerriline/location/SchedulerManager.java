package ua.com.kerriline.location;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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
	private String locationCronExpression = "0 45 7,13 ? * *I";
	
	@Value("${mileage.schedule}")
	private String mileageCronExpression = "0 45 5 ? * *I";
	
	@Autowired LocationManager location;
	@Autowired MileageManager mileage;

	private Scheduler sched;
	
	@PostConstruct
	public void setup() throws SchedulerException {
		try {
			SchedulerFactory sf = new StdSchedulerFactory();
			sched = sf.getScheduler();
			locationTrigger();
			mileageTrigger();
			sched.start();
			LOG.info("Scheduler was setup successfully");
		} catch (Exception e) {
			LOG.error("Failed to setup scheduler", e);
		}
	}

	
	@PreDestroy
	public void close() throws SchedulerException {
		sched.shutdown();
	}
	
	private void locationTrigger() throws SchedulerException {
		JobDataMap newJobDataMap = new JobDataMap();
		newJobDataMap.put("LocationManager", location);
		
		JobDetail job = newJob(LocationScheduledJob.class)
				.usingJobData(newJobDataMap)
				.withIdentity("job1", "group1")
				.build();

		CronTrigger trigger = newTrigger()
				.withIdentity("trigger1", "group1")
				.withSchedule(cronSchedule(locationCronExpression))
				.build();

		sched.scheduleJob(job, trigger);
	}
	
	private void mileageTrigger() throws SchedulerException {
		JobDataMap newJobDataMap = new JobDataMap();
		newJobDataMap.put("MileageManager", mileage);
		
		JobDetail job = newJob(MileageScheduledJob.class)
				.usingJobData(newJobDataMap)
				.withIdentity("job2", "group2")
				.build();

		CronTrigger trigger = newTrigger()
				.withIdentity("trigger2", "group2")
				.withSchedule(cronSchedule(mileageCronExpression))
				.build();

		sched.scheduleJob(job, trigger);
	}
}
