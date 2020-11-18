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

import eubr.atmosphere.tma.entity.qualitymodel.ActionPlan;
import eubr.atmosphere.tma.entity.qualitymodel.ActionPlanStatus;
import eubr.atmosphere.tma.entity.qualitymodel.ActionRule;
import eubr.atmosphere.tma.entity.qualitymodel.Actuator;
import eubr.atmosphere.tma.entity.qualitymodel.Configuration;
import eubr.atmosphere.tma.entity.qualitymodel.Plan;
import eubr.atmosphere.tma.entity.qualitymodel.PlanStatus;
import eubr.atmosphere.tma.execute.database.ActionPlanManager;
import eubr.atmosphere.tma.execute.database.ActionRuleManager;
import eubr.atmosphere.tma.execute.database.ActuatorManager;
import eubr.atmosphere.tma.execute.database.ConfigurationManager;
import eubr.atmosphere.tma.execute.database.PlanManager;
import eubr.atmosphere.tma.execute.utils.PropertiesManager;
import eubr.atmosphere.tma.execute.utils.RestServices;

public class Main 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main( String[] args ) {
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
        Plan plan = PlanManager.obtainPlanByPlanId(planId);
        
        // Verify if the plan status is READY_TO_RUN
        if (planId == -1 || PlanStatus.valueOf(plan.getStatus()) != PlanStatus.READY_TO_RUN) {
            return;
        }
        
        List<ActionPlan> actionPlanList = ActionPlanManager.obtainActionPlanByPlanId(planId);

        // Change the status of the plan to IN_PROGRESS
        PlanManager.updatePlanStatusByPlanId(plan, PlanStatus.IN_PROGRESS.ordinal());
        
        for (ActionPlan actionPlan: actionPlanList) {
            
        	ActionRule actionRule = ActionRuleManager.obtainActionRuleById(actionPlan.getId().getActionRuleId());
            
            List<Configuration> configList = ConfigurationManager.obtainConfiguration(planId, actionRule.getActionRuleId());
            for (Configuration config: configList) {
                actionRule.addConfiguration(config);
            }

            // Change the status of the action to in RUNNING
            actionPlan.setStatus(ActionPlanStatus.RUNNING.ordinal());
            
            Actuator actuator = ActuatorManager.obtainActuatorByAction(actionRule);
            if (actuator != null) {
                act(actuator, actionRule);
                // Change the status of the action to EXECUTED
                actionPlan.setStatus(ActionPlanStatus.EXECUTED.ordinal());
            } else {
                LOGGER.warn("Actuator not found: (ActuatorId = {})", actionRule.getActuator().getActuatorId());
            }
        }
        
        // Change the status of the plan COMPLETED
        PlanManager.updatePlanStatusByPlanId(plan, PlanStatus.COMPLETED.ordinal());
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void act(Actuator actuator, ActionRule action) {
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
