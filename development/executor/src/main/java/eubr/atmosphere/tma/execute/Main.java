package eubr.atmosphere.tma.execute;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.data.Action;
import eubr.atmosphere.tma.data.ActionPlan;
import eubr.atmosphere.tma.data.Actuator;
import eubr.atmosphere.tma.data.Configuration;
import eubr.atmosphere.tma.entity.qualitymodel.ActionRule;
import eubr.atmosphere.tma.execute.database.ActionManager;
import eubr.atmosphere.tma.execute.database.ActionPlanManager;
import eubr.atmosphere.tma.execute.database.ActuatorManager;
import eubr.atmosphere.tma.execute.database.ConfigurationManager;
import eubr.atmosphere.tma.execute.utils.PropertiesManager;
import eubr.atmosphere.tma.execute.utils.RestServices;

public class Main 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main( String[] args )
    {
        runConsumer();
    }

    private static void runConsumer() {

        Consumer<Long, String> consumer = ConsumerCreator.createConsumer();
        int noMessageFound = 0;
        int maxNoMessageFoundCount = Integer.parseInt(
                PropertiesManager.getInstance().getProperty("maxNoMessageFoundCount"));

        try {
            while (true) {

              ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);

              // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
              if (consumerRecords.count() == 0) {
                  noMessageFound++;

                  if (noMessageFound > maxNoMessageFoundCount) {
                    // If no message found count is reached to threshold exit loop.
                      sleep(2000);
                  } else {
                      continue;
                  }
              }

              // Manipulate the records
              consumerRecords.forEach(record -> {
                  handlePlan(record);
               });

              // commits the offset of record to broker.
              consumer.commitAsync();
              sleep(5000);
            }
        } finally {
            consumer.close();
        }
    }

    private static void handlePlan(ConsumerRecord<Long, String> record) {
        
    	LOGGER.info(record.toString());
        
        String stringPlanId = record.value();
        Integer planId = Integer.parseInt(stringPlanId);
        
        if (planId == -1) // TODO Verify if the plan status is TO_DO
            return;
        
        List<ActionPlan> actionPlanList = ActionPlanManager.obtainActionPlanByPlanId(planId);

        // TODO Change the status of the plan to in progress
        for (ActionPlan actionPlan: actionPlanList) {
            ActionRule action = ActionManager.obtainActionById(actionPlan.getActionId());
            List<Configuration> configList =
                    ConfigurationManager.obtainConfiguration(planId, actionPlan.getActionId());
            for (Configuration config: configList) {
                action.addConfiguration(config);
            }

            // TODO Change the status of the action to in progress
            Actuator actuator = ActuatorManager.obtainActuatorByAction(action);
            if (actuator != null) {
                act(actuator, action);
                // TODO Change the status of the action to completed
            } else {
                LOGGER.warn("Actuator not found: (ActuatorId = {})", action.getActuatorId());
            }
        }
        // TODO Change the status of the plan to completed
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void act(Actuator actuator, Action action) {
        // Request the service from the actuator to perform the adaptation
        try {
            RestServices.requestRestService(actuator, action);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
