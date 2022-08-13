package data;

import java.sql.Timestamp;

public class NftTransfer extends Trx implements Comparable<NftTransfer>{
    public int id;
    public double amount;
    public String collection_address;
    public int block;
    public String currency;
    public String from;
    public String to;
    public int tokenId;
    public String transactionHash;

    String gas_price;

    String gas_used;

    public NftTransfer(int id, double amount, String collection_address, int block, String currency, String from, String to, int tokenId, String transactionHash, String gas_price, String gas_used) {
        super(transactionHash,from,to);
        this.id = id;
        this.amount = amount;
        this.collection_address = collection_address;
        this.block = block;
        this.currency = currency;
        this.tokenId = tokenId;
        this.gas_price = gas_price;
        this.gas_used =  gas_used;
    }

    @Override
    public String toString() {
        return "NftTransfer{" +
                "amount=" + amount +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }

    @Override
    public String[] toCSV() {
        return new String[] {"",this.getHash(),this.getFromAddress(),this.getToAddress(),currency,Double.toString(amount),Integer.toString(block),Integer.toString(tokenId),gas_price,gas_used};
    }


    @Override
    public int compareTo(NftTransfer nftTransfer) {
        if(this.block == nftTransfer.block){
            return 0;
        }else if(this.block < nftTransfer.block){
            return -1;
        }else {
            return 1;
        }
    }
}
