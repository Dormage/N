package data;

public class NftTransfer {
    public int id;
    public double amount;
    public String collection_address;
    public int block;
    public String currency;
    public String from;
    public String to;
    public int tokenId;
    public String transactionHash;

    public NftTransfer(int id, double amount, String collection_address, int block, String currency, String from, String to, int tokenId, String transactionHash) {
        this.id = id;
        this.amount = amount;
        this.collection_address = collection_address;
        this.block = block;
        this.currency = currency;
        this.from = from;
        this.to = to;
        this.tokenId = tokenId;
        this.transactionHash = transactionHash;
    }
}
