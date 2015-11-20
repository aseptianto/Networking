package data.collector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class GeneratorReceiver extends Thread {

	private List<GeneratorHandle> generatorHandles; // must handle concurrent things too
	private boolean shutdown;
	
	public GeneratorReceiver(){
		generatorHandles = new ArrayList<GeneratorHandle>();
		shutdown = false;
	}
	
	public void shutDown(){
		shutdown = true;
	}
	
	public String showConnections(){
		String result = "";
		int index = 0;
		synchronized(generatorHandles){
			for(GeneratorHandle g : generatorHandles){
				Socket s = g.getSocket();
				result += index + ": " + s.getInetAddress().getHostAddress() + ":" + s.getPort();
				result += "\n";
				index++;
			}
		}
		return result;
	}
	
	public synchronized void addWord(String word){
		word += "\n"; // add new line
		File file = new File("storedWords.txt");
		try {
			// lock
			FileOutputStream out = new FileOutputStream(file, true);
			java.nio.channels.FileLock lock = out.getChannel().lock();
			try{
				out.write(word.getBytes());
			} finally{
				lock.release();
				out.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void addGeneratorHandle(String host, int port){
		synchronized(generatorHandles){
			try {
				System.out.println("Creating connection to " + host + ":" + port);
				GeneratorHandle handle = new GeneratorHandle(host, port, GeneratorReceiver.this);
				generatorHandles.add(handle);
				handle.start();
				System.out.println("New handle created!");
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				System.out.println("Host or port can't be reached!");
			} catch (ConnectException e){
				System.out.println("Connection refused!");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void closeHandle(int index){
		if(generatorHandles.isEmpty()) return;
		synchronized(generatorHandles){
			Iterator<GeneratorHandle> iter = generatorHandles.iterator();
			GeneratorHandle g = iter.next();
			for(int i = 1; i < index; i++){
				g = iter.next();
			}
			try {
				g.shutDownHandle();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Removing index " + index);
			iter.remove();
		}
	}
	
	public void closeAllHandles(){
		if(generatorHandles.isEmpty()) return;
		synchronized(generatorHandles){
			Iterator<GeneratorHandle> iter = generatorHandles.iterator();
			while(iter.hasNext()){
				GeneratorHandle g = iter.next();
				try {
					g.shutDownHandle();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				iter.remove();
			}
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//monitor interrupted handles
		while(!shutdown){
			if(generatorHandles.isEmpty()){
				// some anomaly happens if only continue, sleep..
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// do nothing
				}
				continue;
			}
			synchronized(generatorHandles){
				Iterator<GeneratorHandle> iter = generatorHandles.iterator();
				int index = 0;
				while (iter.hasNext()) {
				    GeneratorHandle g = iter.next();
				    if (g.isFinished()){
				    	System.out.println("Handle index " + index + " is closed by collector!");
				        iter.remove();
				    }
				    index++;
				}
			}
		}
		closeAllHandles();
	}

}
