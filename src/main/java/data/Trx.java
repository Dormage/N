package data;

import java.math.BigInteger;

public abstract class Trx {

    String hash;

    String fromAddress;

    String toAddress;

    public Trx(String hash, String fromAddress, String toAddress){

        this.hash = hash;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;

    }

    public String getHash() {
        return hash;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public abstract String[] toCSV();



    public double weiToEth(BigInteger amount){
        // one eth is 10**18 wei
        // remove last 12 digits and convert to long
        if(amount.toString().length()<=12){
            return 0.0;
        }
        long value = Long.parseLong(amount.toString().substring(0,amount.toString().length()-12));
        return value / (Math.pow(10,6));
    }

    public long weiToGwei(BigInteger amount){
        long gwei = Long.parseLong(amount.divide(new BigInteger("1000000000")).toString());
        return gwei;
    }

}
