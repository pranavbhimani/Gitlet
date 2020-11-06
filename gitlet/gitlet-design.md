# Gitlet Design Document

**Name**: Pranav Bhimani

## Classes and Data Structures
### Commit

##### Fields
 * ID - This could be a reference string or an actual number ID for every single one of our commits.
 There will not be any repeats for each commits. This relates to the concept of a 
 primary key.
 
 * Message - Contains all the information related to the specific commit. 
 * Time stamp - the time at which a commit was developed or created.
 * Parent - The first commit in the series or the parent commit of a commit object.
 * Blobs - These will store the text of the actual commit not the information like the message.
 
 ##### Useful Data Structures
 * There will also exist a pointer to the HEAD which means pointing to the most recent commit.
 * HASH SET: This will be used for IDs of commits to make sure there are no duplicate IDs
 and so we can point each ID to its specific commit.
 * HASH MAP: This will be used for the blobs. This way we can make the key the name of the file 
 and value will be the text inside the file. 
 
 ### repo
 
* This class has an object that uses Commit class. 
However, here we are creating a repo object with more functionalities so that in our Main method, we can just create a new repo object. 
We will have various methods for initializing a new repo, 
adding files, creating logs, printing commit statements, 
committing files (in which we call our commit class), 
checking out files to restore them to a previous commit version, getting a commit from its ID, checking to see if files are tracked or untracked, removing files and branches, and creating staging files. Additionally, we would add our extra credit here: add-remote, rm-remote, push, fetch, and pull. This class would enable to make our Gitlet a remote version-check system.
##### Methods
* add
* init
##### Data Structures
* Staging area: HashMap of two items (Name of files to be added, sha1 of blobs) 
* Tracked files to be Add: Hash map
* Tracked files to be Delete: Hash map
### Merge
##### Data Structures
* Branches: 
## Algorithms
### Commit
To save a version of the files we are working on, we first add the files into the staging area. This creates a blob with the new contents. Once commit is called this additions/ deletions are saved. We create a unique commit ID (hashcode) each time we commit one or more files. We can point our head commit to that version by going using that unique ID 
## Persistence
Serialize and deserialize all objects that we need to reference again.
