package statistics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
	private DataOutputStream out;
	//private List<String> wordList;
	private Map<String, List<String>> generatorWordList;
	
	public StatisticCalculator(){
		//wordList = new ArrayList<String>();
		generatorWordList = new HashMap<String, List<String>>();
	}
	
	public void getWordList(String host, int port, String generatorName) throws IOException, UnknownHostException, ConnectException{
		socket = new Socket(host, port);
		List<String> wordList = new ArrayList<String>();
		//wordList.clear();
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		out.writeUTF(generatorName);
		String result = in.readUTF();
		if(result.equals("1")){
			while(true){
				String input = in.readUTF();
				if(input.equals("!{EOF}")) break;
				wordList.add(input);
			}
			generatorWordList.put(generatorName, wordList);
		}
		else{
			System.out.println("Requested generator not available!");
		}
		in.close();
		socket.close();
	}
	
	public void doStatistics(String generatorKey){
		List<String> wordList = new ArrayList<String>();
		if(generatorKey.equals("all")){
			wordList = new ArrayList<String>();
			for(String key : generatorWordList.keySet()){
				wordList.addAll(generatorWordList.get(key));
			}
		}
		else{
			wordList = generatorWordList.get(generatorKey);	
		}
		if(wordList == null){
			System.out.println(generatorKey + " is not in the list!");
			return;
		}
		else if(wordList.isEmpty()){
			System.out.println("There is no word collected from the generator/all generators");
			return;
		}
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
				uniqueWords.put(w, count);
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
		if(generatorKey.equals("all")){
			System.out.println("Statistics for all gathered generators");
		}
		else{
			System.out.println("Statistics for generator in " + generatorKey);
		}
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
	
	public void listGenerators(){
		if(generatorWordList.isEmpty()){
			System.out.println("No saved generator statistics at the moment");
			return;
		}
		System.out.println("Saved generators:");
		for(String s : generatorWordList.keySet()){
			System.out.println(s);
		}
		System.out.println();
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
				if(inputs.length != 4){
					System.out.println("Usage: get <host> <port> <generatorIP:generatorPort>");
				}
				else{
					try{
						int port = Integer.parseInt(inputs[2]);
						getWordList(inputs[1], port, inputs[3]);
						
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
				if(inputs.length != 2){
					System.out.println("Usage: statistics <generatorIP:generatorPort>");
				}
				else{
					String generatorKey = inputs[1];
					doStatistics(generatorKey);
				}
				break;
			case "help":
				System.out.println("RSCLI (Really Simple Command Line Interface) V1.0\nCommands:\n"
						+ "get <host> <port> <generatorIP:generatorPort> -> connects to a DataCollector and collects the stored words\n"
						+ "'get localhost 4500 127.0.0.1:4221' connects collector in localhost:4500 and requests statistics from generator in 127.0.0.1:4221\n"
						+ "WARNING: get command will remove previous collected words from prior get commands to the same generators\n"
						+ "statistics <generatorIP:generatorPort> -> shows statistics of the word gathered\n"
						+ "exit -> shuts down the program\n");
				break;
			case "list":
				listGenerators();
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
