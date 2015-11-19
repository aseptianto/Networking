package data.collector;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class GeneratorHandle extends Thread {

	private Socket clientSocket;
	private DataInputStream in;
	private GeneratorReceiver parent;
	private boolean finished;
	
	public GeneratorHandle(String host, int port, GeneratorReceiver parent) throws UnknownHostException, IOException{
		clientSocket = new Socket(host, port);
		in = new DataInputStream(clientSocket.getInputStream());
		this.parent = parent;
		finished = false;
	}
	
	public boolean isFinished(){
		return finished;
	}
	
	public Socket getSocket(){
		return clientSocket;
	}
	
	public void shutDownHandle() throws IOException{
		in.close();
		clientSocket.close();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!finished){
			try {
				String word = in.readUTF();
				parent.addWord(word);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// assume server closes connection
				break;
			}
		}
		finished = true;
	}

}
