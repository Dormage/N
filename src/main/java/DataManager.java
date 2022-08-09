import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import data.NftNode;
import data.NftTransfer;
import data.Result;
import data.Transaction;
import me.tongfei.progressbar.ProgressBar;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.alg.cycle.QueueBFSFundamentalCycleBasis;
import org.jgrapht.graph.DirectedWeightedPseudograph;

import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class DataManager {
    private Config                            config;
    private Database                          database;
    private ArrayList<NftTransfer>            edgeList;
    private HashMap<String, NftNode>          nodeList;
    private HashMap<Integer, HashSet<String>> tokenMap;
    private Graph<NftNode, NftTransfer>       nftGraph;
    private Graph<String, Transaction>        ethGraph;
    private Set<String>                       setOfWallets;
    private List<Result>                      results;
    private Set<String>                       blacklist;

    public DataManager(Config config, Set<String> blacklist) {
        this.config    = config;
        this.database  = new Database(config);
        edgeList       = new ArrayList<>();
        nodeList       = new HashMap<>();
        nftGraph       = new DirectedWeightedPseudograph<>(NftTransfer.class);
        ethGraph       = new DirectedWeightedPseudograph<>(Transaction.class);
        setOfWallets   = new HashSet<>();
        results        = new ArrayList<>();
        tokenMap       = new HashMap<>();
        this.blacklist = blacklist;
    }

    public void loadData() {
        loadEdges();
        buildTokenMap();
        //buildNftGraph();
        buildEthGraph();
        findCyclesEth();
        System.out.println(Constants.INFO + "Loading data complete. " + " Memory used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024102 + "MB");
        System.out.println(Constants.INFO + "NFT Graph contains " + nftGraph.edgeSet().size() + " edges");
        System.out.println(Constants.INFO + "NFT Graph contains " + nftGraph.vertexSet().size() + " vertices");

        System.out.println(Constants.INFO + "ETH Graph contains " + ethGraph.edgeSet().size() + " edges");
        System.out.println(Constants.INFO + "ETH Graph contains " + ethGraph.vertexSet().size() + " vertices");

    }

    public void buildTokenMap() {
        edgeList.stream().forEach(nftTransfer -> {
            if (!tokenMap.keySet().contains(nftTransfer.tokenId)) {
                tokenMap.put(nftTransfer.tokenId, new HashSet<>());
            }
            tokenMap.get(nftTransfer.tokenId).add(nftTransfer.from);
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
        String query = "SELECT * FROM Transfers";
        try (Statement stmt = database.conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                NftTransfer edge = new NftTransfer(rs.getInt("id"), rs.getDouble("amount"), rs.getString("collection_address"), rs.getInt("block_no"), rs.getString("currency"), rs.getString("from"), rs.getString("to"), rs.getInt("token_id"), rs.getString("transaction_hash"));
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
    public Graph buildNftGraph() {
        edgeList.stream().forEach(edge -> {
            nodeList.putIfAbsent(edge.from, new NftNode(edge.from));
            nodeList.putIfAbsent(edge.to, new NftNode(edge.to));
        });
        nodeList.forEach((s, nftNode) -> nftGraph.addVertex(nftNode));
        edgeList.stream().forEach(edge -> {
            nftGraph.addEdge(nodeList.get(edge.from), nodeList.get(edge.to), edge);
        });
        return nftGraph;
    }

    public void findCyclesEth() {
        System.out.println(Constants.RESULT + "Detected " + new QueueBFSFundamentalCycleBasis<>(this.nftGraph).getCycleBasis().getCycles().size() + " Cycles");
        List<List<String>> cycles = new HawickJamesSimpleCycles<>(this.ethGraph).findSimpleCycles();
        cycles.forEach(System.out::println);
        System.out.println(Constants.INFO + "Number of cycles detected: " + cycles.size());

        ProgressBar.wrap(tokenMap.keySet().parallelStream(), "Cycle Search").forEach(token -> {
            cycles.forEach(cycle -> {
                long count = cycle.stream().filter(wallet -> tokenMap.get(token).contains(wallet)).count();
                if (count > 1) {
                    System.err.println(Constants.SUCCESS + " Found match for token " + token + " matching " + count + " wallets in cycle of size: " + cycle.size());
                }
            });
        });
    }


    public void findCycles() {
        System.out.println(Constants.RESULT + "Detected " + new QueueBFSFundamentalCycleBasis<>(this.nftGraph).getCycleBasis().getCycles().size() + " Cycles");
        new QueueBFSFundamentalCycleBasis<>(this.nftGraph).getCycleBasis().getCycles().forEach(cycle -> {
            cycle.forEach(nftTransfer -> {
                System.out.println(Constants.INFO + "Searching cycles containing vertex: " + nftTransfer.from);
                Set<String> tmp   = new CycleDetector<>(ethGraph).findCyclesContainingVertex(nftTransfer.from);
                long        count = cycle.stream().filter(nft -> tmp.contains(nft.from)).count();
                System.out.println(Constants.SUCCESS + " Found collusion matching " + count + " wallets in cycle of size: " + cycle.size() + " for tokenId: " + nftTransfer.tokenId + " Ratio: " + (count * 1f / cycle.size()) * 100 + " %");
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
        try {
            parser.beginParsing(new InputStreamReader(new FileInputStream(transactions)));
            String[] row;
            while ((row = parser.parseNext()) != null) {
                if (row.length < 7) {
                    System.out.println(Constants.ERROR + "Broken line: " + row);
                    continue;
                }
                Transaction transaction = new Transaction(row);
                if (transaction.getFrom_address() != null && transaction.getTo_address() != null &&
                        !(blacklist.contains(transaction.getFrom_address())) || blacklist.contains(transaction.getTo_address()) &&
                        (transaction.getFrom_address() != transaction.getTo_address())) {
                    ethGraph.addVertex(transaction.getTo_address());
                    ethGraph.addVertex(transaction.getFrom_address());
                    ethGraph.addEdge(transaction.getFrom_address(), transaction.getTo_address(), transaction);
                } else {
                    //System.out.println(Constants.ERROR + transaction);
                }
                if (processed > 10000) {
                    return;
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
    }
}