package fr.ekinci.abstractdatasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Logger;
import javax.sql.DataSource;


/**
 * A class which make code factorization for DataSource implementation
 * Limitation : Only 1 BASE per deployed WAR (Web application)
 * 
 * Advancement : 100%
 * Tests : Need database connection, complete version of tests in another project : mock-postgresql-rendez_vous
 * 
 * @author Gokan EKINCI
 * @author Christian DAGO
 */ 
public abstract class AbstractDataSource implements DataSource {
    protected DataSource dataSource;
       
    // DataSource initialization values :
    protected final String nodeHost; // ipv4 format
    protected final int nodePort;
    protected final String baseName; 
    protected final String jdbcUrl;   
    protected final String username;
    protected final String password; 
    protected final String driverName;
    protected final int minNumberOfConnections;
    protected final int maxNumberOfConnections;
    
    // Check jvm arguments
    private final static String JDBC_RDBMS = "jdbc:postgresql://";
    private final static String REGEX_NODE_ID = "([0-9]{1,3}\\.){3}[0-9]{1,3}:[0-9]{1,5}";
    private final static String REGEX_BASE_NAME = "[a-zA-Z][a-zA-Z0-9_]{0,}";
    
    /* If result == false => Master | if result == true => Slave */
    private final static String SQL_REQUEST_IS_IN_RECOVERY = "SELECT pg_is_in_recovery()";
    
    protected AbstractDataSource(
        final String nodeHost,
        final int nodePort,
        final int numberOfConnectionsPerDataSource,
        final String sqlDriverName
    ){
        if(numberOfConnectionsPerDataSource < 1){
            throw new IllegalArgumentException("numberOfConnectionsPerDataSource must be superior to 0");
        }
        
        this.nodeHost = nodeHost;
        this.nodePort = nodePort;
        this.minNumberOfConnections = numberOfConnectionsPerDataSource;
        this.maxNumberOfConnections = numberOfConnectionsPerDataSource;
        this.driverName = sqlDriverName;
                
        // Setted by user :
        baseName = System.getProperty("pw_sql_base_name");
        username = System.getProperty("pw_sql_username");
        password = System.getProperty("pw_sql_password");
        
        this.jdbcUrl = constructJdbcUrl(nodeHost, nodePort, baseName);
    }
        
    
    @Override
    public String toString() {
        return "Partner's username : " + username + "\n"
                + "Connected to this nodeId : " + nodeHost + ":" + nodePort + "\n"
                + "basename : " + baseName + "\n"
                + "Authorized number of connection for current DataSource : " + maxNumberOfConnections;            
    }



    /**
     * @return nodeId
     */
    public String getNodeId(){
        return nodeHost + ":" + nodePort;
    }
    
    
    /**
     * Close current DataSource
     * 
     * @throws SQLException
     */
    public abstract void close() throws SQLException;
    
    
    /** 
     * Check if current DataSource's RDBMS node is slave
     * 
     * @return true if it's slave, false if it's master
     * @throws SQLException  if an SQL error happen during checking RDBMS node is slave or master
     */
    public boolean isSlave() throws SQLException {
        // Check RDBMS machine type           
        boolean isSlave = true; // Is slave by default
        
        // Try-with-resources, connection will automatically be closed (returned to the pool)
        try(
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(SQL_REQUEST_IS_IN_RECOVERY)
        ){
            if(rs.next()){
                isSlave = rs.getBoolean(1); // false = Master | true = Slave
            }
        }    
        
        return isSlave;
    }
    
    
    
    /**
     * Construct and return a JDBC URL
     * Format should be -> jdbc:postgresql://<host>:<post_number>/<base_name>
     * @return
     * @throws IllegalArgumentException
     */
    public String constructJdbcUrl(String nodeHost, int nodePort, String baseName) throws IllegalArgumentException {
        String jdbcChain = JDBC_RDBMS + nodeHost + ":" + nodePort + "/" + baseName;
        String regex = JDBC_RDBMS + REGEX_NODE_ID + "/" + REGEX_BASE_NAME;
        
        if(!jdbcChain.matches(regex)){
            throw new IllegalArgumentException("JDBC Chain is not correct : " + jdbcChain 
                    + ", please check your pw_sql_*** jvm arguments !");
        }
        
        return jdbcChain;
    }
    
    
    
    /**
     * Get jdbcUrl
     * @return
     */
    public String getJdbcUrl() {       
        return jdbcUrl;
    }
    
    
    @Override
    public Connection getConnection() throws SQLException {
        if(dataSource != null){
            return dataSource.getConnection();
        } else {
            // This method does not terminate with return (and it's normal)
            throw new NullPointerException("DataSource is null !"); 
        }
    }


    @Override
    public Connection getConnection(String arg0, String arg1) throws SQLException {
        return getConnection();
    }
    
    
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        if(dataSource != null){
            return dataSource.getLogWriter();
        } else {
            // This method does not terminate with return (and it's normal)
            throw new NullPointerException("DataSource is null !"); 
        } 
    }

    
    @Override
    public int getLoginTimeout() throws SQLException {
        if(dataSource != null){
            return dataSource.getLoginTimeout();
        } else {
            // This method does not terminate with return (and it's normal)
            throw new NullPointerException("DataSource is null !"); 
        }  
    }


    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        if(dataSource != null){
            return dataSource.getParentLogger();
        } else {
            // This method does not terminate with return (and it's normal)
            throw new NullPointerException("DataSource is null !"); 
        } 
    }


    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        if(dataSource != null){
            dataSource.setLogWriter(out);
        } else {
            // This method does not terminate with return (and it's normal)
            throw new NullPointerException("DataSource is null !"); 
        }           
    }


    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        if(dataSource != null){
            dataSource.setLoginTimeout(seconds);
        } else {
            // This method does not terminate with return (and it's normal)
            throw new NullPointerException("DataSource is null !"); 
        }        
    }


    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if(dataSource != null){
            return dataSource.isWrapperFor(iface);
        } else {
            // This method does not terminate with return (and it's normal)
            throw new NullPointerException("DataSource is null !"); 
        }
    }


    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if(dataSource != null){
            return dataSource.unwrap(iface);
        } else {
            // This method does not terminate with return (and it's normal)
            throw new NullPointerException("DataSource is null !"); 
        }
    }
    
}
