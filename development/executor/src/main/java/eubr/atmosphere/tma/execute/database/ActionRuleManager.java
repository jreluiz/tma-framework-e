package eubr.atmosphere.tma.execute.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import eubr.atmosphere.tma.entity.qualitymodel.ActionRule;
import eubr.atmosphere.tma.entity.qualitymodel.Actuator;
import eubr.atmosphere.tma.entity.qualitymodel.Resource;
import eubr.atmosphere.tma.database.DatabaseManager;

public class ActionRuleManager {

    public static ActionRule obtainActionRuleById(int actionRuleId) {
        ActionRule actionRule = null;

        String sql = "select actionName, resourceId, actuatorId from ActionRule where actionRuleId = ? ;";
        
        try {
            PreparedStatement ps = DatabaseManager.getConnectionInstance().prepareStatement(sql);
            ps.setInt(1, actionRuleId);
            ResultSet rs = DatabaseManager.executeQuery(ps);

            if (rs.next()) {
                String actionName = (String) rs.getObject("actionName");
                
                int resourceId = (int) rs.getObject("resourceId");
                Resource resource = ResourceManager.obtainActuatorById(resourceId);
                int actuatorId = (int) rs.getObject("actuatorId");
                Actuator actuator = ActuatorManager.obtainActuatorById(actuatorId);
                
                actionRule = new ActionRule(actionRuleId, actionName, actuator, resource);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return actionRule;
    }
}
