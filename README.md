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


### MahoutEngine
This is a thin wrapper for a selected list of commonly used Mahout algorithms. 
