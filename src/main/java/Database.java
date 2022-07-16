import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    private Config config;
    public Connection conn;

    public Database(Config config){
        this.config = config;
        Properties connectionProps = new Properties();
        connectionProps.put("user", config.databaseUser);
        connectionProps.put("password", config.databasePassword);
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://" +
                            config.databaseAddress +
                            ":" + config.databasePort + "/" + config.databaseName,
                    connectionProps);
        } catch (SQLException e) {
            System.out.println(Constants.NETWORK     + "Failed to connected to database: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println(Constants.NETWORK + "Connected to database");
    }

    public Connection getConn() {
        return conn;
    }
}
