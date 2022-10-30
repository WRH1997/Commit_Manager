import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

//this class stores a graph of the different commit files that have appeared thus far
//and the edges between them that denote files appeared together in a commit.
//the edges also store how many times files have appeared together
public class CommitFileGraph {

    //graph stored as String-Object map, where string denotes files (vertices)
    //and GraphEdges object stores all the other files they appeared together with and how many times they appeared together
    //(similar to adjacency list, but with GraphEdges object instead of linked lists for each vertex)
    private Map<String, GraphEdges> commitFileGraph;

    CommitFileGraph(){
        commitFileGraph = new HashMap<>();
    }


    //this function is invoked whenever addCommit(..) function is called.
    //it is fed the set of files of the commit, and updates the graph accordingly
    void addToGraph(Set<String> commitFiles){
        for(String file: commitFiles){
            if(!commitFileGraph.containsKey(file)){
                commitFileGraph.put(file, new GraphEdges());  //add new vertex if file doesn't already exist in graph
            }
            commitFileGraph.get(file).addEdges(commitFiles, file);   //update all the vertex's edges based on the files of the commit
        }
    }


    //this function groups files into components based on the threshold set in componentMinimum(..)
    Set<Set<String>> groupComponents(int threshold){
        Set<Set<String>> allComponents = new HashSet<>();
        for(Map.Entry<String, GraphEdges> file: commitFileGraph.entrySet()){
            Set<String> existingComponents = componentsContain(allComponents, file.getKey());   //check if file is part of an existing component
            Set<String> component;
            if(existingComponents!=null){
                component = existingComponents;  //file exists in a component, so operate on that existing component
            }
            else{
                component = new HashSet<>();   //files does not exist in a component, so create new component
                component.add(file.getKey());   //and add file to that new component
            }
            //now we go through each of the file's edges (other files that appeared with it)
            //and add each of those edges that appeared with the file at least "threshold" time to the component
            Map<String, Integer> fileEdges = file.getValue().getEdges();
            for(Map.Entry<String, Integer> edge: fileEdges.entrySet()){
                if(edge.getValue()>=threshold && !component.contains(edge.getKey())){   //note that we also check whether the edge already exists in the component since we may be operating on an existing component
                    component.add(edge.getKey());
                }
            }
            allComponents.add(component);
        }
        return allComponents;
    }


    //this function checks whether a file is already present in an existing component
    //the function is called during the groupComponents(..) function above
    Set<String> componentsContain(Set<Set<String>> allComponents, String file){
        //CITATION NOTE: I was having trouble iterating through a set of sets since my original approach [Iterator itr = allComponents.iterator()]
        //was not iterating properly. So I looked online and it seems I needed to explicitly state the type of data
        //the iterator needs to go through like such: Iterator<Set<String>> itr = allComponents.iterator().
        //I found the cited information at the following URL: //https://stackoverflow.com/questions/17883812/iterating-over-sets-of-sets
        //Accessed: October 27, 2022
        Iterator<Set<String>> itr = allComponents.iterator();
        while(itr.hasNext()){
            Set<String> components = itr.next();
            if(components.contains(file)){
                return components;
            }
        }
        return null;
    }


    void clear(){
        commitFileGraph.clear();
    }


    boolean isEmpty(){
        if(commitFileGraph.isEmpty()){
            return true;
        }
        else{
            return false;
        }
    }
}
