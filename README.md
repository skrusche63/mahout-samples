mahout-samples
==============

This repository provides a collection of Mahout code snippets. The main purpose of these snippets is to use 
Mahout from Java applications (and not from commandline).

### Frequent Pattern Mining
Note, that Mahout is not specialised for pattern mining. There is only one algorithm, which is called "Parallel FP-Growth" 
for finding itemsets. Algorithms for generating the association rules after the itemsets are found are not included and need
to be implemented by yourself. 

Besides, note that what they call "Parallel FPGrowth" differs a little bit from the original FPGrowth. The version in Mahout 
mines top-k itemsets instead of mining itemsets with the minsup threshold.

For more mature algorithms, please check [SPMF software](http://www.philippe-fournier-viger.com/spmf/index.php).

It is a pure Java library for more than 45 algorithms related to frequent itemset and association rule mining. The library 
includes algorithms for some classic algorithms like FPGrowth. But it also offers several specialized algorithms that you 
won't find in other data mining tools like for mining 
* rare itemset, 
* erasable itemsets, 
* high-utility itemsets, 
* itemsets from uncertain data, 
* and more.

_to be continued_

### Classification

_to be continued_

### Topic Modeling
Topic modeling is a machine learning discipline that provides a simple use case for labeling and classifying a collection 
of texts and documents. Latent Dirichlet Allocation (LDA), the most commonly used topic model, is implemented by various 
other applications, such as MALLET, LDA-c, and GENSIM. 

As stated in the paper "Latent Dirichlet Allocation" by David M. Blei, Andrew Y. Ng, and Michael I. Jordan, "LDA is a 
generative probabilistic model for collections of discrete data such as text corpora."

Corpora essentially represent a large and structured set of texts. LDA performs topic distribution learning in the form 
of Bayesian inference, an approximate inference technique based on vibrational methods and an expectation-maximization 
(EM) algorithm for empirical Bayes parameter estimation. 


_to be continued_

### Recommendation System
The recommendation system provided here uses the non-Hadoop-based collaborative filtering functionality inside Mahout from a former 
project called "Taste". The main objective associated with this chosen functionality is, that it may also be used e.g. with low 
latency processing.

A collaborative filtering engine takes users' preferences for items ("tastes") and returns estimated preferences for other items.
This recommendation system is based on the [MovieLens Dataset](http://www.grouplens.org/system/files/ml-10m-README.html).

The system comprises the following components

* MySQL Database
* ModelBuilder
* ItemRecommender

The picture illustrates the dataflow between these components.
![Recommendation System / Dataflow](https://raw.github.com/skrusche63/mahout-samples/master/src/main/resources/dataflow.png)


#### MySQL Database 
This relational database is used as storage for the Movielens reference data (userID, itemID, preference). Other tables may be 
defined to e.g. hold the movie data. The MySQL database may be invoked by a (separate) web service to add or manipulate the respective 
reference data.

The listing below indicates how to load the MovieLens preference data into the MySQL database.
```
  	String[] args = {
				"127.0.0.1",
				"3306",
				"movielens",
				"user",
				"secret",
				"/your path/mahout-samples/src/main/resources/ratings.dat"
		};
		
		MySqlLoader.main(args);

```

#### ModelBuilder
This component operates on the reference data provided by the MySQL database and computes a file-based training dataset. The ModelBuilder 
uses Mahout's LogLikelihoodSimilarity.  

The listing below gives a hint how to create the training dataset.
```
		String modelFile = "/your path/mahout-samples/src/main/resources/ratings.model";

		HashMap<String, String> props = new HashMap<String, String>();
		
		props.put("host", "127.0.0.1");
		props.put("port", "3306");
		
		props.put("user", "user");
		props.put("pass", "secret");
		
		props.put("database", "movielens");
		
		DataModel dataModel = new MySQLConnector(props).getTrainingData();
		
		int numProcessors = 1;
		int similarItemsPerItem = 50;
		
		new ModelBuilder().build(new File(modelFile), dataModel, similarItemsPerItem, numProcessors);

```

#### ItemRecommender
This recommender operates on the training dataset and computes recommendations for a list of items.

The listing below shows how to use the ItemRecommender.

```
		// preparation phase, must only be invoked once

		File file = new File(modelFile);

		HashMap<String, String> props = new HashMap<String, String>();
		
		props.put("host", "127.0.0.1");
		props.put("port", "3306");
		
		props.put("user", "user");
		props.put("pass", "secret");
		
		props.put("database", "movielens");
		
		DataModel dataModel = new MySQLConnector(props).getDataModel();		

		ItemSimilarity itemSimilarity = new FileItemSimilarity(file);
		AllSimilarItemsCandidateItemsStrategy allSimilarItemsStrategy = new AllSimilarItemsCandidateItemsStrategy(itemSimilarity);

		ItemRecommender recommender = new ItemRecommender(dataModel, 
        		itemSimilarity, allSimilarItemsStrategy, allSimilarItemsStrategy);

		// operation phase
		
		int similarItems = 5;
		long[] itemIDs = {
			1234	
		};

		List<RecommendedItem> recommendedItems = recommender.recommendByItems(itemIDs, similarItems, null);

		// do what you want with these recommmendations

```

### MahoutWrapper
This is a thin wrapper for a selected list of commonly used Mahout algorithms. 

_to be continued_
