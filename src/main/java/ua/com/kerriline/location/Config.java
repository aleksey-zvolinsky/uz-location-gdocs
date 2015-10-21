package ua.com.kerriline.location;

import org.quartz.SchedulerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:config.properties")
@Import(WebController.class)
public class Config {
	
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
	
	@Bean
	public MailManager mailManager() {
		return new MailManager();
	}
	
	@Bean
	public MailParser mailParser() {
		return new MailParser();
	}
	
	@Bean
	public GDocsSheet gDocsSheet() {
		return new GDocsSheet();
	}
	
	public SchedulerManager schedulerManager() throws SchedulerException {
		SchedulerManager manager = new SchedulerManager();
//		manager.setup();
		return manager;
	}
}
