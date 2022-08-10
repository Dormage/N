package data;

import java.util.List;
import java.util.Set;

public class Result implements Comparable<Result> {
    List<Link>        cycle;

    double            totalWeight;
    public Result(List<Link> cycle, double totalWeight) {
        this.cycle    = cycle;
        this.totalWeight = totalWeight;
    }


    @Override
    public String toString() {
        return "Result{" +
                ", weight=" + totalWeight +
                ", cycle=" + cycle +
                '}';
    }



    @Override
    public int compareTo(Result result) {
        if(this.totalWeight == result.totalWeight){
            return 0;
        }else if(this.totalWeight < result.totalWeight){
            return -1;
        }else {
            return 1;
        }
    }

    public void printCycle(){
        cycle.stream().forEach(link -> {
            System.out.print(link.getToAddress()+ " -["+ link.transactionsToString()+ "]- ");
        });
        System.out.println();
        System.out.println("Cycle weight: "+ totalWeight + " Cicle length: "+ cycle.size());
    }

}
