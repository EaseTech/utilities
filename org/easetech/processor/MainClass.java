package org.easetech.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class MainClass {
	
	

	public static void main(String[] args) {
		long startTime = System.nanoTime();
		
		
		Option valueSeparator   = Option.builder("delimiter").argName("delimiter")
				.required(false)
				.hasArg()
				.desc(  "OPTIONAL.Value delimiter. Defaults to ; <SEMI-COLON>. Double quotes should be used around the delimeter value." )
				.build();
		
		Option fieldsToHash   = Option.builder("fieldsToHash").argName("fieldsToHash")
				.required(false)
				.hasArg()
				.desc(  "OPTIONAL.The comma separated list of fields to hash.If not specified then default to hashing values for parameters that ends with SSID, MACAddress, SerialNumber. " )
				.build();
		
		Option hashingAlgo   = Option.builder("hashingAlgo").argName("hashingAlgo")
				.required(false)
				.hasArg()
				.desc(  "OPTIONAL.The hashing algo to use.If not specified then default to SHA-256. Currently it supports MD5, SHA-1, SHA-256 " )
				.build();
		
		Option inputBufferSize   = Option.builder("inputBufferSize").argName("inputBufferSize")
				.required(false)
				.hasArg()
				.desc(  "OPTIONAL.The input buffer size, as integer value, to read the portion of file in memory. If not specified, defaults to 10000 chars" )
				.build();
		
		Option help = new Option( "help", "OPTIONAL.print this message" );
		
		Option displayTimings = new Option( "displayTimings", "OPTIONAL.Display information about time taken by script in nano- and milli- seconds" );
		
		Option verbose = new Option( "verbose", "OPTIONAL.Display execution information" );
		Option dryRun = new Option( "dryRun", "OPTIONAL.Do a dry run of the process before actually executing it." );
		
		Option inputDataFile   = Option.builder("inputFilePath").argName("inputFilePath")
				.required(true)
				.hasArg()
				.desc(  "REQUIRED.The complete path of the input file containing data" )
				.build();
		
		Option dateTimeFormat   = Option.builder("dateFormat").argName("dateFormat")
				.required(false)
				.hasArg()
				.desc(  "OPTIONAL.The date time format used to tag the output and mapping files. If not specified, then defaults to yyyyMMddhhmmss" )
				.build();
		Options options = new Options();
		options.addOption(valueSeparator)
			   .addOption(inputDataFile)
			   .addOption(fieldsToHash)
			   .addOption(hashingAlgo)
			   .addOption(inputBufferSize)
			   .addOption(help)
			   .addOption(displayTimings)
			   .addOption(verbose)
			   .addOption(dryRun).
			   addOption(dateTimeFormat);
		
		// create the parser
	    CommandLineParser parser = new DefaultParser();
	    
	    String inputFilePath = null;
	    String valSeparator = null;
	    String fieldsListToHash = null;
	    String hashingAlgor = null;
	    String inputBuffer = null;
	    String helpString = "The Data Processor utility reads a file line by line, to find fields to be hashed and then hash the values using SHA-256 hashing algorithm. "
	    		+ "The fileds to be hashed can be provided by the user using the fieldsToHash command line argument."
	    		+ "User needs to specify the complete path of the input file that needs to be read and whose certain fields need to be hashed."
	    		+ "The Data Processor utility produces two output files:An output file with the same format as the input file except that certain fields have hashed values. "
	    		+ "The other file is the mapping file containing the original value to Hashed value mapping. Both the output and mapping file are suffixed with a date time of the format : yyyyMMddhhmmss";

	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        if(line.hasOption("help")) {
	        	HelpFormatter formatter = new HelpFormatter();
		    	formatter.printHelp( helpString, options );
		    	System.exit(0);
	        }
	        if( line.hasOption( "inputFilePath" ) ) {
	            inputFilePath = line.getOptionValue("inputFilePath");
	        } else {
	        	HelpFormatter formatter = new HelpFormatter();
		    	formatter.printHelp( helpString , options );
	        }
	        if(line.hasOption("delimiter")) {
	        	valSeparator = line.getOptionValue("delimiter");
	        }
	        if(line.hasOption("fieldsToHash")) {
	        	fieldsListToHash = line.getOptionValue("fieldsToHash");
	        }
	        if(line.hasOption("hashingAlgo")) {
	        	hashingAlgor = line.getOptionValue("hashingAlgo");
	        }
	        if(line.hasOption("inputBufferSize")) {
	        	inputBuffer = line.getOptionValue("inputBufferSize");
	        }
	        
	        List<String> toHash = new ArrayList<>();
	        if(fieldsListToHash != null) {
	        	String[] fields = fieldsListToHash.split(",");
	        	for(String val : fields) {
	        		toHash.add(val);
	        	}
	        } else {
	        	toHash.add("SSID");
	        	toHash.add("SerialNumber");
	        	toHash.add("MACAddress");
	        }
	        
	        DataProcessor dataProcessor = new DataProcessor(toHash);
	        //DataProcessor dataProcessor = new DataProcessor(toHash);
	        if(valSeparator != null) {
	        	dataProcessor.setDelimeter(valSeparator);
	        }
	        if(inputBuffer != null) {
	        	dataProcessor.setInputBuffer(Integer.getInteger(inputBuffer));
	        }
	        if(line.hasOption("dateFormat")) {
	        	dataProcessor.setDateFormat(line.getOptionValue("dateFormat"));
	        }
	        if(hashingAlgor != null) {
	        	dataProcessor.setHashingAlgo(hashingAlgor);
	        }
	        
	        dataProcessor.setFilePath(inputFilePath);
	        if(line.hasOption("verbose")) {
	        	System.out.println("Following values are used to process the file using the DataProcessor class:");
	        	System.out.println("File Path = " + dataProcessor.getFilePath());
	        	System.out.println("Delimiter = " + dataProcessor.getDelimeter());
	        	System.out.println("Input Buffer Size = " + dataProcessor.getInputBuffer());
	        	System.out.println("Hashing Algo = " + dataProcessor.getHashingAlgo());
	        	System.out.println("Fields To hash = " + dataProcessor.getKeysToHash());
	        	dataProcessor.setVerbose(true);
	        }
	        if(line.hasOption("dryRun")) {
	        	dataProcessor.dryRun();
	        } else {
	        	dataProcessor.processFile();
	        }
	        
	        if(line.hasOption("displayTimings")) {
	        	System.out.println("Total time taken in nano seconds is : " + (System.nanoTime() - startTime));
			    System.out.println("Total time taken in millisecond  is : " + (System.nanoTime() - startTime)/1000000);
			    System.out.println("Total time taken in second  is : " + (System.nanoTime() - startTime)/1000000000);
	        }
	        
	    }
	    catch( ParseException exp ) {
	    	System.out.println("Required options were missing from the command");
	    	HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp( helpString, options );
	    	System.exit(0);
	    }
	    
	    
	}

}
