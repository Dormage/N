package data;

import java.math.BigInteger;

public class Transaction extends Trx{

    int        nonce;
    int        transaction_index;

    double value;
    String gas;
    String gas_price;
    String     input;
    long       block_timestamp;
    long       block_number;
    String     block_hash;
    int        transaction_type;

    public Transaction(String[] line) {
        super(line[0],line[5],line[6]);
        //this.nonce             = Integer.parseInt(line[1]);
        //this.block_hash        = line[2];
        this.block_number      = Long.parseLong(line[3]);
        //this.transaction_index = Integer.parseInt(line[4]);
        this.value             = this.weiToEth(new BigInteger(line[7]));
        this.gas               = line[8];
        this.gas_price         = line[9];
        //this.input             = line[10];
        //this.block_timestamp   = Long.parseLong(line[11]) * 1000;
        //this.max_fee_per_gas = new BigInteger((line[12].equals("")) ? "0" : line[12]);
        //this.max_priority_fee_per_gas = new BigInteger((line[13].equals("")) ? "0" : line[13]);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "hash='" + hash + '\'' +
                ", from_address='" + this.getFromAddress()+ '\'' +
                ", to_address='" + this.getToAddress() + '\'' +
                ", block_number=" + block_number +
                ", block_hash='" + block_hash + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public String getHash() {
        return hash;
    }




    @Override
    public String[] toCSV() {
        return new String[] {"",this.getHash(),this.getFromAddress(),this.getToAddress(),"ETH",Double.toString(value),Long.toString(block_number),gas_price,gas};
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


    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getGas_price() {
        return gas_price;
    }

    public void setGas_price(String gas_price) {
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
