public class Pair implements Comparable<Pair>{
    PairInt ai;
    byte[] b;
    public Pair(PairInt a, byte[] b){
        this.ai = a;
        this.b = b;
    }

    @Override
    public int compareTo(Pair p){
        if(ai.a<p.ai.a){
            return -1;
        }
        if(ai.a>p.ai.a){
            return 1;
        }
        return 0;
    }

    @Override
    public String toString(){
        String s="";
        for(int i=0;i<b.length;i++){
            s+= (char)b[i];
        }
        return s;
    }
}
