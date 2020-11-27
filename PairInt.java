public class PairInt implements Comparable<PairInt>{
    int a;
    int b;
    public PairInt(int a, int b){
        this.a = a;
        this.b = b;
    }

    @Override
    public int compareTo(PairInt p){
        if(a<p.a){
            return -1;
        }
        if(a>p.a){
            return 1;
        }
        return 0;
    }

}
