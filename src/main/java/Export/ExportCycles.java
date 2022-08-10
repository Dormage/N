package Export;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import data.Link;
import data.NftLink;
import data.NftTransfer;
import data.Result;
import me.tongfei.progressbar.ProgressBar;
import org.jgrapht.Graph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;


public class ExportCycles {

    public FileOutputStream fileOutputStream = null;

    public CsvWriter writer;

    public int batchSize;

    public String fileName;

    private Graph<String, Link> graph;

    private List<List<String>> cycles;

    private List<Object[]> batch = new ArrayList<>(); // list of strings


    private int id = 0;


    public ExportCycles(String fileName, Graph<String, Link> graph, List<List<String>> cycles){
        batchSize = 1000;
        this.graph = graph;
        this.cycles = cycles;
        this.fileName = fileName;
    }

    public void exportRoutine(){
        setFile("graph");
        setLinkId();
        addNftTransferHeader();
        printGraph();
        dumpBatch();
        closeWriter();
        setFile("cycles");
        addCycleHeader();
        printCycles();
        dumpBatch();
        closeWriter();
    }

    public void setFile(String type){
        try {
            this.fileOutputStream = new FileOutputStream(new File(this.fileName+"_"+type+".csv"),false);
            Writer outputWriter = new OutputStreamWriter(fileOutputStream,"UTF-8");
            this.writer = new CsvWriter(outputWriter, new CsvWriterSettings());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void printCycles(){
        id = 0;
        ProgressBar.wrap(this.cycles.stream(), " Write cycles on file").forEach(cycle -> {
            List<String[]> rows = new ArrayList<>();
            Link currentLink = null;
            for (int i = 0; i < cycle.size()-1 ;i++ ) {
                currentLink = this.graph.getEdge(cycle.get(i), cycle.get(i+1));
                rows.add(new String[]{Integer.toString(id),Integer.toString(currentLink.getLinkId())});
            }
            currentLink = graph.getEdge(cycle.get(cycle.size()-1), cycle.get(0));
            rows.add(new String[]{Integer.toString(id),Integer.toString(currentLink.getLinkId())});
            if (rows.size()>0) {
                feed(rows);
            }
            id++;
        });
    }


    public void setLinkId(){
        id = 0;
        this.graph.edgeSet().forEach(link -> {
            link.setLinkId(id);
            id +=1;
        });
    }

    public void printGraph(){
        ProgressBar.wrap(this.graph.edgeSet().stream(), " Write graph on file").forEach(link -> {
            feed(link.toCSV());
        });
    }



    public void feed(List<String[]> rows){
        if(batch.size()>batchSize){
            writeBatch();
            batch.clear();
        }
        //System.out.print(".");
        for (String[] row: rows ) {
            batch.add(row);
        }
    }


    public void dumpBatch(){
        if(batch.size()>0) {
            writeBatch();
            batch.clear();
        }
    }

    public void writeBatch(){
        try {
            writer.writeRows(this.batch);
        } catch (Exception e) {
            // handle exception
            System.out.println("An exception occurred when writing: "+ e);
        }
    }

    public void closeWriter(){
        try {
            writer.close();
        } catch (Exception e) {
            // handle exception
            System.out.println("An exception occurred when closing writer: "+ e);
        }
    }

    public void addTransactionHeader() {
        batch.add(new String[] {"edge_id","transaction_hash","from_address","to_address","currency","amount","block_number","gas_price","gas_used"});
    }

    public void addCycleHeader() {
        batch.add(new String[] {"cycle_id","edge_id"});
    }

    public void addNftTransferHeader() {
        batch.add(new String[] {"edge_id","transaction_hash","from_address","to_address","currency","amount","block_number","token_id","gas_price","gas_used"});
    }




}
