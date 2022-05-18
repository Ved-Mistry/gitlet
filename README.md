# gitlet
Java Implementation of Git (the version control system for Gitlet)

Commit:
  A class representing an individual datastructure that stores all of the information held in a single commit of a repository. Stores its own hash, it's parent's hash (in the commit tree), hashes of "blobs", and other metadata about the commit.
  
Blob:
  A class representing an individual file that stores the serialized information representing a file from the repository. Blobs are created every time files are added, and each commit is tied to blobs representing the state of a given added file at the time of the commit.
  
Gitlet:
  The class containing all of the main methods of the Git repository functions.

Main:
  The switch/case argument taker that interprets the commands from the terminal and calls methods in Gitlet accordingly.
