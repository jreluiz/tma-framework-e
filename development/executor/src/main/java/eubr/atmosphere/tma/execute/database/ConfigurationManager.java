package eubr.atmosphere.tma.execute.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eubr.atmosphere.tma.entity.qualitymodel.Configuration;
import eubr.atmosphere.tma.entity.qualitymodel.ConfigurationPK;
import eubr.atmosphere.tma.database.DatabaseManager;

public class ConfigurationManager {

    public static List<Configuration> obtainConfiguration(int planId, int actionRuleId) {
        List<Configuration> configurationList = new ArrayList<Configuration>();
        
        String sql = "SELECT c.configurationId, " + 
        		"		     c.keyName, " + 
        		"		     c.domain, " + 
        		"		     c.value " + 
        		"	  FROM   Plan p " + 
        		"		     INNER JOIN ActionPlan ap " + 
        		"		             ON p.planId = ap.planId " + 
        		"		     INNER JOIN Configuration c " + 
        		"		             ON c.actionRuleId = ap.actionRuleId " + 
        		"	  WHERE  p.planId = ? " + 
        		"		     AND c.actionRuleId = ?;";
        
        try {
            PreparedStatement ps = DatabaseManager.getConnectionInstance().prepareStatement(sql);
            ps.setInt(1, planId);
            ps.setInt(2, actionRuleId);
            ResultSet rs = DatabaseManager.executeQuery(ps);

            while (rs.next()) {
                int configurationId = (int) rs.getObject("configurationId");
                String keyName = (String) rs.getObject("keyName");
                String domain = (String) rs.getObject("domain");
                String value = (String) rs.getObject("value");
                
                // gera key
                ConfigurationPK configurationPK = new ConfigurationPK();
                configurationPK.setConfigurationId(configurationId);
                configurationPK.setActionRuleId(actionRuleId);
                
                Configuration config = new Configuration(configurationPK, keyName, domain, value);
                
                configurationList.add(config);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return configurationList;
    }
}
