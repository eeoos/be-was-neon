package db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    private static final String JDBC_URL = "jdbc:h2:~/test;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static class DatabaseConfigHolder{
        private static final DatabaseConfig INSTANCE = new DatabaseConfig();
    }

    public static DatabaseConfig getInstance() {
        return DatabaseConfigHolder.INSTANCE;
    }

    private DatabaseConfig() {
        try {
            Class.forName("org.h2.Driver");
            initDatabase();
        } catch (ClassNotFoundException e) {
            logger.error("H2 드라이버를 로드할 수 없습니다.", e);
            throw new RuntimeException("데이터베이스 초기화에 실패했습니다.", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    private void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("create table if not exists USERS (" +
                    "id bigint auto_increment primary key," +
                    "user_id varchar(50) not null," +
                    "password varchar(100) not null," +
                    "name varchar(50) not null," +
                    "email varchar(100) not null" +
                    ")");

            stmt.execute("create table if not exists ARTICLES (" +
                    "id bigint auto_increment primary key," +
                    "user_id varchar(50) not null," +
                    "content text not null," +
                    "created_at timestamp default current_timestamp," +
                    "foreign key (user_id) references USERS(user_id)" +
                    ")");

            logger.info("init database");
        } catch (SQLException e) {
            logger.error("데이터베이스 초기화 중 오류 발생",  e);
            throw new RuntimeException("데이터베이스 초기화에 실패", e);
        }

    }
}
