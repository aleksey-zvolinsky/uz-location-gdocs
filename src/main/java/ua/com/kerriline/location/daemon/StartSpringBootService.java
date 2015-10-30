package ua.com.kerriline.location.daemon;

/**
 * Start a Spring Boot application as a service.
 *
 * @author Stephane Nicoll
 */
public class StartSpringBootService {

	public static void main(String[] args) throws Exception {
		new SpringBootService().start(args);
	}

}
