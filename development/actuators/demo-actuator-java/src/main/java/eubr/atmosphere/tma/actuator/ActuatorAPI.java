package eubr.atmosphere.tma.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jep.Jep;
import jep.JepException;

@RestController
@RequestMapping("/docker")
public class ActuatorAPI implements Actuator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActuatorAPI.class);

    @RequestMapping("/initialize")
    public void initialize() {
        // This is for the conceptual model
        LOGGER.info("Initialize :)");
    }

    @RequestMapping("/act")
    public void act(@RequestBody ActuatorPayload actuatorPayload) {
        LOGGER.info("Act new!! " + actuatorPayload.toString());
        String action = actuatorPayload.getAction();
        try (@SuppressWarnings("deprecation")
		Jep jep = new Jep(false,
				"/home/jorge/workspace/sts/tma-framework-e/development/actuators/demo-actuator-java/src/main/java/eubr/atmosphere/tma/actuator/docker-actuator.py")) {
			jep.runScript("docker-actuator.py");
			Object ret = jep.invoke("execute_action", action);
			if ((Boolean) ret) {
				System.out.println("--- Ok!");	
			} else {
				System.out.println("--- Problem when performing the action!");
			}
			
		} catch (JepException e) {
			e.printStackTrace();
		}

    }

    @RequestMapping("/register")
    protected void register(Actuator callback) {
        // This is for the conceptual model
        LOGGER.info("Register!");
    }
}
