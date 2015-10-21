package ua.com.kerriline.location;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
/**
 * 
 * @author Aleksey
 *
 */
public class SchedulerManager {
	
	
	public void setup() throws SchedulerException {
		SchedulerFactory sf = new StdSchedulerFactory();
		
		Scheduler sched = sf.getScheduler();
		
		
		JobDetail job = newJob(MyJob.class)
			    .withIdentity("job1", "group1")
			    .build();

			CronTrigger trigger = newTrigger()
			    .withIdentity("trigger1", "group1")
			    .withSchedule(cronSchedule("0/20 * * * * ?"))
			    .build();

			sched.scheduleJob(job, trigger);
	}
	
	class MyJob implements org.quartz.Job{

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			//controller.full();
		}
		
	}
}
