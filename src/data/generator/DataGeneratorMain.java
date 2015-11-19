package data.generator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DataGeneratorMain extends Thread {

	private List<GeneratorConnection> generatorConnections;
	private ServerSocket serverSocket;
	private long sendRate; // in milliseconds
	private int port;
	private List<String> wordList;
	private final int WORD_LIMIT = 1000;
	
	public DataGeneratorMain(int port, long sendRate) throws IOException{
		this.sendRate = sendRate;
		this.port = port;
		generatorConnections = new ArrayList<GeneratorConnection>();
		// read file words.txt to get 1000 words
		wordList = Files.readAllLines(Paths.get("words.txt"), Charset.defaultCharset());
		if(wordList.isEmpty()) throw new IOException("words.txt might have something wrong!");
		serverSocket = new ServerSocket(this.port);
		System.out.println("Data Generator in port " + this.port + " ready to accept");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			try {
				Socket clientSocket = serverSocket.accept();
				GeneratorConnection newConnection = new GeneratorConnection(wordList, clientSocket, sendRate, WORD_LIMIT);
				synchronized(generatorConnections){ // works like magic!
					generatorConnections.add(newConnection);
				}
				newConnection.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void checkConnections(){
		// prevent concurrent exception
		synchronized(generatorConnections){
			Iterator<GeneratorConnection> iter = generatorConnections.iterator();

			while (iter.hasNext()) {
			    GeneratorConnection g = iter.next();
			    if (g.isFinished()){
			    	Socket clientSocket = g.getClientSocket();
			    	System.out.println("Generator to " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " has finished!");
			    	System.out.println(g.getReason());
			        iter.remove();
			    }
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			DataGeneratorMain dataGeneratorMain = new DataGeneratorMain(4221, 1000);
			dataGeneratorMain.start();
			while(true){
				dataGeneratorMain.checkConnections();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
