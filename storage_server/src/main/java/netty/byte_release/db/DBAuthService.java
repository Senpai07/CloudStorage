package netty.byte_release.db;

import org.apache.log4j.Logger;

import java.sql.*;

public class DBAuthService {
    public static final String SELECT_ID_FROM_USERS_WHERE_LOGIN_AND_PASSWORD = "SELECT id FROM users WHERE login = ? AND password = ?";
    public static final String UPDATE_USERS_SET_IS_LOGIN_WHERE_LOGIN = "UPDATE users SET isLogin = ? WHERE login = ?";
    public static final String SELECT_ID_FROM_USERS_WHERE_LOGIN_AND_IS_LOGIN_1 = "SELECT id FROM users WHERE login = ? AND isLogin = 1";
    public static final String SELECT_ID_FROM_USERS_WHERE_LOGIN = "SELECT id FROM users WHERE login = ?";
    public static final String INSERT_INTO_USERS_LOGIN_PASSWORD_VALUES = "INSERT INTO users (login , password) VALUES (? , ?)";
    public static final String UPDATE_USERS_SET_IS_LOGIN_0 = "UPDATE users SET isLogin = 0";
    private Connection connection = null;
    private static final Logger logger = Logger.getLogger(DBAuthService.class);
    private static final String URL = "jdbc:sqlite:storage_server/src/main/resources/users.sqlite";

    public void start() {
        try {
            connection = DriverManager.getConnection(URL);
            resetIsLogin();
            logger.info("База данных подключена");
        } catch (SQLException e) {
            logger.fatal("Ошибка подключения к базе данных");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void stop() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isAuthorise(String login, String password) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    SELECT_ID_FROM_USERS_WHERE_LOGIN_AND_PASSWORD
            );
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setIsLogin(String login, boolean isLogin) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    UPDATE_USERS_SET_IS_LOGIN_WHERE_LOGIN
            );
            statement.setInt(1, isLogin ? 1 : 0);
            statement.setString(2, login);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isLogin(String login) {
        return prepareSelect(login, SELECT_ID_FROM_USERS_WHERE_LOGIN_AND_IS_LOGIN_1);
    }

    private boolean prepareSelect(String login, String selectIdFromUsersWhereLoginAndIsLogin1) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    selectIdFromUsersWhereLoginAndIsLogin1
            );
            statement.setString(1, login);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isLoginExist(String login) {
        return prepareSelect(login, SELECT_ID_FROM_USERS_WHERE_LOGIN);
    }

    public void registration(String login, String password) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    INSERT_INTO_USERS_LOGIN_PASSWORD_VALUES
            );
            statement.setString(1, login);
            statement.setString(2, password);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetIsLogin() {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    UPDATE_USERS_SET_IS_LOGIN_0
            );
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
