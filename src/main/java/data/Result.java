package data;

import java.util.List;
import java.util.Set;

public class Result {
    Set<String>       wallets;
    List<Transaction> cycle;
    int               tokenId;
    long              detected;

    public Result(Set<String> wallets, List<Transaction> cycle, int tokenId, long detected) {
        this.wallets  = wallets;
        this.cycle    = cycle;
        this.tokenId  = tokenId;
        this.detected = detected;
    }


    @Override
    public String toString() {
        return "Result{" +
                "wallets=" + wallets +
                ", tokenId=" + tokenId +
                ", detected=" + detected +
                ", cycle=" + cycle +
                '}';
    }
}
