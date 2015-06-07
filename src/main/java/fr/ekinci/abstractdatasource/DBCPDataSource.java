package fr.ekinci.abstractdatasource;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;


/**
 * Apache DBCP Benchmark implementation
 * see configuration here : http://commons.apache.org/proper/commons-dbcp/configuration.html
 * 
 * Advancement : 100%
 * 
 * @author Gokan EKINCI
 */ 
public class DBCPDataSource extends AbstractDataSource {   
    private BasicDataSource dbcpDS; // Keep reference for close() method
    
    public DBCPDataSource(
        final String nodeIp,
        final int nodePort,
        final int numberOfConnectionsPerDataSource,
        final String sqlDriverName
    ) {
        super(nodeIp, nodePort, numberOfConnectionsPerDataSource, sqlDriverName);
        dataSource = setup();
    }

    /**
     * Specific method for Apache DBCP initialization 
     * 
     * @return
     */
    private DataSource setup() {
        dbcpDS = new BasicDataSource();
        
        // ex: jdbc:postgresql://<host>:<post_number>/<base_name>
        dbcpDS.setDriverClassName(driverName);
        dbcpDS.setUrl(this.getJdbcUrl());
        dbcpDS.setUsername(username);
        dbcpDS.setPassword(password);
        
        // Size of pool
        dbcpDS.setInitialSize(minNumberOfConnections);
        dbcpDS.setMaxActive(maxNumberOfConnections);
        
        return dbcpDS;    
    }

    @Override
    public void close() throws SQLException {
        dbcpDS.close();
    }

}
