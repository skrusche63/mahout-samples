package de.kp.mahout;

import org.apache.mahout.fpm.pfpgrowth.FPGrowthDriver;

@SuppressWarnings("deprecation")
public class MahoutEngine {

	/**
	 * This class is a thin wrapper for a selected set of Mahout machine learning
	 * algorihtms. 
	 */
	public MahoutEngine() {		
	}
	
	/**
	 * Association Rule Learning is a method to find relations between variables in a dataset. 
	 * For instance, using shopping receipts, we can find association between items: bread is 
	 * often purchased with peanut butter or chips and beer are often bought together. 
	 * 
	 * The basis is the Mahout Frequent Pattern Mining implementation to find the associations 
	 * between items. For details on the algorithms (apriori and fpgrowth) used to find frequent 
	 * patterns, you can look at “The comparative study of apriori and FP-growth algorithm”.
	 * 
	 * @param args
	 */
	public void frequentPatternMining(String[] args) {

		/*
		 * Sample args
		 * 
		 * 
		 * String[] args = { 
		 * "-i", "input file", 
		 * "-o", "output folder", 
		 * "-k", "10", 
		 * "-method", "mapreduce", 
		 * "-s", "2" 
		 * };
		 * 
		 * -k 10 
		 * Maximum Heap Size k, to denote the requirement to mine top K items is 10.
		 * 
		 * -s 2 
		 * The minimum number of times a co-occurrence must be present is 2.
		 * 
		 * 
		 * This method generates several files in the output directory:
		 * 
		 * fList (sequence file), frequentpatterns (sequence file)
		 *  
		 */
		try {
			FPGrowthDriver.main(args);

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

}
