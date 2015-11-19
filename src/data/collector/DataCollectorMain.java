package data.collector;

import java.util.Scanner;

public class DataCollectorMain extends Thread{

	private GeneratorReceiver generatorReceiver;
	private StatisticReceiver statisticReceiver;
	
	public DataCollectorMain(int port) throws Exception{
		generatorReceiver = new GeneratorReceiver();
		statisticReceiver = new StatisticReceiver(port);
		generatorReceiver.setName("generatorReceiver");
		generatorReceiver.start();
		System.out.println("GeneratorReceiver Started");
		statisticReceiver.setName("statisticReceiver");
		statisticReceiver.start();
		System.out.println("StatisticReceiver Started");
	}
	
	@Override
	public void run(){
		Scanner s = new Scanner(System.in);
		String rawInput = "";
		while(!rawInput.toLowerCase().equals("exit")){
			rawInput = s.nextLine();
			rawInput = rawInput.toLowerCase();
			String[] inputs = rawInput.split(" ");
			switch(inputs[0]){
			case "add":
				if(inputs.length != 3){
					System.out.println("Invalid Command (add <host> <port>)");
				}
				else{
					try{
						int port = Integer.parseInt(inputs[2]);
						generatorReceiver.addGeneratorHandle(inputs[1], port);
					}
					catch(Exception e){
						System.out.println("Port is not integer?");
					}
				}
				break;
			case "show":
				System.out.println(generatorReceiver.showConnections());
				break;
			case "close":
				if(inputs.length != 2){
					System.out.println("Invalid Command (close <index>)");
				}
				else{
					try{
						int index = Integer.parseInt(inputs[1]);
						generatorReceiver.closeHandle(index);
					}
					catch(Exception e){
						System.out.println("Index is not integer?");
					}
				}
				break;
			case "exit":
				break;
			case "help":
				System.out.println("\nCommands:\n"
						+ "add <host> <port> -> connects to a DataGenerator\n"
						+ "show -> shows active connections to Data Generators\n"
						+ "close <index> -> closes a socket given the index number from 'show' command\n"
						+ "exit -> shuts down the program\n\n");
				break;
			default:
				System.out.println("Unknown command. Type 'help' to see commands");
			}
		}
		shutDown();
	}
	
	public void shutDown(){
		generatorReceiver.shutDown();
		statisticReceiver.shutDown();
	}
	
	public static void main(String[] args) {
		try{
			DataCollectorMain d = new DataCollectorMain(4214);
			d.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
