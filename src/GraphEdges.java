import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

//this class stores the edges between different commit files
//and how many times that edge appears (how many time files appeared together in a single commit)
public class GraphEdges {

    //each edge is stored as String-Integer map where the string denotes the
    //adjacent vertex (other file that appeared with this file) and the integer value
    //denotes how many times they appeared together
    private Map<String, Integer> edges;

    GraphEdges(){
        edges = new HashMap<>();
    }


    //function that add/updates a file's (source vertex's) edges based on all the files of a single commit
    void addEdges(Set<String> commitFiles, String thisFile){
        for(String file: commitFiles){
            if(!file.equals(thisFile)){   //skip source vertex file (vertex should not connect to itself)
                if(edges.containsKey(file)){   //update existing edge
                    int occurrencesTogether = edges.get(file);
                    occurrencesTogether++;
                    edges.put(file, occurrencesTogether);
                }
                else{   //add new edge
                    edges.put(file, 1);
                }
            }
        }
    }


    Map<String, Integer> getEdges(){
        return edges;
    }
}
