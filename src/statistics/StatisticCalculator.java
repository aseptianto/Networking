package statistics;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class StatisticCalculator {

	private Socket socket;
	private DataInputStream in;
	private List<String> wordList;
	
	public StatisticCalculator(){
		wordList = new ArrayList<String>();
	}
	
	public void getWordList(String host, int port) throws IOException, UnknownHostException, ConnectException{
		socket = new Socket(host, port);
		wordList.clear();
		in = new DataInputStream(socket.getInputStream());
		while(true){
			String input = in.readUTF();
			if(input.equals("!{EOF}")) break;
			wordList.add(input);
		}
		in.close();
		socket.close();
	}
	
	public void doStatistics(){
		Map<String, Integer> uniqueWords = new HashMap<String, Integer>();
		int count;
		int biggestCount = 1;
		String mostCommonWord = "";
		long totalLetters = 0;
		int shortestWordLength = Integer.MAX_VALUE;
		int longestWordLength = 0;
		for(String w : wordList){ // do all things in one loop
			if(uniqueWords.containsKey(w)){
				// find most common word
				count = (uniqueWords.get(w) + 1);
				if(count > biggestCount){
					biggestCount = count;
					mostCommonWord = w;
				}
				uniqueWords.replace(w, count);
			}
			else{
				// add unique words
				uniqueWords.put(w, 1);
			}
			// min max avg
			totalLetters += w.length();
			if(w.length() < shortestWordLength)
				shortestWordLength = w.length();
			if(w.length() > longestWordLength)
				longestWordLength = w.length();
		}
		double average = totalLetters / (double) wordList.size();
		System.out.println("There are " + uniqueWords.size() + " unique words from a total of " + wordList.size() + " words.");
		if(!mostCommonWord.equals("")){
			System.out.println("The most common word is '" + mostCommonWord + "' with " + biggestCount + " occurences.");
		}
		else {
			System.out.println("All words are apparently unique, no common word at all.");
		}
		System.out.println("Word length statistics:");
		System.out.println("Min: " + shortestWordLength);
		System.out.println("Max: " + longestWordLength);
		System.out.println("Avg: " + average);
	}
	
	public void beginCLI(){
		Scanner s = new Scanner(System.in);
		String rawInput = "";
		while(!rawInput.toLowerCase().equals("exit")){
			System.out.print("RSCLI: ");
			rawInput = s.nextLine();
			String[] inputs = rawInput.toLowerCase().split(" ");
			switch(inputs[0]){
			case "get":
				if(inputs.length != 3){
					System.out.println("Usage: get <host> <port>");
				}
				else{
					try{
						int port = Integer.parseInt(inputs[2]);
						getWordList(inputs[1], port);
						
					} catch(NumberFormatException e){
						System.out.println("Port is not integer?");
					} catch(UnknownHostException e){
						System.out.println("Host unknown!");
					} catch(ConnectException e){
						System.out.println("Connection refused!");
					} catch(IOException e){
						System.out.println("Socket disconnected!");
					}
				}
				break;
			case "statistics":
				doStatistics();
				break;
			case "help":
				System.out.println("RSCLI (Really Simple Command Line Interface) V1.0\nCommands:\n"
						+ "get <host> <port> -> connects to a DataCollector and collects the stored words\n"
						+ "WARNING: get command will remove previous collected words from prior get commands\n"
						+ "statistics -> shows statistics of the word gathered\n"
						+ "exit -> shuts down the program\n");
				break;
			case "exit":
				break;
			default:
				System.out.println("Invalid command - see 'help' for information");
			}
		}
		s.close();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StatisticCalculator s = new StatisticCalculator();
		s.beginCLI();
	}

}
