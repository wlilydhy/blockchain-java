package one.wangwei.blockchain.pb;

import one.wangwei.blockchain.pb.Strategy.HelloWorld;
import one.wangwei.blockchain.pb.Strategy.SendBlock;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import org.apache.commons.cli.*;
import one.wangwei.blockchain.pb.client.ClientManager;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Client main. Parse command line options and provide default values.
 * 

 * @see {@link one.wangwei.blockchain.pb.Utils}
 * @author aaron
 *
 */
public class Client  {
	private static Logger log = Logger.getLogger(Client.class.getName());
	private static int port=Utils.serverPort; // default port number for the server
	private static String host=Utils.serverHost; // default host for the server
	
	private static void help(Options options){
		String header = "one.wangwei.blockchain.pb Client for Unimelb COMP90015\n\n";
		String footer = "\ncontact aharwood@unimelb.edu.au for issues.";
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("one.wangwei.blockchain.pb.Client", header, options, footer, true);
		System.exit(-1);
	}
	
	public static void main( String[] args ) throws IOException, InvalidMessage {
    	// set a nice log format
		System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tl:%1$tM:%1$tS:%1$tL] %2$s %4$s: %5$s%n");
        
    	// parse command line options
        Options options = new Options();
        options.addOption("port",true,"server port, an integer");
        options.addOption("host",true,"hostname, a string");
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
			cmd = parser.parse( options, args);
		} catch (ParseException e1) {
			help(options);
		}
        
        if(cmd.hasOption("port")){
        	try{
        		port = Integer.parseInt(cmd.getOptionValue("port"));
			} catch (NumberFormatException e){
				System.out.println("-port requires a port number, parsed: "+cmd.getOptionValue("port"));
				help(options);
			}
        }
        
        if(cmd.hasOption("host")) {
        	host = cmd.getOptionValue("host");
        }
        
        // start up the client
        log.info("one.wangwei.blockchain.pb Client starting up");
        
        // the client manager will make a connection with the server
        // and the connection will use a thread that prevents the JVM
        // from terminating immediately
		//System.out.println("host"+host+"  port"+port);
        new ClientManager(host,9999, SendBlock.strategyName);




    }
}
