package eubr.atmosphere.tma.execute.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import eubr.atmosphere.tma.database.DatabaseManager;
import eubr.atmosphere.tma.entity.qualitymodel.Plan;

public class PlanManager {

    public static Plan obtainPlanByPlanId(int planId) {
        Plan plan = null;

        String sql = "select valueTime, status from Plan where planId = ? ;";
        
        try {
            PreparedStatement ps = DatabaseManager.getConnectionInstance().prepareStatement(sql);
            ps.setInt(1, planId);
            ResultSet rs = DatabaseManager.executeQuery(ps);

            while (rs.next()) {
            	Timestamp valueTime = (Timestamp) rs.getObject("valueTime");
                int status = (int) rs.getObject("status");
                
                plan = new Plan(planId, status, valueTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plan;
    }
    
    public static int updatePlanStatusByPlanId(Plan plan, int newStatus) {
        int ret = 0;
        plan.setStatus(newStatus);

        String sql = "update Plan set status = ? where planId = ? ;";
        
        try {
            PreparedStatement ps = DatabaseManager.getConnectionInstance().prepareStatement(sql);
            ps.setInt(1, newStatus);
            ps.setInt(2, plan.getPlanId());
            
            DatabaseManager databaseManager = new DatabaseManager();
            ret = databaseManager.execute(ps);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }
}
