package data;

import java.math.BigInteger;

public class Transaction {
    String     hash;
    int        nonce;
    int        transaction_index;
    String     from_address;
    String     to_address;
    BigInteger value;
    BigInteger gas;
    BigInteger gas_price;
    String     input;
    long       block_timestamp;
    long       block_number;
    String     block_hash;
    int        transaction_type;

    public Transaction(String[] line) {
        this.hash              = line[0];
        //this.nonce             = Integer.parseInt(line[1]);
        //this.block_hash        = line[2];
        this.block_number      = Long.parseLong(line[3]);
        //this.transaction_index = Integer.parseInt(line[4]);
        this.from_address      = line[5];
        this.to_address        = line[6];
        this.value             = new BigInteger(line[7]);
        //this.gas               = new BigInteger(line[8]);
        //this.gas_price         = new BigInteger(line[9]);
        //this.input             = line[10];
        //this.block_timestamp   = Long.parseLong(line[11]) * 1000;
        //this.max_fee_per_gas = new BigInteger((line[12].equals("")) ? "0" : line[12]);
        //this.max_priority_fee_per_gas = new BigInteger((line[13].equals("")) ? "0" : line[13]);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "hash='" + hash + '\'' +
                ", from_address='" + from_address + '\'' +
                ", to_address='" + to_address + '\'' +
                ", block_number=" + block_number +
                ", block_hash='" + block_hash + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public int getTransaction_index() {
        return transaction_index;
    }

    public void setTransaction_index(int transaction_index) {
        this.transaction_index = transaction_index;
    }

    public String getFrom_address() {
        return from_address;
    }

    public void setFrom_address(String from_address) {
        this.from_address = from_address;
    }

    public String getTo_address() {
        return to_address;
    }

    public void setTo_address(String to_address) {
        this.to_address = to_address;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public BigInteger getGas() {
        return gas;
    }

    public void setGas(BigInteger gas) {
        this.gas = gas;
    }

    public BigInteger getGas_price() {
        return gas_price;
    }

    public void setGas_price(BigInteger gas_price) {
        this.gas_price = gas_price;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public long getBlock_timestamp() {
        return block_timestamp;
    }

    public void setBlock_timestamp(long block_timestamp) {
        this.block_timestamp = block_timestamp;
    }

    public long getBlock_number() {
        return block_number;
    }

    public void setBlock_number(long block_number) {
        this.block_number = block_number;
    }

    public String getBlock_hash() {
        return block_hash;
    }

    public void setBlock_hash(String block_hash) {
        this.block_hash = block_hash;
    }

    public int getTransaction_type() {
        return transaction_type;
    }

    public void setTransaction_type(int transaction_type) {
        this.transaction_type = transaction_type;
    }
}
