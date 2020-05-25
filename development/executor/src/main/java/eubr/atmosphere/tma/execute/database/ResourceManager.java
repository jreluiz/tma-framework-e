package eubr.atmosphere.tma.execute.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.database.DatabaseManager;
import eubr.atmosphere.tma.entity.qualitymodel.Resource;

public class ResourceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManager.class);
    
    public static Resource obtainActuatorById(Integer resourceId) {
        Resource resource = null;

        String sql = "select resourceName, resourceType, resourceAddress from Resource where resourceId = ? ;";
        
        try {
            PreparedStatement ps = DatabaseManager.getConnectionInstance().prepareStatement(sql);
            ps.setInt(1, resourceId);
            ResultSet rs = DatabaseManager.executeQuery(ps);

            if (rs.next()) {
                String resourceName = (String) rs.getObject("resourceName");
                String resourceType = (String) rs.getObject("resourceType");
                String resourceAddress = (String) rs.getObject("resourceAddress");
                LOGGER.info(resourceName);
                LOGGER.info(resourceType);
                LOGGER.info(resourceAddress);
                resource = new Resource(resourceId, resourceAddress, resourceName, resourceType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resource;
    }
}
