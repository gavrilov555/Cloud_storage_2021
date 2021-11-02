package serverVariants.Netty;

import java.sql.*;
import java.util.List;

public class DBAuthService implements AuthService{

    private static DBAuthService INSTANCE;

    private static String DB_URL="jdbc:mysql://127.0.0.1:3306/mysql";
    private static String USER="root";
    private static String PASS="123456";
    private static Connection connection;

    public static DBAuthService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DBAuthService();
        }
        return INSTANCE;
    }

    DBAuthService() {

        try {
            connect();

        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }
    }

    public static void connect () throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        System.out.println("PostgreSQL JDBC Driver successfully connected");
        connection = DriverManager.getConnection(DB_URL,USER,PASS);
        System.out.println("Success connected to DB");
    }
    public static void disconnect () {
        if (connection!=null){
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }}
    }



    @Override
    public String findByLogin(String login) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format("SELECT password FROM USERS WHERE login = " + "'%s';", login));
            {
                if (!resultSet.next()) return null;
                else {
                    String password = resultSet.getString("password");
                    return password;
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();

        }
        return null;
    }

    @Override
    public Object save(Object object) {
        return null;
    }

    @Override
    public Object remove(Object object) {
        return null;
    }

    @Override
    public Object removeById(Object o) {
        return null;
    }

    @Override
    public Object findById(Object o) {
        return null;
    }

    @Override
    public List findAll() {
        return null;
    }
}
