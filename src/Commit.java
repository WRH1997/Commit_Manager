import java.util.*;

//this class stores the data of a single commit. In other words, each commit
//is encapsulated into an object of this class.
public class Commit {

    //declare instance variables (commit data)
    private int commitTime;
    private Set<String> commitFiles;
    private String task;
    private String developer;


    Commit(int commitTime, Set<String> commitFiles, String task, String developer){
        this.commitTime = commitTime;
        this.commitFiles = commitFiles;
        this.task = task;
        this.developer = developer;
    }


    /*various getter functions*/

    int getCommitTime(){
        return commitTime;
    }

    Set<String> getCommitFiles(){
        return commitFiles;
    }

    String getDeveloper(){
        return developer;
    }

    String getTask(){
        return task;
    }

}
