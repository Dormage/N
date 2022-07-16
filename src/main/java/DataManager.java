import data.NftNode;
import data.NftTransfer;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class DataManager {
    private Config config;
    private Database database;
    private ArrayList<NftTransfer> edgeList;
    private HashMap<String,NftNode> nodeList;
    private Graph<NftNode, NftTransfer> nftGraph;

    public DataManager(Config config) {
        this.config = config;
        this.database = new Database(config);
        edgeList = new ArrayList<>();
        nodeList = new HashMap<>();
        nftGraph = new DirectedWeightedMultigraph<>(NftTransfer.class);
    }

    public void loadData(){
        loadEdges();
        loadVertices();
        buildNftGraph();
        System.out.println(Constants.SUCCESS + " Loading data complete. "  +  " Memory used: " +(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024102 + "MB");
    }

    public void loadVertices(){
        String query = "SELECT * FROM Address WHERE isSmartContract = 0";
        try (Statement stmt = database.conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                NftNode node = new NftNode(
                        rs.getString("address")
                );
                nodeList.put(node.address,node);
            }
            System.out.println(Constants.INFO + "Successfully imported "+ nodeList.size() +  " Nodes");
        }catch (Exception e){
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
            System.out.println(Constants.INFO + "Successfully imported "+ edgeList.size() +  " Edges");
        }catch (Exception e){
            System.out.println(Constants.ERROR + "Edge query error: " + e.getMessage());
        }
    }
    public void buildNftGraph(){
        nodeList.values().forEach(nftNode -> nftGraph.addVertex(nftNode));
        edgeList.stream().filter(nftTransfer -> {
            return nodeList.values().contains(nftTransfer.to);
        }).forEach(nftTransfer -> nftGraph.addEdge(
                nodeList.get(nftTransfer.from),
                nodeList.get(nftTransfer.to),
                nftTransfer
                ));
        System.out.println(Constants.SUCCESS + " Graph successfully built!");
    }
    public void detectCycles(){
        System.out.println(Constants.WARN+ "Detected " + new HawickJamesSimpleCycles(this.nftGraph).findSimpleCycles().size() +" Cycles");
    }
}