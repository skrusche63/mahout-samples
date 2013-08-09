package de.kp.mahout;

import org.apache.mahout.fpm.pfpgrowth.FPGrowthDriver;
import org.apache.mahout.text.SequenceFilesFromDirectory;

@SuppressWarnings("deprecation")
public class MahoutWrapper {

	/**
	 * This class is a thin wrapper for a selected set of Mahout machine learning
	 * algorihtms. 
	 */
	public MahoutWrapper() {		
	}
	
	/**
	 * Association Rule Learning is a method to find relations between variables in a dataset. 
	 * For instance, using shopping receipts, we can find association between items: bread is 
	 * often purchased with peanut butter or chips and beer are often bought together. 
	 * 
	 * The basis is the Mahout Frequent Pattern Mining implementation to find the associations 
	 * between items. For details on the algorithms (apriori and fpgrowth) used to find frequent 
	 * patterns, you can look at ÒThe comparative study of apriori and FP-growth algorithmÓ.
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

	/**
	 * A helper method to convert a directory of text files into SequenceFiles. This method takes
	 * in a directory containing sub folders of text documents and recursively reads the files and 
	 * creates a SequenceFile of docid => content. 
	 * 
	 * The docid is set as the relative path of the document from the text directory prepended with 
	 * a specified prefix. The input encoding of the text files is set to UTF-8. The content of the 
	 * output SequenceFiles are encoded as UTF-8 text.
 	 * 
	 * @param textDir
	 * @param sequenceDir
	 */
	public void sequenceFiles(String textDir, String sequenceDir) {

		String[] args = {
			"-c",
			"UTF-8",			
			"-i",
			textDir,
			"-o",
			sequenceDir
		};

		try {			
			SequenceFilesFromDirectory.main(args);

			
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

}
