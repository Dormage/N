import Export.ExportCycles;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import data.*;
import me.tongfei.progressbar.ProgressBar;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.alg.cycle.QueueBFSFundamentalCycleBasis;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import static java.lang.Thread.sleep;

public class DataManager {
    private Config                            config;
    private Database                          database;
    private ArrayList<NftTransfer>            edgeList;
    private HashMap<String, NftNode>          nodeList;
    private HashMap<Integer, HashSet<NftTransfer>> tokenMap;
    private Graph<String, Link>  nftGraph;
    private Graph<String, Link>  ethGraph;
    private Set<String>                       setOfWallets;
    private List<Result>                      results;
    private Set<String>                       blacklist;
    private List<List<String>>                cycles;

    public static int max_path_len;

    public static long                          totalPaths;

    public static int removed;

    public DataManager(Config config, Set<String> blacklist) {
        this.config    = config;
        this.database  = new Database(config);
        edgeList       = new ArrayList<>();
        nodeList       = new HashMap<>();
        nftGraph       = new SimpleDirectedGraph<>(Link.class);
        ethGraph       = new SimpleDirectedGraph<>(Link.class); //DirectedPseudograph<>(Link.class);
        setOfWallets   = new HashSet<>();
        results        = new ArrayList<>();
        tokenMap       = new HashMap<>();
        this.blacklist = blacklist;
    }

    public void loadData() {
        //exportETH4DIB();
        loadEdges();
        buildTokenMap();
        buildEthGraph();
        findPathsPerNft();
        //buildNftGraph();
        //buildEthGraph();
        //findCyclesEth();
        //computeCycleWeight();
        //printHead();
        System.out.println(Constants.INFO + "Loading data complete. " + " Memory used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024102 + "MB");
        System.out.println(Constants.INFO + "NFT Graph contains " + nftGraph.edgeSet().size() + " edges");
        System.out.println(Constants.INFO + "NFT Graph contains " + nftGraph.vertexSet().size() + " vertices");

        System.out.println(Constants.INFO + "ETH Graph contains " + ethGraph.edgeSet().size() + " edges");
        System.out.println(Constants.INFO + "ETH Graph contains " + ethGraph.vertexSet().size() + " vertices");

    }

    public void exportETH4DIB(){
        buildEthGraph();
        List<List<String>> cycles = findCycles(this.ethGraph);
        ExportCycles ec = new ExportCycles("eth_data",this.ethGraph,cycles);
        ec.exportETHRoutine();
    }


    public void exportNFT4DIB(){
        loadEdges();
        buildNftGraph();
        List<List<String>> cycles = findCycles(this.nftGraph);
        ExportCycles ec = new ExportCycles("nft_data",this.nftGraph,cycles);
        ec.exportNFTRoutine();
    }

    public void buildTokenMap() {
        edgeList.stream().forEach(nftTransfer -> {
            if (!tokenMap.keySet().contains(nftTransfer.tokenId)) {
                tokenMap.put(nftTransfer.tokenId, new HashSet<>());
            }
            tokenMap.get(nftTransfer.tokenId).add(nftTransfer);
        });
    }

    public void loadVertices() {
        String query = "SELECT * FROM Address WHERE isSmartContract = 0";
        try (Statement stmt = database.conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                NftNode node = new NftNode(rs.getString("address"));
                nodeList.put(node.address, node);
            }
            System.out.println(Constants.INFO + "Successfully imported " + nodeList.size() + " Nodes");
        } catch (Exception e) {
            System.out.println(Constants.ERROR + "Edge query error: " + e.getMessage());
        }
    }

    public void loadEdges() {
        String query = "SELECT * FROM Transfers ";
        try (Statement stmt = database.conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                NftTransfer edge = new NftTransfer(rs.getInt("id"), rs.getDouble("amount"), rs.getString("collection_address"), rs.getInt("block_no"), rs.getString("currency"), rs.getString("from"), rs.getString("to"), rs.getInt("token_id"), rs.getString("transaction_hash"),rs.getString("gas_price"),rs.getString("gas_used"));
                edgeList.add(edge);
            }
            System.out.println(Constants.INFO + "Successfully imported " + edgeList.size() + " Edges");
        } catch (Exception e) {
            System.out.println(Constants.ERROR + "Edge query error: " + e.getMessage());
        }
    }

    /**
     * Builds a DirectedWeightedPseudograph from previously loaded transaction data.
     * <p>
     * This method assumes the data was preloaded with loading edges, otherwise returns null.
     * The returned graph is a directed graph in which vertices are Ethereum wallets, and edges are transactions.
     *
     * @return A DirectedWeightedPseudograph
     **/
  /*  public Graph buildNftGraphNftNodes() {
        edgeList.stream().forEach(edge -> {
            nodeList.putIfAbsent(edge.from, new NftNode(edge.from));
            nodeList.putIfAbsent(edge.to, new NftNode(edge.to));
        });
        nodeList.forEach((s, nftNode) -> nftGraph.addVertex(nftNode));
        edgeList.stream().forEach(edge -> {
            nftGraph.addEdge(nodeList.get(edge.from), nodeList.get(edge.to), edge);
        });
        return nftGraph;
    }*/

    public void buildNftGraph(){
        long start = System.currentTimeMillis();
        removed = 0;
        edgeList.stream().forEach(transfer -> {
            if(!transfer.getFromAddress().equals(transfer.getToAddress())) {
                Link currentEdge = nftGraph.getEdge(transfer.getFromAddress(), transfer.getToAddress());
                if (currentEdge == null) {
                    nftGraph.addVertex(transfer.getToAddress());
                    nftGraph.addVertex(transfer.getFromAddress());
                    nftGraph.addEdge(transfer.getFromAddress(), transfer.getToAddress(), new Link<NftTransfer>(transfer));
                } else {
                    currentEdge.addEdge(transfer);
                }
            }else{
                removed++;
            }
        });
        System.out.println(Constants.INFO + " number of removed self loops: "+removed);
        System.out.println(Constants.RESULT + "Built graph of size: " + nftGraph.vertexSet().size() + " vertexes " + nftGraph.edgeSet().size()+ " links in seconds: "+ (System.currentTimeMillis()-start)/1000);
    }

    public List<List<String>> findCycles(Graph graph){
        long start = System.currentTimeMillis();
        HawickJamesSimpleCycles cycleFinder = new HawickJamesSimpleCycles<>(graph);
        //cycleFinder.setPathLimit(20);
        List<List<String>> cycles = cycleFinder.findSimpleCycles();
        // det max path legnth for ETH
        //cycles.forEach(System.out::println);
        System.out.println(Constants.INFO + "Number of cycles detected: " + cycles.size()+" in seconds: "+ (System.currentTimeMillis()-start)/1000);
        //return cycles;
        return cycles;
    }

    public void findCyclesEth() {
        System.out.println(Constants.RESULT + "Graph of size: " + this.ethGraph.vertexSet().size() + " vertexes " + this.ethGraph.edgeSet().size()+ " links");
        this.cycles = new HawickJamesSimpleCycles<>(this.ethGraph).findSimpleCycles();
        //cycles.forEach(System.out::println);
        System.out.println(Constants.INFO + "Number of cycles detected: " + this.cycles.size());

        ProgressBar.wrap(tokenMap.keySet().parallelStream(), "Cycle Search").forEach(token -> {
            this.cycles.forEach(cycle -> {
                long count = cycle.stream().filter(wallet -> tokenMap.get(token).contains(wallet)).count();
                if (count > 1) {
                    //System.err.println(Constants.SUCCESS + " Found match for token " + token + " matching " + count + " wallets in cycle of size: " + cycle.size());
                    updateWeights(cycle,count);
                }
            });
        });
    }

    public void updateWeights(List<String> cycle, long count){
        Link currentLink = null;
        for (int i = 0; i < cycle.size()-1 ;i++ ) {
            currentLink = ethGraph.getEdge(cycle.get(i), cycle.get(i+1));
            currentLink.addWeight(count / cycle.size());
        }
        currentLink = ethGraph.getEdge(cycle.get(cycle.size()-1), cycle.get(0));
        currentLink.addWeight(count / cycle.size());
    }

    public void computeCycleWeight(){
        ProgressBar.wrap(this.cycles.parallelStream(), "Compute cycle weight").forEach(cycle -> {
            List<Link> listOfLinks = new ArrayList<>();
            double weight = 0;
            Link currentLink = null;
            for (int i = 0; i < cycle.size()-1 ;i++ ) {
                currentLink = ethGraph.getEdge(cycle.get(i), cycle.get(i+1));
                listOfLinks.add(currentLink);
                weight += currentLink.getWeight();
            }
            currentLink = ethGraph.getEdge(cycle.get(cycle.size()-1), cycle.get(0));
            listOfLinks.add(currentLink);
            weight += currentLink.getWeight();
            if (listOfLinks != null) {
                this.results.add(new Result(listOfLinks, weight));
            }
            //double weight = this.ethGraph.edgeSet(this.ethGraph.getvertex).stream().mapToDouble(Link::getWeight).sum();
        });
    }


    public void printHead(){
        Collections.sort(this.results);
        Collections.reverse(this.results);
        for (int i = 0; i < 10; i++){
            this.results.get(i).printCycle();
        }

    }


    public void findPathsPerNft(){
        max_path_len = 4;
        System.out.println("Search paths for max path length: "+ max_path_len);
        totalPaths = 0;
        ProgressBar.wrap(tokenMap.keySet().parallelStream(), "Find paths per NFT").forEach(token -> {
           List<NftTransfer> list = new ArrayList<>(tokenMap.get(token)); // nft transfers per NFT
           Collections.sort(list); // transfers must be sorted in ASC order
           List<String> vertexes = filterNotInEth(list);
           Set<String> source = new HashSet<>(); //source vertex
           Set<String> destination;
           long allPaths = 0;
           while( vertexes.size() > 1 ){
               source.add(vertexes.remove(0));
               destination = new HashSet<>(vertexes);
               List<GraphPath<String,Link>> paths = new AllDirectedPaths(ethGraph).getAllPaths(source,destination,true,max_path_len);
               allPaths += paths.size();
               totalPaths += paths.size();
               source.clear();
               destination.clear();
           }
           System.out.println("Token ID: "+token + " found "+ allPaths + " paths");
        });
        System.out.println("------Total paths: " + totalPaths);
    }

    // filter out addresses not present in the Eth graph and return all addresses as list
    public List<String> filterNotInEth(List<NftTransfer> transfers){
        List<String> addresses = new ArrayList<>();
        for (NftTransfer transfer: transfers ) {
            if(this.ethGraph.vertexSet().contains(transfer.getFromAddress())){
                addresses.add(transfer.getFromAddress());
            }
        }
        if(this.ethGraph.vertexSet().contains(transfers.get(transfers.size()-1).getToAddress())){
            addresses.add(transfers.get(transfers.size()-1).getToAddress());
        }
        return addresses;
    }


    /**
     * Given a path GraphPath<String,Link> measure the TVL of the path
     *
     *
     */



    public void findCycles() {
        System.out.println(Constants.RESULT + "Detected " + new QueueBFSFundamentalCycleBasis<>(this.nftGraph).getCycleBasis().getCycles().size() + " Cycles");
        new QueueBFSFundamentalCycleBasis<>(this.nftGraph).getCycleBasis().getCycles().forEach(cycle -> {
            cycle.forEach(nftTransfer -> {
                System.out.println(Constants.INFO + "Searching cycles containing vertex: " + nftTransfer.getFromAddress());
                Set<String> tmp   = new CycleDetector<>(ethGraph).findCyclesContainingVertex(nftTransfer.getFromAddress());
                long        count = cycle.stream().filter(nft -> tmp.contains(nft.getFromAddress())).count();
                System.out.println(Constants.SUCCESS + " Found collusion matching " + count + " wallets in cycle of size: " + cycle.size() + " for tokenId: + nftTransfer.tokenId +  Ratio: " + (count * 1f / cycle.size()) * 100 + " %");
            });
            System.out.println();
        });
    }

    public void buildEthGraph() {
        File transactions = new File(config.transactionFile);
        System.out.println(Constants.SUCCESS + "Opening new file: " + config.transactionFile);
        List<Transaction> transactionBatch;
        long              processed = 0;
        CsvParserSettings settings  = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        CsvParser parser = new CsvParser(settings);
        long start = System.currentTimeMillis();
        try {
            parser.beginParsing(new InputStreamReader(new FileInputStream(transactions)));
            String[] row;
            while ((row = parser.parseNext()) != null) {
                if (row.length < 7) {
                    System.out.println(Constants.ERROR + "Broken line: " + row);
                    continue;
                }
                Transaction transaction = new Transaction(row);
                if ((transaction.getFromAddress() != null && transaction.getToAddress() != null) &&
                        !(blacklist.contains(transaction.getFromAddress()) || blacklist.contains(transaction.getToAddress())) &&
                        (!transaction.getFromAddress().equals(transaction.getToAddress()))) {
                    Link currentEdge = ethGraph.getEdge(transaction.getFromAddress(),transaction.getToAddress());
                    if(currentEdge==null){
                        ethGraph.addVertex(transaction.getToAddress());
                        ethGraph.addVertex(transaction.getFromAddress());
                        ethGraph.addEdge(transaction.getFromAddress(), transaction.getToAddress(), new Link<Transaction>(transaction));
                    }else{
                         currentEdge.addEdge(transaction);
                    }


                } else {
                    //System.out.println(Constants.ERROR + transaction);
                }
                processed++;
                if (processed % 1000000 == 0) {
                    System.out.println(Constants.INFO + "Processed 100000 transactions " + " Memory used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024102 + "MB");
                    processed = 0;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(Constants.RESULT + "Built graph of size: " + ethGraph.vertexSet().size() + " vertexes " + ethGraph.edgeSet().size()+ " links in seconds: "+ (System.currentTimeMillis()-start)/1000);
    }
}