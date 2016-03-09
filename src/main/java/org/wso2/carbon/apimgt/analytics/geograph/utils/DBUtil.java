/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.analytics.geograph.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.analytics.geograph.api.Location;
import org.wso2.carbon.apimgt.analytics.geograph.exception.GeoLocationResolverException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;

public class DBUtil {
    private static final DBUtil dbUtil = new DBUtil();
    private static volatile DataSource dataSource = null;
    private static String dataSourceName;
    private static final Log log = LogFactory.getLog(DBUtil.class);

    private DBUtil() {
    }

    public static DBUtil getInstance() {
        return dbUtil;
    }

    public static void initialize() throws GeoLocationResolverException {
        if (dataSource != null) {
            return;
        }

        synchronized (DBUtil.class) {
            if (dataSource == null) {
                try {
                    Context ctx = new InitialContext();
                    dataSource = (DataSource) ctx.lookup(dataSourceName);
                } catch (NamingException e) {
                    String msg = "Couldn't find JDBC Data Source from Data source name" + dataSourceName;
                    throw new GeoLocationResolverException(msg,e);
                }
            }
        }
    }

    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws java.sql.SQLException if failed to get Connection
     */
    public  Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("Data source is not configured properly.");
    }

    /**
     * Utility method to close the connection streams.
     *
     * @param preparedStatement PreparedStatement
     * @param connection        Connection
     * @param resultSet         ResultSet
     */
    public  void closeAllConnections(PreparedStatement preparedStatement, Connection connection,
                                           ResultSet resultSet) {
        closeConnection(connection);
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
    }

    /**
     * Close Connection
     *
     * @param dbConnection Connection
     */
    private  void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
               log.error("Couldn't close connection",e);
            }
        }
    }

    /**
     * Close ResultSet
     *
     * @param resultSet ResultSet
     */
    private  void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("Couldn't close ResultSet",e);
            }
        }

    }

    /**
     * Close PreparedStatement
     *
     * @param preparedStatement PreparedStatement
     */
    public  void closeStatement(Statement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Couldn't close Statement",e);
            }
        }

    }

    public Location getLocation(long ipLong) throws GeoLocationResolverException {
        String sql = "SELECT loc.country_name,loc.city_name FROM blocks AS block , location AS loc WHERE " + ipLong +
                " BETWEEN block.network AND block.broadcast AND block.geoname_id=loc.geoname_id";
        Location location = new Location();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            initialize();
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                location.setCity(resultSet.getString("city_name"));
                location.setCountry(resultSet.getString("country_name"));
            }
        } catch (SQLException e) {
            throw new GeoLocationResolverException(e.getMessage(), e);
        } finally {
            closeConnection(connection);
            closeStatement(statement);
            closeResultSet(resultSet);
        }
        return location;
    }

    public void setDataSourceName(String dataSourceName) {
        DBUtil.dataSourceName = dataSourceName;
    }

}
