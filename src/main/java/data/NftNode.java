package data;

public class NftNode {
    public String address;

    public NftNode(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return address +" ";
    }
}
