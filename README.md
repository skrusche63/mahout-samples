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

#### ModelBuilder
This component operates on the reference data provided by the MySQL database and computes a file-based training dataset. The ModelBuilder 
uses Mahout's LogLikelihoodSimilarity.  

#### ItemRecommender
This recommender operates on the training dataset and computes recommendations for a list of items.


### MahoutEngine
This is a thin wrapper for a selected list of commonly used Mahout algorithms. 

_to be continued_
