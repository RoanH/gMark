package dev.roanh.gmark.cli.client;

import org.apache.commons.cli.CommandLine;

import dev.roanh.gmark.cli.CommandLineClient;
import dev.roanh.gmark.cli.InputException;

public class IndexClient extends CommandLineClient{
	/**
	 * Instance of this client.
	 */
	public static final IndexClient INSTANCE = new IndexClient();
	
	protected IndexClient(){
		super(null, null);
		// TODO Auto-generated constructor stub
	}
	
	//TODO recurse and have a bisim and cpqnative index client? or allow multi-arg prefixes and register both at the top level?

	@Override
	protected void handleInput(CommandLine cli) throws InputException{
		// TODO Auto-generated method stub
		
	}

}
