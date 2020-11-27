import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// This class implements the Queue
public class queue<V extends Comparable<V>>{

    private List<V> q;
    private int capacity, currentSize, front, rear;
	
    public queue(int capacity) {
    	this.front = 0;
    	this.rear =0;
    	this.capacity = capacity;
    	this.currentSize = 0;
    	this.q = new ArrayList<V>(capacity);
    	for(int i=0;i<capacity;i++){
    		q.add(null);
    	}
    }

    public int size() {
    	return currentSize;
    }

    public boolean isEmpty() {
    	if(currentSize==0){
    		return true;
    	}
    	return false;
    }
	
    public boolean isFull() {
    	if(currentSize == capacity){
    		return true;
    	}
    	return false;
    }
    
   
    public void push(V node) {
	    if(this.isFull()==false){
	    	if(currentSize == 0){
	    		this.q.set(front, node);
	    	}else{
			    this.q.set((rear+1)%capacity, node);
			    this.rear = (rear+1)%capacity;
	    	}
	    	this.currentSize++;
	    }
    }

    public V pop() {
    	if(this.isEmpty() == true){
    		return null;
    	}
    	if(currentSize==1){
    		V b = q.get(front);
    		q.set(front, null);
    		currentSize--;
    		rear = front;
    		return b;
    	}
    	else{
	    	V b = q.get(front);
	    	this.q.set(front, null);
	    	this.front = (front+1)%capacity;
	    	this.currentSize--;
	    	return b;
    	}
    }

    public void assemble(){
        List<V> l = new ArrayList<>();
        while(this.isEmpty() == false){
            l.add(this.pop());
        }
        Collections.sort(l);
        for(int i=0;i<l.size();i++){
            this.push(l.get(i));
        }
        
    }



}

