package com.kingrealzyt.cbot.database;

import com.kingrealzyt.cbot.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SQLiteDataSource implements DatabaseManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteDataSource.class);
    private final HikariDataSource ds;

    public SQLiteDataSource() {
        try {
            final File dbFile = new File("database.db");

            if (!dbFile.exists()) {
                if (dbFile.createNewFile()) {
                    LOGGER.info("Created database file");
                } else {
                    LOGGER.info("Could not create database file");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:database.db");
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);

        try
        {
            final PreparedStatement statement = getConnection().prepareStatement(String.format("CREATE TABLE IF NOT EXISTS guild_settings (id INTEGER PRIMARY KEY AUTOINCREMENT, guild_id VARCHAR(20) NOT NULL, prefix VARCHAR(255) NOT NULL DEFAULT '%s')", Config.PREFIX));
            statement.executeUpdate();
            statement.close();
            LOGGER.info("Table initialised");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPrefix(long guildId) {
        try (final PreparedStatement preparedStatement = getConnection()
                // language = SQLite
                .prepareStatement("SELECT prefix FROM guild_settings WHERE guild_id = ?")) {

            preparedStatement.setString(1, String.valueOf(guildId));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("prefix");
                }
            }

            try (final PreparedStatement insertStatement = getConnection()
                    // language=SQLite
                    .prepareStatement("INSERT INTO guild_settings(guild_id) VALUES(?)")) {

                insertStatement.setString(1, String.valueOf(guildId));

                insertStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Config.PREFIX;
    }

    @Override
    public void setPrefix(long guildId, String newPrefix) {

        try (final PreparedStatement preparedStatement = getConnection()
                // Language = SQLite
                .prepareStatement("UPDATE guild_settings SET prefix = ? WHERE guild_id = ?")) {

            preparedStatement.setString(1, newPrefix);
            preparedStatement.setString(2, String.valueOf(guildId));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getBcId(long guildId) {
        try (final PreparedStatement preparedStatement = getConnection()
                // language = SQLite
                .prepareStatement("SELECT bcid FROM guild_settings WHERE guild_id = ?")) {

            preparedStatement.setString(1, String.valueOf(guildId));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return  resultSet.getString("bcid");
                }
            }

            try (final PreparedStatement insertStatement = getConnection()
                    // Language = SQLite
                    .prepareStatement("INSERT INTO guild_settings(guild_id) VALUES(?)")) {
                insertStatement.setString(1, String.valueOf(guildId));

                insertStatement.execute();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "None";
    }

    @Override
    public void setBcId(long guildId, String newBcId) {
        try (final PreparedStatement preparedStatement = getConnection()
                // Language = SQLite
                .prepareStatement("UPDATE guild_settings SET bcid = ? WHERE guild_id = ?")) {

            preparedStatement.setString(1, newBcId);
            preparedStatement.setString(2, String.valueOf(guildId));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean doesBcidExist(String guildID) {
        try {
            PreparedStatement statement = getConnection().prepareStatement("SELECT EXISTS(SELECT Bcid FROM guild_settings WHERE guild_id=?)");
            statement.setString(1, guildID);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            resultSet.next();
            boolean exists = resultSet.getBoolean(1);
            statement.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
