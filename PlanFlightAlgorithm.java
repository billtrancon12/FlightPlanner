/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PlanFlight;

import static java.lang.System.out;
import java.util.*;
import java.io.*;

/**
 *
 * @author billt
 */
public class PlanFlightAlgorithm {
    public static void main(String[] args){
        if(args.length != 3){
            out.println("Need three arguments!");
        }
        FlightToolHelper helper = new FlightToolHelper(args[0]);
        FlightToolHelper helper2 = new FlightToolHelper(args[1], helper.getGraph(), false);
        helper2.getRequestAndOutput(args[2]);
    }
}

// class Flight stores flight information
class Flight{
    final private String src;           // Starting location
    final private List<Flight> dst;     // Destination
    final private Map<Flight, double[]> acost; // Associated cost included cost and time
    final private Set<String> udst;     // Unique destination
    
    // Default constructor
    public Flight(){
        src = "";
        dst = new ArrayList<>();
        udst = new HashSet<>();
        acost = new HashMap<>();
    }
    
    // Create a class based on the starting flight
    public Flight(String src){
        this.src = src;
        this.dst = new ArrayList<>();
        this.udst = new HashSet<>();
        this.acost = new HashMap<>();
    }
    
    // Create a class based on a full flight information including
    // the flight routes and their associated cost that starting with 
    // starting location src
    public Flight(String src, Map<Flight, double[]> acost){
        this.src = src;
        this.acost = acost;
        this.dst = new ArrayList<>();
        this.udst = new HashSet<>();
        for(Map.Entry<Flight, double[]> entry : this.acost.entrySet()){
            this.dst.add(entry.getKey());
            this.udst.add(entry.getKey().getSrc());
        }
    }
    
    // Getters
    public String getSrc(){return src;}
    public List<Flight> getDst(){return dst;}
    public Map<Flight, double[]> getDstWithCost(){return acost;}
    
    /* @param dst: a new destination that the flight can flight directly to
    *  @param cost: a cost (in money) to fly from this location to the new location
       @param time: a cost (in time) to fly from this location to the new location
       @ret status: can add the new location to the flight routine?
       @funct: Determine if the flight can add a new location to the flight routine
    */
    public boolean addDst(Flight dst, double cost, double time){
        // Check if the location was already part of the flight direct destination
        if(!udst.contains(dst.getSrc())){
            this.acost.put(dst, new double[]{cost, time});
            this.dst.add(dst);      // Adding a new location this flight can flight directly to
            udst.add(dst.getSrc()); 
            return true;
        }
        return false;
    }
}

// A class that will contain all the necessary tools to determine the shortest
// path, and the rest of the paths, and it helps collect/write data from/to
// a given file
class FlightToolHelper{
    private File ifile;     // input file
    final private Map<String, Flight> graph;    // A graph contains all flight routines of all locations
    
    // There is no default constructor
    // Every class needs at least one argument 
    
    // Create a class that will help write/read to/from a file
    // It can also help build a graph and do flight-routine-related tasks
    public FlightToolHelper(String fname){
        if(!open(fname)){   // Check if it can successfully open a file
            System.out.println("Cannot open the file");
        }
        graph = new HashMap<>();
        buildGraph();   // Building a graph
    }
    
    // This class will not be able to do file tasks
    // Only doing flight-routine-related tasks
    public FlightToolHelper(Map<String, Flight> graph){
        this.graph = graph;
        this.ifile = null;
    }
    
    // This class will do all of the tasks that FlightToolHelper can offer
    // The class will help extending the graph further if the user wants to read
    // a file to retrieve data and append to the flight routine map
    public FlightToolHelper(String fname, Map<String, Flight> graph, boolean buildStatus){
        if(!open(fname)){   // Check if it can open the file
            out.println("Cannot open the file");
        }
        this.graph = graph;
        if(buildStatus)
            buildGraph();
    }
    
    /* @param fname: a name of an input file
    *  @ret status: can open the file?
    *  @funct: check if it can open a file
    */
    private boolean open(String fname){
        ifile = new File(fname);
        return ifile.exists() && ifile.isFile();
    }
    
    /*
    *   @funct: -Reading the input file to build a graph
    *           based on the retrieved data
    *           -The input file format will be:
    *                   #number_of_flight_legs (only in first line)
    *                   Src|Dst|Cost|Time
    *            whereas:
    *                +Src: Starting location
    *                +Dst: destination
    *                +Cost: cost it takes to fly from Src to Dst
    *                +Time: time it takes to fly from Src to Dst
    */
    private void buildGraph(){
        try (Scanner scanner = new Scanner(ifile)) {    // Open a scanner
            int times = scanner.nextInt();              // Read how many times to loop
            scanner.nextLine();                         // Scan for '\n'
            
            // From now on, it will build a graph as it is reading a file
            for(int i = 0; i < times; i++){
                //Getting all neccessary arguments separated by pipe "|"
                String iline = scanner.nextLine();      
                String[] args = iline.split("\\|");
                    
                // If the flight already existed in the graph, then add a new routine
                // to that flight; otherwise, create a new flight and then add a new
                // routine to that newly created flight
                Flight f1 = (graph.containsKey(args[0])) ? graph.get(args[0]) : 
                            new Flight(args[0]);
                Flight f2 = (graph.containsKey(args[1])) ? graph.get(args[1]) : 
                            new Flight(args[1]);
                try{
                    f1.addDst(f2, Double.parseDouble(args[2]), Double.parseDouble(args[3]));
                    f2.addDst(f1, Double.parseDouble(args[2]), Double.parseDouble(args[3]));
                }
                catch(NumberFormatException e){
                    out.println(e);
                }
                
                // Put the flight back to the graph
                graph.put(args[0], f1);
                graph.put(args[1], f2);
            }
            scanner.close();    // Clean up
        }
        catch(FileNotFoundException e){
            out.println(e);
        }
    }
    
    /* @param ofile: name of the output file
    *  @funct: reading requests from the request file and then
    *          get 3 lowest cost (either in terms of money/time, 
    *          the request file will specify the term) paths from
    *          starting location Src to destination. The request file
    *          will have this format:
    *                   #number_of_requests(only 1st line)
    *                   Src|Dst|Type
    *          whereas
    *               + Src: Starting location
    *               + Dst: Destination
    *               + Type: Cost or time (T for time, C for cost)
    */
    public void getRequestAndOutput(String ofile){
        FileWriter writer;
        try (Scanner scanner = new Scanner(ifile)) {    // Open a scanner
            writer = new FileWriter(new File(ofile));   // Try to open a writer
            int times = scanner.nextInt();              // Read the number of requests
            scanner.nextLine();                         // Read "\n"
            
            // From now on, the program will read all arguments from the request files to determine
            // all paths that a person can travel from starting location src to destination dst
            // (The program will do exhaustive search in order to this)
            // The program will then output or write to the output file with 3 lowest cost paths 
            // (either in terms of money or time).
            for(int i = 0; i < times; i++){
                // Get all necessary arguments
                String iline = scanner.nextLine();
                String[] args = iline.split("\\|");
                    
                List<FlightPath> request = new ArrayList<>();   // List of all requested flight
                this.getPath(args[0], args[1], new HashSet<>(), request, new FlightPath(), 0, 0);
                    
                // Output format
                writer.write("Flight " + (i + 1) + " ");
                writer.write(args[0]);
                writer.write("->");
                writer.write(args[1]);
                writer.write((args[2].equalsIgnoreCase("T")) ? " (Time)" : " (Cost)");
                writer.write(": \n");
                    
                if(request.size() == 0){    // Empty list -> impossible to travel from src to dst
                    writer.write("Cannot find any route!\n \n");
                }
                else{
                    // Sorting the list based on time or cost
                    if(args[2].equalsIgnoreCase("T")){
                        Collections.sort(request, (FlightPath p1, FlightPath p2) -> {
                            if(p1.getTime() < p2.getTime()) return -1;
                            if(p1.getTime() > p2.getTime()) return 1;
                            return 0;
                        });
                    }
                    else
                        Collections.sort(request, (FlightPath p1, FlightPath p2) -> {
                            if(p1.getCost() < p2.getCost()) return -1;
                            if(p1.getCost() > p2.getCost()) return 1;
                            return 0;
                        });
                       
                    // Outputing at most 3 lowest cost path
                    for(int j = 0; j < ((request.size() <= 3) ? request.size() : 3); j++){
                        writer.write(request.get(j).displayPath());
                    }
                    writer.write("\n");
                }    
            }
            writer.close(); // Clean up
        }
        catch(FileNotFoundException e){
            out.println(e);
        }
        catch(IOException e){
            out.println(e);
        }
    }
    
    /* @param src: Starting location
    *  @param dst: Destination
    *  @param visited: hash set to determine if the location already visited
    *  @param result: used to store all possible paths from src to dst
    *  @param path:  current path
    *  @param cost: cost (in money) to travel from src to dst
    *  @param time: time (in time) to travel from src to dst
    *  @funct: Doing exhaustive search to get all possible paths from src to dst
    */
    public void getPath(String src, String dst, Set<Flight> visited, List<FlightPath> result, FlightPath path, double cost, double time){
        Flight curr = graph.get(src);   // Get the current flight based on the current location
        // Check if the current location already visited the next location to prevent loop
        if(!visited.contains(curr)){
            path.addFlight(src, cost, time);    // Add the current location to the flight path
            visited.add(curr);
            
            if(src.equals(dst)){    // Reach destination!
                // Copying path in a new memory location 
                FlightPath officialPath = new FlightPath();
                officialPath.copyPath(path);
                
                result.add(officialPath);   // Store it to the list
            }
            else{
                // Doing exhaustive search, looking through all edges from src to dst
                for(int i = 0; i < curr.getDst().size(); i++){
                    double[] costFlight = curr.getDstWithCost().get(curr.getDst().get(i)); // Including cost of time and money
                    getPath(curr.getDst().get(i).getSrc(), dst, visited, result, path, cost + costFlight[0], time + costFlight[1]);
                }
            }
            // Path can be considered as a stack. Remove the last flight from the path
            path.removeFlight();
            // Remove the current location as it can be visited from different path
            visited.remove(curr);
        }
    }
    
    // Getters
    public Map<String, Flight> getGraph(){return this.graph;}
}

// Containing a path from one location to another
class FlightPath{
    final public String src;    // Current location
    private FlightPath pred;    // Previous location
    private FlightPath next;    // Next location
    private FlightPath tail;    // Last location
    private double time;        // Time it takes from starting location to the last location
    private double cost;        // Cost it takes from starting location to the last location
    
    // Default constructor
    public FlightPath(){
        src = "";
        next = null;
        tail = null;
        pred = null;
        time = 0;
        cost = 0;
    }
    
    // Forming a path based on the first location
    public FlightPath(String src){
        this.src = src;
        this.next = null;
        this.tail = this;
        this.pred = null;
        this.time = 0;
        this.cost = 0;
    }
    
    // Forming a path based on the given path
    public FlightPath(FlightPath path){
        this.src = path.src;
        this.time = path.time;
        this.cost = path.cost;
        this.next = path.next;
        this.pred = path.pred;
        this.tail = path.tail;
    }
    
    /*  @param flight:  a name of the new flight
    *   @param cost: cost it takes to fly from the starting location to the new location
    *   @param time: time it takes to fly from the starting location to the new location
    *   @funct: Appending a new flight to the path
    */
    public void addFlight(String flight, double cost, double time){
        this.cost = cost;
        this.time = time;
        
        // Appending the newest flight to the path
        if(this.next == null){
            this.next = new FlightPath(flight);
            this.next.pred = this;
            this.tail = this.next;
        }
        else{
            this.tail.next = new FlightPath(flight);
            this.tail.next.pred = this.tail;
            this.tail = this.tail.next;
        }
    }
    
    /*  @funct: displaying the series of flights from starting location to the last location
    */
    public String displayPath(){
        FlightPath dummy = this.next;
        StringBuilder pathBuilder = new StringBuilder();
        
        pathBuilder.append(dummy.src);
        // Traversing through the path
        while(dummy.next != null){
            pathBuilder.append("->");
            pathBuilder.append(dummy.next.src);
            dummy = dummy.next;
        }
        
        pathBuilder.append(".");
        pathBuilder.append(" Time: ");
        pathBuilder.append(this.time);
        pathBuilder.append(" Cost: ");
        pathBuilder.append(this.cost);
        pathBuilder.append("\n");
        
        return pathBuilder.toString();
    }
    
    /* @funct: "Remove" the last flight from the path
    */
    public void removeFlight(){
        this.tail = this.tail.pred;
        this.tail.next = null;
        this.tail.tail = null;
    }

    /*  @param path: An input path
    *   @funct: Copying all properties from the input path to this path
    */
    public void copyPath(FlightPath path){
        FlightPath dummy1 = this;
        FlightPath dummy2 = path;
        
        while(dummy2.next != null){
            dummy1.next = new FlightPath(dummy2.next);
            if(dummy2.pred != null)
                dummy1.pred = new FlightPath(dummy2.pred);
            if(dummy2.tail != null)
                dummy1.tail = new FlightPath(dummy2.tail);
            dummy1.cost = dummy2.cost;
            dummy1.time = dummy2.time;
            
            dummy1 = dummy1.next;
            dummy2 = dummy2.next;
        }
    }
    
    // Getters
    public double getCost(){return this.cost;}
    public double getTime(){return this.time;}
    public String getSrc(){return this.src;}
}