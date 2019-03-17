# Search Engine made for any sized text based corpus

## Overview
  This project was made as a part of the search engines course (CECS 529) at CSU Long Beach. We have expanded on the features of the engine with each milestone releases. The per release changes and features added can be found in the corresponding readme files of the project.
  
## Features

### Any size corpus 

### Searching using Wildcard charecters (* as the wildcard) K-Grams 

### Search by a complete phrase

### Boolean Searches

### Ranked Search results

### B+ Trees Vocabulary for faster searching

### Different Types of Indexes

### Different scoring techniques 


***************************************************** MILESTONE 2  Change log **********************************************

Index : Index now has two different methods, GetPositionalPosting and GetPostings to avoid needlessly getting positions on ranked and And/Or queries.\
On Disk Index Writer : We can now build and store index on disk. Index is stored at path : Corpus/index.
SPIMI : Used SPIMI Alogirthm to allow pottentially unlimited size of index to be read and stored on Disk without running out of memory.\
B+ Tree: Vocabs are now stored in B+ Tree to avoid binary search of the vocabTable.bin We used MapDB Library which implements on disk databases. \
K Gram : Completely changed the way KGrams are calculated. Now K-Grams work and also works on OnDisk Indices! (We store the unstemmed vocabs in a new file, unstemmedVocabs.bin as UTF words.) \
Unit Testing : Changed the structure of Unit tests so that now each functionality has its own method.
TokenProcessor now passed abstractly to methods.


*****************************************************************************************************************************
