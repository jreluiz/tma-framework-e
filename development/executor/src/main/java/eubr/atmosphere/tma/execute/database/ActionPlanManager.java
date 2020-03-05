package eubr.atmosphere.tma.execute.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eubr.atmosphere.tma.database.DatabaseManager;
import eubr.atmosphere.tma.entity.qualitymodel.ActionPlan;

public class ActionPlanManager {

    public static List<ActionPlan> obtainActionPlanByPlanId(int planId) {
        // TODO: This method can go to TMA-K component
        List<ActionPlan> actionPlanList = new ArrayList<ActionPlan>();

        String sql = "select actionId, executionOrder from ActionPlan where planId = ? ;";
        
        try {
            PreparedStatement ps = DatabaseManager.getConnectionInstance().prepareStatement(sql);
            ps.setInt(1, planId);
            ResultSet rs = DatabaseManager.executeQuery(ps);

            while (rs.next()) {
                int actionId = (int) rs.getObject("actionId");
                int executionOrder = (int) rs.getObject("executionOrder");
                
                ActionPlan actionPlan = new ActionPlan(planId, actionId, executionOrder);
                actionPlanList.add(actionPlan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return actionPlanList;
    }
}
