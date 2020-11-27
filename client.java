import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.*;
import java.io.File;  // Import the File class
import java.io.IOException;
import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



class client implements Runnable{
    int client_id;
    ArrayList<PairInt> reqQueue;
    ArrayList<Pair> storeQueue;
    int chunk_size;
    Lock readLock;
    Lock writeLock;
    int id;
    ArrayList<PairInt> currentSave ;
    String source;
    String filename;
    ArrayList<Double> time;
    ArrayList<Integer> down;
    Long startTime;
    int d_sum;
    public client(String source,String filename,int i, ArrayList<PairInt> reqQueue, ArrayList<Pair> storeQueue, int chunk_size, Lock readLock, Lock writeLock, int id){
        this.client_id = i;
        this.chunk_size = chunk_size;
        this.readLock = readLock;
        this.writeLock = writeLock;
        this.reqQueue = reqQueue;
        this.storeQueue = storeQueue;
        this.id = id;
        this.currentSave = new ArrayList<>();
        this.source =source;
        this.filename = filename;
        this.time = new ArrayList<>();
        this.down = new ArrayList<>();
        this.down.add(0);
        this.startTime = System.nanoTime();
        this.time.add(0.0);
        this.d_sum = 0;
    }

    public void run(){
        while(true){
            readLock.lock();
            if(reqQueue.isEmpty()==true){
                readLock.unlock();
                break;
            }
            readLock.unlock();
            try{
                createTCP();
            }catch(Exception e){
                System.out.println(e);
                System.out.println("Req Queue Size: "+reqQueue.size());
                while(currentSave.isEmpty() == false){
                    PairInt r = currentSave.get(0);
                    currentSave.remove(0);
                    readLock.lock();
                    reqQueue.add(r);
                    readLock.unlock();
                }
            }
        }
    }

    public void createTCP() throws Exception
    {
        String sentence;

        BufferedReader inFromUser = 
            new BufferedReader(new InputStreamReader((System.in)));

        Socket clientSocket = new Socket(source, 80);
        // clientSocket.setKeepAlive(true);
        clientSocket.setSoTimeout(5000);
        DataOutputStream outToServer = 
            new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer = 
            new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
        // ArrayList<Double> time = new ArrayList<>();
        // ArrayList<Integer> down = new ArrayList<>();
        // int d_sum=0;
        // down.add(0);
        // Long startTime = System.nanoTime();
        // time.add(0.0);
        int req=0;
        while(req<100){
            ArrayList<PairInt> burst = new ArrayList<>();
            readLock.lock();
            if (reqQueue.isEmpty() == true){
                readLock.unlock();
                break;
            }
            int lp=0;
            while(lp<5 && reqQueue.isEmpty()==false){
                PairInt range = reqQueue.get(0);
                reqQueue.remove(0);
                currentSave.add(range);
                burst.add(range);
                lp++;
            }
            
            readLock.unlock();

            int c_size = 0;
            lp=0;
            while(lp<burst.size()){
                PairInt range = burst.get(lp);
                sentence = String.format("GET %s HTTP/1.1\r\nHost: %s\r\nConnection: keep-alive\r\nRange: bytes=%d-%d\r\n\r\n", filename,source,
                    range.a, range.b);
            
                req++;
                outToServer.writeBytes(sentence);
                outToServer.flush();
                c_size += (range.b-range.a+1);
                lp++;
            }
            
            
            int i=0;
            int n=0;
            int k=0;
            d_sum+=c_size;
            String line="";
            int state=0;
            lp=0;
            while((i=inFromServer.read()) !=-1 && k<c_size){
                
                if (state==1){
                    PairInt range = burst.get(lp);
                    int cc = (range.b-range.a)+1;
                    byte[] data = new byte[cc];
                    int j=0;
                    while(true){
                        data[j] = (byte)i;
                        k++;
                        j++;
                        if(k==c_size){
                            break;
                        }
                        if (j==cc){
                            i=inFromServer.read();
                            break;
                        }
                        if((i=inFromServer.read()) ==-1){
                            break;
                        }
                    }
                    Pair p = new Pair(range, data);
                    writeLock.lock();
                    storeQueue.add(p);
                    writeLock.unlock();
                    currentSave.remove(0);
                    n=0;
                    state=0;
                    lp++;
                }
                if(k==c_size){
                    break;
                }
                if(i==-1){
                    break;
                }
                char c = (char)i;
                // System.out.print(c);
                line+=c;
                if(c=='\n'){
                    if(line.compareTo("\r\n")==0){
                        state=1;
                    }
                    line="";
                    n++;
                }
            }
            down.add(d_sum);
            time.add(((System.nanoTime()-startTime)*1.0)/1000000000.0);
            
        }

        clientSocket.close();

        readLock.lock();
        if (reqQueue.isEmpty() == false){
            readLock.unlock();
            createTCP();
            return;
        }
        readLock.unlock();
        // writeTimeData(time, down);
    }

    public void writeTimeData(ArrayList<Double> time, ArrayList<Integer> down){
        String filename = String.format("%d.txt", id);
        for(int i=0;i<down.size();i++){
            String s = String.format("%f %d\n", time.get(i),down.get(i));
            try { 
                File myObj = new File(filename);
                if (myObj.createNewFile()) {
                    BufferedWriter out = new BufferedWriter( 

                       new FileWriter(filename, true)); 
                    out.write(s);
                    out.close(); 
                } else {
                    BufferedWriter out = new BufferedWriter( 
                       new FileWriter(filename, true)); 
                    out.write(s); 
                    out.close(); 
                }
                // Open given file in append mode. 
    
            } 
            catch (IOException e) { 
                System.out.println("exception occoured" + e); 
            }
        }
    }
}