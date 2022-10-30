import java.util.*;


//this helper class provides CommitManager with utility operations that are used in the experts, repetitionInBugs, broadFeatures, and bustClasses methods
public class CommitUtilityOperations {


    //invoked when experts() method called. It groups each developer with all the files they committed.
    //it returns a String-List map with the string denoting the developer and the list containing all files they committed.
    static Map<String, List<String>> groupDeveloperCommitFiles(List<Commit> allCommits, int startTime, int endTIme){
        Map<String, List<String>> developers = new HashMap<>();
        for(int i=0; i<allCommits.size(); i++){
            Commit commit = (Commit) allCommits.get(i);
            if(startTime!=-1 && endTIme!=-1){   //a time window is set
                if(commit.getCommitTime()<startTime || commit.getCommitTime()>endTIme){
                    continue;   //skip commits outside the time window set
                }
            }
            List<String> existingFiles;   //list to store files developer committed
            if(developers.containsKey(commit.getDeveloper())){   //check if developer already has files associated with them
                existingFiles = developers.get(commit.getDeveloper());   //operate on existing list of files
            }
            else{
                existingFiles = new ArrayList<>();   //create new list of files
            }
            //add each file in the commit to the developer's list of files
            Set<String> commitFiles = commit.getCommitFiles();
            Iterator itr = commitFiles.iterator();
            while(itr.hasNext()){
                String file = (String) itr.next();
                if(existingFiles.contains(file)){
                    continue;   //skip files that already exist in developer's list of files
                }
                existingFiles.add(file);
            }
            developers.put(commit.getDeveloper(), existingFiles);
        }
        return developers;
    }


    //invoked when broadFeatures() method is called. This method groups each feature task identifier with all the files it was committed with.
    //returns String-List map with string denoting feature task identifier and list containing all the files associated with it.
    static Map<String, List<String>> groupFeatureFiles(List<Commit> allCommits, int startTime, int endTime){
        Map<String, List<String>> features = new HashMap<>();
        for(int i=0; i<allCommits.size(); i++){
            Commit commit = (Commit) allCommits.get(i);
            if(startTime!=-1 && endTime!=-1){   //a time window is set
                if(commit.getCommitTime()<startTime || commit.getCommitTime()>endTime){
                    continue;   //skip commits outside the time window set
                }
            }
            String commitTask = commit.getTask();
            if(commitTask.charAt(0)!='F'){
                continue;  //skip any commit tasks that are not features
            }
            List<String> existingFiles;   //list to store associated files
            if(features.containsKey(commitTask)){   //check if feature already has some files associated with it
                existingFiles = features.get(commitTask);   //operate on existing list of associated files
            }
            else{
                existingFiles = new ArrayList<>();   //create new list for this feature
            }
            Set<String> commitFiles = commit.getCommitFiles();
            //iterate through all commits and add all files that appear with this feature task into its list
            Iterator filesItr = commitFiles.iterator();
            while(filesItr.hasNext()){
                String file = (String) filesItr.next();
                if(!existingFiles.contains(file)){
                    existingFiles.add(file);
                }
            }
            features.put(commitTask, existingFiles);
        }
        return features;
    }



    //invoked when busyClasses() is called. The method organizes all files committed during a certain time window into a list
    //that is sorted according to how many times each file appeared in descending order
    static Map<String, Integer> calculateFileOccurrences(List<Commit> allCommits, int startTime, int endTime){
        Map<String, Integer> fileOccurrences = new HashMap<>();
        for(int i=0; i<allCommits.size(); i++){
            Commit commit = (Commit) allCommits.get(i);
            if(startTime!=-1 && endTime!=-1){   //check if a time window is set
                int commitTime = commit.getCommitTime();
                if(commitTime<startTime || commitTime>endTime){
                    continue;   //skips commits that are outside the time window
                }
            }
            Set<String> commitFiles = commit.getCommitFiles();
            //iterate through all the files in this commit
            Iterator filesItr = commitFiles.iterator();
            while(filesItr.hasNext()){
                String file = (String) filesItr.next();
                //update the file's occurrence tally
                if(fileOccurrences.containsKey(file)){
                    int occurrences = fileOccurrences.get(file);
                    fileOccurrences.put(file, occurrences + 1);
                }
                else{
                    fileOccurrences.put(file, 1);
                }
            }
        }
        Map<String, Integer> sortedFilesOccurrences = sortByValue(fileOccurrences);   //sort file occurrence tally map by value (sort by number of occurrences) in descending order
        return sortedFilesOccurrences;
    }


    //this method is used to sort maps by value in descending order.
    //invoked when organizeFileOccurrences is called
    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortedMap){
        List<Integer> occurrences = new ArrayList<>();
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        //add all the occurrence tallies to a list
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
                }
            }
        }
        return sortedMap;
    }



    //this method is invoked during repetitionInBugs, and simply returns a list of all the bug tasks during the time window set
    static List<String> getBugTasks(List<Commit> allCommits, int startTime, int endTime){
        List<String> bugTasks = new ArrayList<>();
        if(startTime==-1 && endTime==-1){   //no time window set, so add all bug tasks
            for(int i=0; i<allCommits.size(); i++){
                Commit commit = (Commit) allCommits.get(i);
                if(commit.getTask().charAt(0)=='B'){
                    bugTasks.add(commit.getTask());
                }
            }
        }
        else{   //a time window is set, so we add only the bug tasks committed during the window
            for(int j=0; j<allCommits.size(); j++){
                Commit commit = (Commit) allCommits.get(j);
                if(commit.getCommitTime()>=startTime && commit.getCommitTime()<=endTime){
                    if(commit.getTask().charAt(0)=='B'){
                        bugTasks.add(commit.getTask());
                    }
                }
            }
        }
        return bugTasks;
    }
}
