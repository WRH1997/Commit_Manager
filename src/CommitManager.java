import java.util.*;

public class CommitManager{

    //declare instance variables
    private CommitDatabase commitDatabase;
    private int startTime;
    private int endTime;
    private CommitFileGraph commitGraph;
    private CommitFileGraph timeWindowCommitGraph;
    private Set<Set<String>> components;
    private int minimumComponentThreshold;

    public CommitManager(){
        commitDatabase = new CommitDatabase();   //"Database" object that stores and organizes commits
        //-1 for startTIme and endTime denote that not time window is currently in effect
        startTime = -1;
        endTime = -1;
        commitGraph = new CommitFileGraph();   //graph that stores ALL commit files and their appearances together
        timeWindowCommitGraph = new CommitFileGraph();   //graph that stores a certain time window's commit files and their appearances together
        components = new HashSet<>();
        minimumComponentThreshold = -1;   //int to store minimum component threshold set by componentMinimum (-1 denotes that threshold not yet set)
    }



    public void addCommit(String developer, int commitTime, String task, Set<String> commitFiles) throws IllegalArgumentException{
        if(developer==null || task==null || commitFiles==null){
            throw new IllegalArgumentException("Null value passed! \n\tSource: addCommit");
        }
        if(developer.trim().equals("")){
            throw new IllegalArgumentException("Developer is empty string! \n\tSource: addCommit");
        }
        if(commitFiles.isEmpty()){
            throw new IllegalArgumentException("Commit files is empty set (no commit files)! \n\tSource: addCommit");
        }
        if(commitTime<0){
            throw new IllegalArgumentException("Commit time is negative (invalid)! \n\tSource: addCommit");
        }
        if(task.charAt(0)!='F' && task.charAt(0)!='B'){
            throw new IllegalArgumentException("Commit task is not a bug ('B') or feature ('F') [case-sensitive]! \n\tSource: addCommit");
        }
        if(task.charAt(1)!='-'){
            throw new IllegalArgumentException("Commit task formatted incorrectly! \n\tSource: addCommit");
        }
        if(task.trim().length()==2){   //denotes task identifier is "B-" or "F-" followed by an empty string, which is invalid as it provides no identifying information
            throw new IllegalArgumentException("Commit task identifier empty! \n\tSource: addCommit");
        }
        Iterator commitFilesItr = commitFiles.iterator();
        while(commitFilesItr.hasNext()){
            String commitFile = (String) commitFilesItr.next();
            if(commitFile==null){
                throw new IllegalArgumentException("One or more of the commit files is null! \n\tSource: addCommit");
            }
            if(commitFile.trim().equals("")){
                throw new IllegalArgumentException("One or more of the commit files names is empty! \n\tSource: addCommit");
            }
        }
        //encapsulate commit data in "Commit" object and store in list for later use
        Commit newCommit = new Commit(commitTime, commitFiles, task, developer);
        commitDatabase.add(newCommit);
        commitGraph.addToGraph(commitFiles);   //update graph based on the commit files
    }



    boolean setTimeWindow(int startTime, int endTime){
        if(startTime<0 || endTime<0){
            return false;
        }
        if(endTime<startTime){
            return false;
        }
        //clear and recalculate time window commit graph based on new time window
        timeWindowCommitGraph.clear();
        List<Commit> allCommits = commitDatabase.getAllCommits();
        for(int i=0; i<allCommits.size(); i++){
            Commit commit = (Commit) allCommits.get(i);
            if(commit.getCommitTime()>=startTime && commit.getCommitTime()<=endTime){
                timeWindowCommitGraph.addToGraph(commit.getCommitFiles());
            }
        }
        this.startTime = startTime;
        this.endTime = endTime;
        return true;
    }



    void clearTimeWindow(){
        startTime = -1;
        endTime = -1;
        timeWindowCommitGraph.clear();   //cleared since no time window is in effect
    }



    boolean componentMinimum(int threshold){
        if(threshold<=0){
            return false;
        }
        components.clear();
        minimumComponentThreshold = threshold;
        if(startTime!=-1 && endTime!=-1){   //a time window is set, so group files within time window into components
            components = timeWindowCommitGraph.groupComponents(threshold);
            return true;
        }
        else{   //no time window set, so group all files into components
            components = commitGraph.groupComponents(threshold);
            return true;
        }
    }



    Set<Set<String>> softwareComponents(){
        if(minimumComponentThreshold>0){   //a minimum component threshold was set
            componentMinimum(minimumComponentThreshold);   //return components based on threshold
        }
        else{   //no minimum component threshold set
            componentMinimum(1);   //group files that appeared at least once together as component
        }
        return components;
    }




    Set<String> repetitionInBugs(int threshold) throws IllegalArgumentException{
        if(threshold<1){
            throw new IllegalArgumentException("Threshold must be greater than 0! \n\tSource: repetitionInBugs");
        }
        //call helper class method that returns a String-List map that groups each bug task with a list of all its associated files committed during the time window set
        Map<String, List<String>> bugTaskFiles = commitDatabase.groupBugTaskFiles(startTime, endTime);
        Set<String> repeatedBugs = new HashSet<>();
        for(Map.Entry<String, List<String>> bugTask: bugTaskFiles.entrySet()){    //iterate through each bug task-file grouping
            ArrayList<String> bugFiles = (ArrayList) bugTask.getValue();
            //iterate through each of the bug task's files and tally how many times that file appeared
            for(int i=0; i<bugFiles.size(); i++){
                int fileTally = 0;
                for(int j=0; j<bugFiles.size(); j++){
                    if(bugFiles.get(i).equals(bugFiles.get(j))){
                        fileTally++;
                    }
                }
                if(fileTally>=threshold){   //a file in that bug task has appeared at least "threshold" times
                    repeatedBugs.add(bugTask.getKey());
                    break;   //at least one file meets threshold criteria, so we don't need to check the rest of the bug task's files
                }
            }
        }
        return repeatedBugs;
    }




    Set<String> broadFeatures(int threshold) throws IllegalArgumentException{
        if(threshold<1){
            throw new IllegalArgumentException("Threshold must be greater than 0! \n\tSource: broadFeatures");
        }
        Set<Set<String>> components = softwareComponents();
        //call helper class method that groups each feature task with all the files associated with it
        Map<String, List<String>> featureTaskFiles = commitDatabase.groupFeatureTaskFiles(startTime, endTime);
        Set<String> broadFeatures = new HashSet<>();
        //loop through feature task in the feature-files groupings map
        for(Map.Entry<String, List<String>> feature: featureTaskFiles.entrySet()){
            List<String> featureFiles = feature.getValue();
            int featureThresholdCounter = 0;
            Iterator<Set<String>> componentsItr = components.iterator();
            //go through each software component and see if feature has "touched" at least one of its files
            while(componentsItr.hasNext()){
                Set<String> componentFiles = componentsItr.next();
                Iterator filesItr = componentFiles.iterator();
                while(filesItr.hasNext()){
                    String file = (String) filesItr.next();
                    if(featureFiles.contains(file)){
                        featureThresholdCounter++;   //feature has "touched" one of this component's files
                        break;   //go to next component
                    }
                }
            }
            if(featureThresholdCounter>=threshold){
                broadFeatures.add(feature.getKey());
            }
        }
        return broadFeatures;
    }




    Set<String> experts(int threshold) throws IllegalArgumentException{
        if(threshold<1){
            throw new IllegalArgumentException("Threshold must be greater than 0! \n\tSource: experts");
        }
        Set<String> experts = new HashSet<>();
        Set<Set<String>> components = softwareComponents();
        //call helper class method that returns a map that groups each developer with every file they committed
        Map<String, List<String>> developerCommitFiles = commitDatabase.groupDeveloperCommitFiles(startTime, endTime);
        for(Map.Entry<String, List<String>> developer: developerCommitFiles.entrySet()){
            int expertThresholdCounter = 0;
            Iterator<Set<String>> componentsItr = components.iterator();
            while(componentsItr.hasNext()){   //loop through each component
                Set<String> thisComponent = componentsItr.next();
                Iterator filesItr = thisComponent.iterator();
                while(filesItr.hasNext()){   //loop through each file in component
                    String file = (String) filesItr.next();
                    if(developer.getValue().contains(file)){
                        expertThresholdCounter++;   //developer has "touched" at least one file in this component
                        break;   //go to next component
                    }
                }
            }
            if(expertThresholdCounter>=threshold){
                experts.add(developer.getKey());
            }
        }
        return experts;
    }



    List<String> busyClasses(int limit) throws IllegalArgumentException{
        if(limit<1){
            throw new IllegalArgumentException("Limit must be greater than 0! \n\tSource: busyClasses");
        }
        List<String> busyClasses = new ArrayList<>();
        if(limit<=0){
            return busyClasses;
        }
        //call helper class method that returns a String-Integer map where the string is a file's name and the integer value is the number of times it occurred.
        //note that this map is sorted by its values (number of occurrences) in descending order and only contains files committed during the time window (if one is set)
        Map<String, Integer> fileOccurrences = commitDatabase.calculateFileOccurrences(startTime, endTime);
        int fileLimitCounter = 0;
        int tiedOccurrenceAtLimit = -1;
        //iterate through the files in the sorted file occurrences map, adding files into the busyClasses list until we hit the limit
        for(Map.Entry<String, Integer> file: fileOccurrences.entrySet()){
            if(fileLimitCounter<limit){
                busyClasses.add(file.getKey());
                fileLimitCounter++;
            }
            else if(fileLimitCounter==limit){
                busyClasses.add(file.getKey());
                tiedOccurrenceAtLimit = file.getValue();   //when we are at the limit, store this file's occurrence tally and go to the next file in the map
                fileLimitCounter++;
            }
            else if(fileLimitCounter>limit){   //keep adding files over the limit that tie with the file at the limit
                if(file.getValue()==tiedOccurrenceAtLimit){
                    busyClasses.add(file.getKey());
                }
                else{
                    break;   //as soon as a file over the limit does not tie with the file at the limit, we stop adding files to the busyClasses list
                }
            }
        }
        return busyClasses;
    }

}
