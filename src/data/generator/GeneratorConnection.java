package data.generator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GeneratorConnection extends Thread {

	private List<String> wordList;
	private Socket clientSocket;
	private DataOutputStream out;
	private long sendRate;
	private int wordsLimit;
	private int sentWords;
	private String reason = "";
	
	public GeneratorConnection(List<String> wordList, Socket clientSocket,
			long sendRate, int wordsLimit) throws IOException {
		// TODO Auto-generated constructor stub
		this.wordList = wordList;
		this.clientSocket = clientSocket;
		this.sendRate = sendRate;
		sentWords = 0;
		this.wordsLimit = wordsLimit;
		out = new DataOutputStream(clientSocket.getOutputStream());
	}
	
	public String getReason(){
		return reason;
	}
	
	public boolean isFinished(){
		return clientSocket.isClosed();
	}
	
	public Socket getClientSocket(){
		return clientSocket;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(sentWords < wordsLimit){
			try{
				// get a word
				int randomIndex = ThreadLocalRandom.current().nextInt(0, 1000);
				String wordToSend = wordList.get(randomIndex);
				//send a word
				out.writeUTF(wordToSend);
				sentWords++;
				// sleep..
				Thread.sleep(sendRate);
			}
			catch(IOException e){
				reason = "Most likely client closes it.";
				break;
			}
			catch(Exception e){
				e.printStackTrace();
				reason = "No idea why. Read StackTrace..";
			}
		}
		if(sentWords == wordsLimit) reason = wordsLimit + " words reached!";
		try {
			out.close();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
