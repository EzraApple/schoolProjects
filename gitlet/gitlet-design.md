# Gitlet Design Document
###Author: Ezra Apple


## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. 

###  Main
Takes in command line arguments and executes gitlet commands.

****
### Commit
Commit class containing info about parent/child commits and file  info####Instance Variables:
* **String dateTime**: Created by accessing device local time, for use with log.
* **String parent**: Reference to parent commit.
* **String self**: Sha1 ID for this commit.
* **String msg**: Commit message.
* **HashMap<String, String> blobMap**: fileName --> serializedContents mapping of files in commit.
****
### Repository
Organizes commit and branch structure, serializes between commands
* **String HEAD**:

* **String headPath**:

* **private StagingArea stage**:

* **String stagePath**:

* **File CWD**:

* **String gitletPath**:
****
### Staging Area
Holds added commits until they are commited
####Instance Variables:
* **HashMap<String, String> toAdd**: fileName --> serializedContents mappings of files staged for addition.

* **ArrayList<String> toRemove**: fileNames of files staged for removal.

****
## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

###  Main Methods
****

### Commit Methods
****
### Repository Methods
***
### Staging Area Methods
* **public void clear()**: sets toAdd and toRemove to an empty HashMap and ArrayList.
* **public void add(String fileName, String sha1)**: adds key fileName with value sha1 to toAdd.
* **public void remove(String fileName)**: adds fileName to toRemove.
* **public HashMap<String, String> getToAdd()**: returns toAdd.
* **public ArrayList<String> getToRemove()**: returns toRemove.

****


## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
  `java gitlet.Main add wug.txt`,
  on the next execution of
  `java gitlet.Main commit -m “modify wug.txt”`,
  the correct commit will be made.

* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.

* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

