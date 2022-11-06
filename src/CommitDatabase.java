import java.util.*;


//this class stores all commits added. It also separates bug and feature commits into two distinct lists.
//Additionally, this class also provides CommitManager with functionalities that organize the commits such as
//grouping bug tasks with all files that appeared with it and calculating how many times each file appeared
public class CommitDatabase {

    private List<Commit> allCommits;
    private List<Commit> bugCommits;
    private List<Commit> featureCommits;

    CommitDatabase(){
        allCommits = new ArrayList<>();
        bugCommits = new ArrayList<>();
        featureCommits = new ArrayList<>();
    }

    List<Commit> getAllCommits(){
        return allCommits;
    }


    //this method is invoked when addCommit() is called. It stores that added commit into this class's allCommits "database"
    //and also stores that added commit into either its bugCommits or featureCommits "database" accordingly
    void add(Commit commit) throws IllegalArgumentException{
        allCommits.add(commit);
        String commitTask = commit.getTask();
        if(commitTask.charAt(0)=='B'){
            bugCommits.add(commit);
        }
        else if(commitTask.charAt(0)=='F'){
            featureCommits.add(commit);
        }
        else{
            //CommitManager should have already thrown an exception in this 'else' case (task does not start with 'B' or 'F')
            //However, the following exception was added because this method is public, meaning it can be accessed outside of CommitManager
            throw new IllegalArgumentException("Commit task is not a bug ('B') or feature ('F') [case-sensitive]! \n\tSource: addCommit");
        }
    }



    //this method is invoked during repetitionInBugs(), and returns each bug task with grouped with all its associated files occurrences during the time window set
    //returns String-List map with bug task string as key and list of all its file occurrences as the list value
    Map<String, List<String>> groupBugTaskFiles(int startTime, int endTime){
        Map<String, List<String>> allBugTaskFiles = new HashMap<>();
        for(int i=0; i<bugCommits.size(); i++){
            Commit commit = (Commit) bugCommits.get(i);
            int commitTime = commit.getCommitTime();
            if(startTime!=-1 && endTime!=-1){   //a time window is in effect
                if(commitTime<startTime || commitTime>endTime){
                    continue;   //skip bug commits outside the time window
                }
            }
            String commitTask = commit.getTask();
            List<String> bugTaskFiles;   //list to store all the files that appear with bug task
            if(allBugTaskFiles.containsKey(commitTask)){   //check if bug task already has files associated with it
                bugTaskFiles = allBugTaskFiles.get(commitTask);   //operate on existing list of files
            }
            else{
                bugTaskFiles = new ArrayList<>();   //create new list of files
            }
            Set<String> commitFiles = commit.getCommitFiles();
            Iterator filesItr = commitFiles.iterator();
            //iterate through this commit's files and add each of them to the bug task associated with them
            while(filesItr.hasNext()){
                bugTaskFiles.add((String) filesItr.next());
            }
            allBugTaskFiles.put(commitTask, bugTaskFiles);   //add/update the bug task's files list in map
        }
        return allBugTaskFiles;
    }



    //invoked when broadFeatures() method is called. This method groups each feature task identifier with all the files it was committed with.
    //returns String-List map with string denoting feature task identifier and list containing all the files associated with it.
    Map<String, List<String>> groupFeatureTaskFiles(int startTime, int endTime){
        Map<String, List<String>> allFeatureTaskFiles = new HashMap<>();
        for(int i=0; i<featureCommits.size(); i++){
            Commit commit = (Commit) featureCommits.get(i);
            int commitTime = commit.getCommitTime();
            if(startTime!=-1 && endTime!=-1){   //a time window is in effect
                if(commitTime<startTime || commitTime>endTime){
                    continue;   //skip feature commits outside the time window set
                }
            }
            List<String> featureTaskFiles;   //list to store all the files that appear with a feature task
            String commitTask = commit.getTask();
            if(allFeatureTaskFiles.containsKey(commitTask)){   //check if feature already has some files associated with it
                featureTaskFiles = allFeatureTaskFiles.get(commitTask);   //operate on existing list of associated files
            }
            else{
                featureTaskFiles = new ArrayList<>();   //create new list of files for this feature
            }
            Set<String> commitFiles = commit.getCommitFiles();
            //iterate through commit's files and add all files that appear with this feature task into its list of files
            Iterator filesItr = commitFiles.iterator();
            while(filesItr.hasNext()){
                String file = (String) filesItr.next();
                if(!featureTaskFiles.contains(file)){
                    featureTaskFiles.add(file);
                }
            }
            allFeatureTaskFiles.put(commitTask, featureTaskFiles);
        }
        return allFeatureTaskFiles;
    }



    //invoked when experts() method called. It groups each developer with all the files they committed.
    //it returns a String-List map with the string denoting the developer and the list containing all files they committed.
    Map<String, List<String>> groupDeveloperCommitFiles(int startTime, int endTime){
        Map<String, List<String>> allDeveloperCommitFiles = new HashMap<>();
        for(int i=0; i<allCommits.size(); i++){
            Commit commit = (Commit) allCommits.get(i);
            int commitTime = commit.getCommitTime();
            if(startTime!=-1 && endTime!=-1){   //a time window is in effect
                if(commitTime<startTime || commitTime>endTime){
                    continue;   //skip commits outside the time window
                }
            }
            String developer = commit.getDeveloper();
            List<String> developerFiles;   //list to store files developer committed
            if(allDeveloperCommitFiles.containsKey(developer)){   //check if developer already has files associated with them
                developerFiles = allDeveloperCommitFiles.get(developer);   //operate on existing list of files
            }
            else{
                developerFiles = new ArrayList<>();   //create new list of files for this developer
            }
            Set<String> commitFiles = commit.getCommitFiles();
            Iterator filesItr = commitFiles.iterator();
            //add each file in the commit to the developer's list of files
            while(filesItr.hasNext()){
                String file = (String) filesItr.next();
                if(developerFiles.contains(file)){
                    continue;   //skip files that already exist in developer's list of committed files
                }
                developerFiles.add(file);
            }
            allDeveloperCommitFiles.put(developer, developerFiles);
        }
        return allDeveloperCommitFiles;
    }


    //invoked when busyClasses() is called. The method organizes all files committed during a certain time window into a list
    //that is sorted according to how many times each file appeared in descending order
    Map<String, Integer> calculateFileOccurrences(int startTime, int endTime){
        Map<String, Integer> fileOccurrences = new HashMap<>();
        for(int i=0; i<allCommits.size(); i++){
            Commit commit = (Commit) allCommits.get(i);
            int commitTime = commit.getCommitTime();
            if(startTime!=-1 && endTime!=-1){   //a time window is in effect
                if(commitTime<startTime || commitTime>endTime){
                    continue;   //skip commits outside the time window
                }
            }
            Set<String> commitFiles = commit.getCommitFiles();
            //iterate through all files in this commit and update each file's overall tally
            Iterator filesItr = commitFiles.iterator();
            while(filesItr.hasNext()){
                String file = (String) filesItr.next();
                if(fileOccurrences.containsKey(file)){
                    int occurrences = fileOccurrences.get(file);
                    fileOccurrences.put(file, occurrences + 1);
                }
                else{
                    fileOccurrences.put(file, 1);
                }
            }
        }
        //sort file-tally map by its values in descending order
        Map<String, Integer> sortedFileOccurrences = sortByValue(fileOccurrences);
        return sortedFileOccurrences;
    }



    //this method is used to sort maps by value in descending order.
    //invoked when organizeFileOccurrences is called
    private Map<String, Integer> sortByValue(Map<String, Integer> unsortedMap){
        List<Integer> occurrences = new ArrayList<>();
        Map<String, Integer> sortedMap = new LinkedHashMap<>();   //LinkedHashMap used to retain insertion order
        //add all the file occurrence tallies (map values) to a list
        for(Map.Entry<String, Integer> entry: unsortedMap.entrySet()){
            occurrences.add(entry.getValue());
        }
        //sort that list in descending order
        Collections.sort(occurrences);
        Collections.reverse(occurrences);
        //create new sortedMap by matching file keys with sorted list's tally values
        for(int i=0; i<occurrences.size(); i++){
            for(Map.Entry<String, Integer> entry: unsortedMap.entrySet()){
                if(entry.getValue()==occurrences.get(i) && !sortedMap.containsKey(entry.getKey())){
                    sortedMap.put(entry.getKey(), entry.getValue());
                    break;
                }
            }
        }
        return sortedMap;
    }

}
