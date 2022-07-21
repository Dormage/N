import data.NftNode;
import data.NftTransfer;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.cycle.QueueBFSFundamentalCycleBasis;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class DataManager {
    private Config config;
    private Database database;
    private ArrayList<NftTransfer> edgeList;
    private HashMap<String, NftNode> nodeList;
    private Graph<NftNode, NftTransfer> nftGraph;

    public DataManager(Config config) {
        this.config = config;
        this.database = new Database(config);
        edgeList = new ArrayList<>();
        nodeList = new HashMap<>();
        nftGraph = new DirectedWeightedPseudograph<>(NftTransfer.class);
    }

    public void loadData() {
        loadEdges();
        //loadVertices();
        buildNftGraph();
        System.out.println(Constants.INFO + "Loading data complete. " + " Memory used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024102 + "MB");
        System.out.println(Constants.INFO + "Graph contains " + nftGraph.edgeSet().size() + " edges");
        System.out.println(Constants.INFO + "Graph contains " + nftGraph.vertexSet().size() + " vertices");
    }

    public void loadVertices() {
        String query = "SELECT * FROM Address WHERE isSmartContract = 0";
        try (Statement stmt = database.conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                NftNode node = new NftNode(
                        rs.getString("address")
                );
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
                NftTransfer edge = new NftTransfer(
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getString("collection_address"),
                        rs.getInt("block_no"),
                        rs.getString("currency"),
                        rs.getString("from"),
                        rs.getString("to"),
                        rs.getInt("token_id"),
                        rs.getString("transaction_hash")
                );
                edgeList.add(edge);
            }
            System.out.println(Constants.INFO + "Successfully imported " + edgeList.size() + " Edges");
        } catch (Exception e) {
            System.out.println(Constants.ERROR + "Edge query error: " + e.getMessage());
        }
    }

    public void buildNftGraph() {
        //map all vertices from transactions
        edgeList.stream().forEach(edge -> {
            nodeList.putIfAbsent(edge.from, new NftNode(edge.from));
            nodeList.putIfAbsent(edge.to, new NftNode(edge.to));
        });
        //add all vertices
        nodeList.forEach((s, nftNode) -> nftGraph.addVertex(nftNode));
        //add all edges
        edgeList.stream().forEach(edge -> {
            nftGraph.addEdge(nodeList.get(edge.from), nodeList.get(edge.to), edge);
        });
        System.out.println(Constants.SUCCESS + "Graph successfully built!");
    }

    public void detectCycles() {
        System.out.println(Constants.RESULT + "Graph has Cycles: " + new CycleDetector<>(this.nftGraph).detectCycles() + "");
        System.out.println(Constants.RESULT + "Detected " + new QueueBFSFundamentalCycleBasis<>(this.nftGraph).getCycleBasis().getCycles().size() + " Cycles");
    }
}