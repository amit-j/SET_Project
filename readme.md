***************************************************** MILESTONE 2 CHANGE LOG ******************************************************

Index : Index now has two different methods, GetPositionalPosting and GetPostings to avoid needlessly getting positions on ranked and And/Or queries.
On Disk Index Writer : We can now build and store index on disk. Index is stored at path : Corpus/index.
SPIMI : Used SPIMI Alogirthm to allow pottentially unlimited size of index to be read and stored on Disk without running out of memory.
B+ Tree: Vocabs are now stored in B+ Tree to avoid binary search of the vocabTable.bin We used MapDB Library which implements on disk databases.
K Gram : Completely changed the way KGrams are calculated. Now K-Grams work and also works on OnDisk Indices! (We store the unstemmed vocabs in a new file, unstemmedVocabs.bin as UTF words.)
Unit Testing : Changed the structure of Unit tests so that now each functionality has its own method.
TokenProcessor now passed abstractly to methods.


***************************************************************************************************************************************
