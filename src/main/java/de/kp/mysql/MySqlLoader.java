package de.kp.mysql;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.model.Preference;

public class MySqlLoader {

	/*
	 * This method loads the movielens ratings (userID, itemID, preference)
	 * prepared as a csv file into a MySQL database
	 */
	public static void main(String[] args) {
		
		HashMap<String,String> properties = new HashMap<String,String>();
		
		/*
		 * Database properties
		 */
		properties.put(MySQLConnector.DB_HOST, args[0]);
		properties.put(MySQLConnector.DB_PORT, args[1]);
		
		properties.put(MySQLConnector.DB_NAME, args[2]);

		/*
		 * Database credentials
		 */
		properties.put(MySQLConnector.DB_USER, args[3]);
		properties.put(MySQLConnector.DB_PASS, args[4]);
		
		MySQLConnector connector = new MySQLConnector(properties);
		connector.prepare();
		
		/*
		 * Insert preferences
		 */
		String ratings = args[5];
		
		BufferedReader reader;
		String inline;

		try {
			
			ArrayList<Preference> preferences = new ArrayList<Preference>();

			reader = new BufferedReader(new FileReader(ratings));
			while ((inline = reader.readLine()) != null) {

				String[] tokens = inline.split("::");

				Preference pref = new GenericPreference(Long.valueOf(tokens[0]), Long.valueOf(tokens[1]), Float.valueOf(tokens[2]));
				preferences.add(pref);

			}

			reader.close();
			
			connector.setPreferencesByBatch(preferences.iterator(), 1000);
			connector.close();
			
		} catch (NumberFormatException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
