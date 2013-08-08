package de.kp.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.common.IOUtils;

import com.google.common.collect.Sets;

public class MySQLConnector {

	public static String DB_HOST = "host";
	public static String DB_PORT = "port";
	public static String DB_NAME = "database";
	
	public static String DB_USER = "user";
	public static String DB_PASS = "pass";
	
	public static String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

	public ResultSet resultSet;
	public Statement statement;
	
	private JDBCDataModel dataModel;
	private BasicDataSource dataSource;

	/*
	 * SQL for preferences
	 */
	private static final String CREATE_PREFERENCES = "CREATE TABLE taste_preferences ("
			+ "user_id bigint(20) NOT NULL, item_id bigint(20) NOT NULL, preference float NOT NULL, PRIMARY KEY (user_id,item_id), KEY item_id (item_id));";
	
	private static final String INSERT_PREFERENCES = "INSERT INTO taste_preferences (user_id, item_id, preference) VALUES (?, ?, ?) "
			+ "ON DUPLICATE KEY UPDATE preference = VALUES(preference)";

	/*
	 * SQL for filters
	 */
	private static final String CREATE_FILTERS = "CREATE TABLE taste_filters (label varchar(255) NOT NULL, item_id bigint(20) NOT NULL, PRIMARY KEY (label,item_id));";
	
	private static final String INSERT_FILTERS = "INSERT INTO taste_filters (label, item_id) VALUES (?, ?)";
	private static final String REMOVE_FILTER = "DELETE FROM taste_filters WHERE label = ? AND item_id = ?";

	private static final String REMOVE_ALL_FILTERS = "DELETE FROM taste_filters WHERE label = ?";

	private static final String GET_FILTERS = "SELECT item_id FROM taste_filters WHERE label = ?";

	public MySQLConnector(HashMap<String,String> properties) {	

	    BasicDataSource ds = new BasicDataSource();
	    ds.setDriverClassName(MYSQL_DRIVER);

		/*
		 * url
		 */
		String endpoint = getEndpoint(properties);

		/*
		 * credentials
		 */
		String alias   = properties.get(DB_USER);
		String keypass = properties.get(DB_PASS);
	    
	    /*
	     * set data source from configuration
	     */
	    ds.setUrl(endpoint);
	    ds.setUsername(alias);
	    if (keypass.equals("") == false) ds.setPassword(keypass);

	    /*
	     * fixed parameters
	     */
	    ds.setMaxActive(10);
	    ds.setMinIdle(5);
	    
	    ds.setInitialSize(5);
	    ds.setValidationQuery("SELECT 1;");
	    
	    ds.setTestOnBorrow(false);
	    ds.setTestOnReturn(false);
	    
	    ds.setTestWhileIdle(true);
	    ds.setTimeBetweenEvictionRunsMillis(5000);

	    /*
	     * set data model (mahout) and register data source
	     */
	    dataModel = new MySQLJDBCDataModel(ds);
	    this.dataSource = ds;

	}

	public void prepare() {

		Connection connection = null;

		try {
			
			connection = dataSource.getConnection();

			/*
			 * create database
			 */
			connection.prepareStatement("CREATE DATABASE IF NOT EXISTS movielens").execute();
			
			/*
			 * Create preferences table
			 */
			connection.prepareStatement("DROP TABLE IF EXISTS taste_preferences").execute();
	   	 	connection.prepareStatement(CREATE_PREFERENCES).execute();
	   	 	
	   	 	/*
	   	 	 * create filter table
	   	 	 */
			connection.prepareStatement("DROP TABLE IF EXISTS taste_filters").execute();
	   	 	connection.prepareStatement(CREATE_FILTERS).execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
			
		} finally {
			IOUtils.quietClose(connection);
		}

	}
	
	public DataModel getTrainingData() throws IOException {
		
		try {
			return new GenericDataModel(dataModel.exportWithPrefs());
		
		} catch (TasteException e) {
			throw new IOException(e);
		}
	}	

	  public DataModel getDataModel() throws IOException {
	    return dataModel;
	  }

	public void setPreference(long userID, long itemID, float value) throws IOException {

		try {
			dataModel.setPreference(userID, itemID, value);

		} catch (TasteException e) {
			throw new IOException(e);
		}
	}

	public void setPreferencesByBatch(Iterator<Preference> preferences, int batchSize) throws IOException {

		Connection connection = null;
		PreparedStatement insertStatement = null;

		try {
			connection = dataSource.getConnection();
			insertStatement = connection.prepareStatement(INSERT_PREFERENCES);

			int recordsQueued = 0;

			while (preferences.hasNext()) {
				
				Preference preference = preferences.next();
				
				/*
				 * insert data
				 */
				insertStatement.setLong(1, preference.getUserID());
				insertStatement.setLong(2, preference.getItemID());
				insertStatement.setFloat(3, preference.getValue());
				
				insertStatement.addBatch();
				if (++recordsQueued % batchSize == 0) insertStatement.executeBatch();

			}

			if (recordsQueued % batchSize != 0) insertStatement.executeBatch();

		} catch (SQLException e) {
			throw new IOException(e);
		
		} finally {
			IOUtils.quietClose(insertStatement);
			IOUtils.quietClose(connection);
		}
	}

	public void addFilter(String label, long itemID) throws IOException {

		Connection connection = null;
		PreparedStatement insertStatement = null;

		try {
			connection = dataSource.getConnection();
			insertStatement = connection.prepareStatement(INSERT_FILTERS);

			insertStatement.setString(1, label);
			insertStatement.setLong(2, itemID);

			insertStatement.execute();

		} catch (SQLException e) {
			throw new IOException(e);
			
		} finally {
			IOUtils.quietClose(insertStatement);
			IOUtils.quietClose(connection);
		}
	}

	public Iterable<String> addFiltersByBatch(Iterator<Filter> filters, int batchSize) throws IOException {

		Set<String> modifiedLabels = Sets.newHashSet();

		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = dataSource.getConnection();
			stmt = conn.prepareStatement(INSERT_FILTERS);

			int recordsQueued = 0;

			while (filters.hasNext()) {

				Filter filter = filters.next();

				modifiedLabels.add(filter.getLabel());

				stmt.setString(1, filter.getLabel());
				stmt.setLong(2, filter.getItemID());
				
				stmt.addBatch();

				if (++recordsQueued % batchSize == 0) {
					stmt.executeBatch();
				}
			}

			if (recordsQueued % batchSize != 0) {
				stmt.executeBatch();
			}

		} catch (SQLException e) {
			throw new IOException(e);

		} finally {
			IOUtils.quietClose(stmt);
			IOUtils.quietClose(conn);
		}

		return modifiedLabels;
	}

	public void deleteFilter(String label, long itemID) throws IOException {

		Connection connection = null;
		PreparedStatement deleteStatement = null;

		try {
			connection = dataSource.getConnection();
			deleteStatement = connection.prepareStatement(REMOVE_FILTER);

			deleteStatement.setString(1, label);
			deleteStatement.setLong(2, itemID);

			deleteStatement.execute();

		} catch (SQLException e) {
			throw new IOException(e);

		} finally {
			IOUtils.quietClose(deleteStatement);
			IOUtils.quietClose(connection);
		}
	}

	public void deleteAllFilters(String label) throws IOException {

		Connection connection = null;
		PreparedStatement deleteStatement = null;

		try {
			connection = dataSource.getConnection();
			deleteStatement = connection.prepareStatement(REMOVE_ALL_FILTERS);

			deleteStatement.setString(1, label);
			deleteStatement.execute();

		} catch (SQLException e) {
			throw new IOException(e);
			
		} finally {
			IOUtils.quietClose(deleteStatement);
			IOUtils.quietClose(connection);
		}
	}

	public Iterable<String> deleteFiltersByBatch(Iterator<Filter> filters,
			int batchSize) throws IOException {

		Set<String> modifiedLabels = Sets.newHashSet();

		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = dataSource.getConnection();
			stmt = conn.prepareStatement(REMOVE_FILTER);

			int recordsQueued = 0;

			while (filters.hasNext()) {

				Filter filter = filters.next();

				modifiedLabels.add(filter.getLabel());

				stmt.setString(1, filter.getLabel());
				stmt.setLong(2, filter.getItemID());
				
				stmt.addBatch();

				if (++recordsQueued % batchSize == 0) {
					stmt.executeBatch();
				}
			}

			if (recordsQueued % batchSize != 0) {
				stmt.executeBatch();
			}

		} catch (SQLException e) {
			throw new IOException(e);

		} finally {
			IOUtils.quietClose(stmt);
			IOUtils.quietClose(conn);
		}

		return modifiedLabels;
	}

	public FastIDSet getCandidates(String label) throws IOException {
		
		Connection connection = null;
		
		PreparedStatement getStatement = null;
		ResultSet resultSet = null;

		try {

			FastIDSet filters = new FastIDSet();

			connection = dataSource.getConnection();
			getStatement = connection.prepareStatement(GET_FILTERS, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			getStatement.setFetchDirection(ResultSet.FETCH_FORWARD);

			getStatement.setFetchSize(1000);
			getStatement.setString(1, label);

			resultSet = getStatement.executeQuery();

			while (resultSet.next()) {
				filters.add(resultSet.getLong(1));
			}

			return filters;

		} catch (SQLException e) {
			throw new IOException(e);

		} finally {
			IOUtils.quietClose(resultSet, getStatement, connection);
		}
	}

	public void close() throws IOException {
		
		try {
			dataSource.close();
		
		} catch (SQLException e) {
			throw new IOException("[MySqlConnector] Closing data source failed.", e);
		}
	}

	private String getEndpoint(HashMap<String,String> properties) {

		/*
		 * Retrieve Database parameters
		 */
		String host = properties.get(DB_HOST);
		int port    = Integer.parseInt(properties.get(DB_PORT));
		
		String database = properties.get(DB_NAME);

		String endpoint = "jdbc:mysql://" + host + ":" + port + "/" + database;
		return endpoint.toString();
		
	}

}
