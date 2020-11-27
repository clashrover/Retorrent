import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.io.*;
import java.security.MessageDigest;
import java.lang.Math;
import java.net.Socket;

public class download {
    public static void main(String argv[]) throws Exception
    {
        
        ArrayList<String>Servers = new ArrayList<>();
        ArrayList<String>Filename = new ArrayList<>();
        ArrayList<Integer> threads = new ArrayList<>();
        String csvFile = argv[0];
        BufferedReader br = null;
        String line = "";
        int total_threads=0;
        br = new BufferedReader(new FileReader(csvFile));
        // Parse the csv file to get server and threads.
        while ((line = br.readLine()) != null) {

            // use comma as separator
            String[] sp = line.split(",");
            total_threads += Integer.parseInt(sp[1]);
            threads.add(Integer.parseInt(sp[1]));
            int x=0;
            while(sp[0].charAt(x)!='/'){
                x++;
            }
            x+=2;
            int y=x;
            while(sp[0].charAt(y)!='/'){
                y++;
            }
            String serv = sp[0].substring(x,y);
            String f = sp[0].substring(y);
            Servers.add(serv);
            Filename.add(f);
        }
        br.close();
        
        int size = 0;
        while(size==0){
            try{
                size = getSize(Servers.get(0),Filename.get(0)); //bytes
                // send hhtp header request. Parse the header and get file size.
                
            }catch(Exception e){
                System.out.println(e);
            }
        }

        System.out.println("Download Size :" + size+"B");
        
        int chunk_size = 16384; //16kB
        
        int num_chunks=(int)Math.ceil((size*1.0)/(chunk_size*1.0));

        Lock readLock = new ReentrantLock();
        Lock writeLock = new ReentrantLock();
        // int burst = 15;

        ArrayList<PairInt> reqQueue = new ArrayList<PairInt>();

        ArrayList<Pair> storeQueue = new ArrayList<Pair>();
        
        for(int i=0;i<num_chunks;i++){
            int x=i*chunk_size;
            int y=x+chunk_size-1;
            if(y>=size){
                y=size-1;
                PairInt p = new PairInt(x,y);
                reqQueue.add(p);
                break;
            }
            PairInt p = new PairInt(x,y);
            reqQueue.add(p);
        }
        int ci=0;
        // start all the threads with parameters calculated from total file size obtained.
        Thread[] t = new Thread[total_threads];
        for(int i=0;i<Servers.size();i++){
            for(int j=0;j<threads.get(i);j++){
                client c = new client(Servers.get(i),Filename.get(i),ci,reqQueue,storeQueue,chunk_size, readLock, writeLock,ci);
                
                t[ci] = new Thread(c);
                t[ci].start();
                ci++;
            }
        }

        for(int i=0;i<t.length;i++){
            t[i].join();
        }

        Collections.sort(storeQueue);
        
        System.out.println("No of Threads : " + total_threads);
        System.out.println("Chunk size : " + chunk_size/1024 + "KB");
        
        // merge the sorted chunks into single byte array and store in file.
        // i have printed into file in chunks. So if filename already exists
        // it appends byte to at the end.
        byte[] b = new byte[size];
        int k=0;
        String[] f = Filename.get(0).split("/");
        String filename = f[f.length-1];
        System.out.println("Saving file as : "+ filename);
        for(int i=0;i<storeQueue.size();i++){
            try { 
                File myObj = new File(filename);
                if (myObj.createNewFile()) {
                    BufferedWriter out = new BufferedWriter(new FileWriter(filename, true)); 
                    Pair p = storeQueue.get(i);
                    String ss="";
                    for(int j=0;j<p.b.length;j++){
                        b[k] = p.b[j];
                        ss+= (char)b[k];
                        k++;
                    }
                    out.write(ss);
                    out.close(); 
                } else {
                    BufferedWriter out = new BufferedWriter(new FileWriter(filename, true)); 
                    Pair p = storeQueue.get(i);
                    String ss ="";
                    for(int j=0;j<p.b.length;j++){
                        b[k] = p.b[j];
                        ss+= (char)b[k];
                        k++;
                    } 
                    out.write(ss);
                    out.close(); 
                }
    
            } 
            catch (IOException e) { 
                System.out.println("exception occoured" + e); 
            }
            
        }
        System.out.println("Byte Recieved: " + k); // to check bytes recieved
        // now check the hash of byte
        checkHashValue(b);
        


    }

    public static void checkHashValue(byte[] b){
        try{
            String digest = null; 
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(b); //converting byte array to Hexadecimal String 
            StringBuilder sb = new StringBuilder(2*hash.length); 
            for(byte bc : hash){ 
                sb.append(String.format("%02x", bc&0xff)); 
            } 
            digest = sb.toString();
            System.out.println("Hash Value : " +digest);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static int getSize(String source, String fname){
        int data_size=0;
        try{
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
            
                
            sentence = String.format("HEAD %s HTTP/1.1\r\nHost: %s\r\nConnection: keep-alive\r\n\r\n", fname,source);
            outToServer.writeBytes(sentence);
            outToServer.flush();
            int i=0;
            String line ="";
            while((i=inFromServer.read()) !=-1){
                char c = (char)i;
                line+=c;
                // System.out.print(c);
                if(c=='\n'){
                    if(line.contains("Content-Length")){
                        int x=0;
                        while(line.charAt(x)<'0' || line.charAt(x)>'9'){
                            x++;
                        }
                        int y=x;
                        while(line.charAt(y)>='0' && line.charAt(y)<='9'){
                            y++;
                        }
                        data_size = Integer.parseInt(line.substring(x,y));
                        break;
                    }
                    line="";
                }
            }
            clientSocket.close();
            return data_size;
        }catch(Exception e){
            System.out.println(e);
            return data_size;
        }
    
    }
    
}