package data;

import java.lang.reflect.Executable;
import java.util.*;

public class Link<T extends Trx> {
    private HashMap<String, Trx> edges;
    private double weight;

    private int linkId;

    String fromAddress;

    String toAddress;

    public Link(Trx trx)
    {
        edges = new HashMap<>();
        edges.put(trx.getHash(),trx);
        this.fromAddress = trx.getFromAddress();
        this.toAddress = trx.getToAddress();
        weight = 0;
    }

    public void addEdge(Trx trx){
        edges.put(trx.getHash(), trx);
    }

    public void addWeight(double weight){
        this.weight = this.weight + weight;
    }


    public double getWeight() {
        return weight;
    }


    public String getFromAddress(){
        return this.fromAddress;
    }

    public String getToAddress(){
        return this.toAddress;
    }

    public String transactionsToString(){
        String res = "";
        Iterator<Map.Entry<String, Trx>> iterator = edges.entrySet().iterator();
        while(iterator.hasNext()){
            res += iterator.next().getKey() + ",";
        }
        return res;
    }


    public int getLinkId() {
        return linkId;
    }

    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }


    public List<String[]> toCSV(){
        List<String[]> rows = new ArrayList<>();
        Iterator<Map.Entry<String, Trx>> iterator = edges.entrySet().iterator();
        String[] tmp;
        while(iterator.hasNext()){
            tmp = iterator.next().getValue().toCSV();
            tmp[0] = Integer.toString(this.linkId); // assign link id
            rows.add(tmp);
        }
        return rows;
    }




}


