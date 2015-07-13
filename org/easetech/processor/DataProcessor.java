package org.easetech.processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple class that reads the data from a file, specified as parameter to
 * {@link #processFile(String)} method, and then hash the relevant values of the
 * field as identified by the {@link #getKeysToHash()}.
 * 
 * @author anuj
 *
 */
public class DataProcessor {
	
	/**
	 * Boolean indicating whether to print execution information or not
	 */
	private Boolean verbose = false;

	/**
	 * The list of keys for which the values need to be hashed.
	 * 
	 */
	private final List<String> keysToHash;

	/**
	 * The Algorithm to use for hashing. If not specified,
	 * {@link #DEFAULT_HASHING_ALGO} will be used
	 */
	private String hashingAlgo =DEFAULT_HASHING_ALGO;


	/**
	 * The Default Hashing Algo to use if none is specified
	 */
	private static final String DEFAULT_HASHING_ALGO = "SHA-256";

	/**
	 * The value delimiter {@link #DEFAULT_DELIMITER} will be used if none is
	 * specified
	 */
	private String delimeter;
	
	/**
	 * Boolean identifying whether the hashed value should be surrounded with quotes or not.
	 * It will depend on whether the original value had quotes around it or not.
	 */
	private boolean surroundWithQuotes = false;
	
	/**
	 * Double quotes
	 */
	private static final String DOUBLE_QUOTE_SYMBOL = "\"";

	public void setDelimeter(String delimeter) {
		this.delimeter = delimeter;
	}


	/**
	 * The Default Delimiter to use if none is specified
	 */
	private static final String DEFAULT_DELIMITER = ";";

	/**
	 * The buffer for reading the file. If not specified,
	 * {@link #DEFAULT_BUFFER_SIZE} will be used
	 */
	private Integer inputBuffer;

	/**
	 * Default buffer size if none is specified
	 */
	private static int DEFAULT_BUFFER_SIZE = 10000;

	/**
	 * Path to the input file. Mandatory field
	 */
	private String inputFilePath;

	/**
	 * Path to the output file. Currently "-output" is appended to the file
	 * path.
	 */
	private String outputFilePath;
	
	/**
	 * The date format to use for the output file name
	 */
	private final static String DEFAULT_DATE_FORMAT = "yyyyMMddhhmmss";
	
	/**
	 * String representation of the date
	 */
	private String dateInStrFormat;
	
	/**
	 * The date format to use for the output and mapping files
	 */
	private String dateFormat = DEFAULT_DATE_FORMAT;
	

	/**
	 * Map containing the 2 Header keys, namely PARAMETER_NAME and
	 * PARAMETER_VALUE and their index in the header row. This will help us
	 * determine the place of the parameter name and the place of the parameter
	 * value in the row.
	 */
	private static Map<String, Integer> headerKeyToIndexMap = new HashMap<>();

	/**
	 * Initializing the #headerKeyToIndexMap with -1 values. This will be
	 * populated when we first read the Header line
	 */
	static {
		headerKeyToIndexMap.put("PARAMETER_NAME", -1);
		headerKeyToIndexMap.put("PARAMETER_VALUE", -1);
		headerKeyToIndexMap.put("SERIAL_NUMBER", -1);
	}

	/**
	 * Constructor
	 * 
	 * @param keysToHash
	 */
	public DataProcessor(List<String> keysToHash) {
		this.keysToHash = keysToHash;
		this.hashingAlgo = DEFAULT_HASHING_ALGO;
		this.delimeter = DEFAULT_DELIMITER;
		this.inputBuffer = DEFAULT_BUFFER_SIZE;
	}

	/**
	 * Constructor
	 * 
	 * @param keysToHash
	 * @param hashingAlgo
	 */
	public DataProcessor(List<String> keysToHash, String hashingAlgo) {
		this.keysToHash = keysToHash;
		this.hashingAlgo = hashingAlgo;
		this.delimeter = DEFAULT_DELIMITER;
		this.inputBuffer = DEFAULT_BUFFER_SIZE;
	}

	/**
	 * Constructor
	 * 
	 * @param keysToHash
	 * @param hashingAlgo
	 * @param delimiter
	 */
	public DataProcessor(List<String> keysToHash, String hashingAlgo,
			String delimiter) {
		this.keysToHash = keysToHash;
		this.hashingAlgo = hashingAlgo;
		this.delimeter = delimiter;
		this.inputBuffer = DEFAULT_BUFFER_SIZE;
	}


	
	public void dryRun()  {
		System.out.println("Starting dry run process.");
		try(BufferedReader br = new BufferedReader(new FileReader(this.inputFilePath),
				getInputBuffer() != null ? getInputBuffer()
						: DEFAULT_BUFFER_SIZE);
				BufferedWriter bw = getFileWriter();) {
			
			File inputFile = new File(this.inputFilePath);
			if(inputFile.exists()) {
				System.out.println("Successfully located the input file at path : " + this.inputFilePath);
			} else {
				System.out.println("Can not find or load input file at path: " + this.inputFilePath);
				System.exit(0);
			}
			
			String headerLine =  br.readLine();
			System.out.println("Successfully read the 1st line from the file.");
			System.out.println(headerLine);
			bw.write(headerLine);
			System.out.println("Successfully wrote the header line to the output file at location: " + this.outputFilePath);
			System.out.println("A basic read from input fie and write to an output file was successfully executed.");
			System.out.println("Ending dry process run");
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	/**
	 * Process the file. Following Steps are followed :
	 * <ul>
	 * <li>
	 * 1) Open a {@link BufferedReader} and {@link BufferedWriter} streams to
	 * read and write respectively from/to the file</li>
	 * <li>
	 * 2) Start reading the File line by line. This is efficient because we
	 * buffer the file first in memory before calling read on it.</li>
	 * <li>
	 * 3) Find out the index of the Header fields PARAMETER_NAME and
	 * PARAMETER_VALUE</li>
	 * <li>
	 * 4) Hash the appropriate values based on the parameter names as specified
	 * in the {@link #keysToHash} list</li>
	 * <li>
	 * 5) Write the values to the {@link BufferedWriter}
	 * <li>
	 */
	public void processFile() {
		if(verbose) {
			System.out.println("Starting to process the file");
		}
		Map<String, String> stringToHashMapping = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(this.inputFilePath),
				getInputBuffer() != null ? getInputBuffer()
						: DEFAULT_BUFFER_SIZE);
				BufferedWriter bw = getFileWriter();
				BufferedWriter strToHashFileWriter = getStringAndItsHashedValuesWriter()) {
			if(verbose) 
				System.out.println("Initialized Buffered Reader and Buffered Writer to read from and write to a file.");
			
			String currentLine;
			Boolean isHeader = true;
			while ((currentLine = br.readLine()) != null) {
				String[] lineValues = currentLine.split(getDelimeter());
				if (isHeader) {
					createIndexMap(lineValues, headerKeyToIndexMap);
					
					String headerStr = convertToString(lineValues);
					
					bw.write(headerStr);
					bw.newLine();
					if(verbose) {
						System.out.println("The Index of PARAMETER_NAME and PARAMETER_VALUE fileds are : " + headerKeyToIndexMap.toString());
						System.out.println("Header fileds written to output file are :" + lineValues.toString());
					}
					isHeader = false;
				} else {

					String[] hashedValues = hashValues(lineValues,
							stringToHashMapping);
					
					String hashedString = convertToString(hashedValues);
					
					bw.write(hashedString);
					bw.newLine();
					if(verbose) {
						System.out.println("String written to output file, after applying potential hashing to some values, if any is :" + hashedString);
					}
				}

			}
			if(verbose){
				System.out.println("Values that are hashed are : " + stringToHashMapping.keySet().toString());
			}
			//finally write the original string to hash mapping in a different file
			String startLine = "VALUE_TO_HASH = HASHED_VALUE";
			strToHashFileWriter.write(startLine);
			strToHashFileWriter.newLine();
			for(String key : stringToHashMapping.keySet()) {
				String line = key + " = " +stringToHashMapping.get(key);
				strToHashFileWriter.write(line);
				strToHashFileWriter.newLine();
			}
			

		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		
	}
	
	private BufferedWriter getStringAndItsHashedValuesWriter() throws IOException{
		int index = this.inputFilePath.lastIndexOf(".");
		String subString = this.inputFilePath.substring(0, index);
		subString = subString.concat("_mapping_").concat(dateInStrFormat).concat(".").concat(
				this.inputFilePath.substring(index + 1,
						this.inputFilePath.length()));
		if(verbose) {
			System.out.println("Mapping file path is :" + subString);
		}
		
		return getFileWriter(subString);
		
	}

	
	private BufferedWriter getFileWriter(String filePath) throws IOException{
		BufferedWriter result;

		FileWriter fw = new FileWriter(filePath);
		result = new BufferedWriter(fw);
		if(verbose) {
			System.out.println("BufferedWriter created");
		}
		
		return result;
	}
	/**
	 * Return a simple {@link BufferedWriter}.
	 * 
	 * @return {@link BufferedWriter}
	 * @throws IOException
	 *             if an exception occurs while creating a new file.
	 */
	private BufferedWriter getFileWriter()  throws IOException {
		
		
		if (this.outputFilePath == null) {
			Date curDate = new Date();
			SimpleDateFormat format = new SimpleDateFormat(dateFormat);
			dateInStrFormat = format.format(curDate);
			int index = this.inputFilePath.lastIndexOf(".");
			String subString = this.inputFilePath.substring(0, index);
			subString = subString.concat("_output_").concat(dateInStrFormat).concat(".").concat(
					this.inputFilePath.substring(index + 1,
							this.inputFilePath.length()));


			this.outputFilePath = subString;
			if(verbose) {
				System.out.println("Output file path is : " + outputFilePath);
			}
		}

		
		return getFileWriter(outputFilePath);
	}

	/**
	 * Convert an array of values to string using the specified {@link #getDelimeter()}
	 * @param hashedValues an array of string
	 * @return string whose values are separated by the specified {@link #getDelimeter()}
	 */
	private String convertToString(String[] hashedValues) {
		StringBuffer valueStr = new StringBuffer();
		for (String val : hashedValues) {
			valueStr.append(val).append(getDelimeter());
		}
		valueStr.deleteCharAt(valueStr.lastIndexOf(getDelimeter()));
		return valueStr.toString();
	}


	public String getFilePath() {
		return inputFilePath;
	}

	public void setFilePath(String filePath) {
		this.inputFilePath = filePath;
	}

	private void createIndexMap(String[] values, Map<String, Integer> indexMap) {
		Set<String> keySet = indexMap.keySet();
		for (int index = 0; index < values.length; index++) {
			if (keySet.contains(values[index])) {
				indexMap.put(values[index], index);
			}
		}
	}

	private String surroundWithQuotesIfRequired(String value) {
		String newValue = value;
		if(!newValue.startsWith(DOUBLE_QUOTE_SYMBOL)) {
			if(surroundWithQuotes) {
				newValue = DOUBLE_QUOTE_SYMBOL + value + DOUBLE_QUOTE_SYMBOL;
			}
		}
		
		return newValue;
	}
	private String[] hashValues(String[] values,
			Map<String, String> stringValueToStringHashMapping) {
		String[] hashedValues = values;

		Integer index = headerKeyToIndexMap.get("PARAMETER_NAME");
		if (index >= values.length) {
			throw new RuntimeException(
					"index value is greater than the total values on the line");
		}
		String parameterName = values[index];
		if (valueNeedsHashing(parameterName)) {
			String valueToHash = values[headerKeyToIndexMap
					.get("PARAMETER_VALUE")];
			String hashedValue = null;
			if (stringValueToStringHashMapping.containsKey(valueToHash)) {
				hashedValue = stringValueToStringHashMapping
						.get(valueToHash);
				hashedValue = surroundWithQuotesIfRequired(hashedValue);
				hashedValues[headerKeyToIndexMap.get("PARAMETER_VALUE")] = hashedValue;
			} else {
				hashedValue = HashGenerator
						.generateHash(valueToHash, getHashingAlgo());
				hashedValue = surroundWithQuotesIfRequired(hashedValue);
				hashedValues[headerKeyToIndexMap.get("PARAMETER_VALUE")] = hashedValue;
				stringValueToStringHashMapping
						.put(valueToHash, hashedValues[headerKeyToIndexMap
								.get("PARAMETER_VALUE")]);
			}
			

			if(verbose) {
				System.out.println("Original Value : " + valueToHash + " Hashed Value : " + hashedValues[headerKeyToIndexMap.get("PARAMETER_VALUE")]);
				
			}
		}
		hashSerialNumber(hashedValues, stringValueToStringHashMapping);
		return hashedValues;
	}
	
	private String[] hashSerialNumber(String[] values,
			Map<String, String> stringValueToStringHashMapping) {
		String[] hashedValues = values;
		Integer index = headerKeyToIndexMap.get("SERIAL_NUMBER");
		if (index >= values.length) {
			throw new RuntimeException(
					"index value is greater than the total values on the line");
		}
		String valueToHash = values[index];
		//valueToHash = removeQuotesIfAny(valueToHash);
		String hashedValue = null;
		if (stringValueToStringHashMapping.containsKey(valueToHash)) {
			hashedValue = stringValueToStringHashMapping
					.get(valueToHash);
			hashedValue = surroundWithQuotesIfRequired(hashedValue);
			hashedValues[headerKeyToIndexMap.get("SERIAL_NUMBER")] = hashedValue;
		} else {
			hashedValue = HashGenerator
					.generateHash(valueToHash, getHashingAlgo());
			hashedValue = surroundWithQuotesIfRequired(hashedValue);
			hashedValues[headerKeyToIndexMap.get("SERIAL_NUMBER")] = hashedValue;
			stringValueToStringHashMapping
					.put(valueToHash, hashedValues[headerKeyToIndexMap
							.get("SERIAL_NUMBER")]);
		}
		return hashedValues;
		
	}

	private Boolean valueNeedsHashing(String paramName) {
		paramName = removeQuotesIfAny(paramName);
		for (String keysToHash : getKeysToHash()) {
			if (paramName.endsWith(keysToHash)) {
				return true;
			}
		}
		return false;
	}

	private String removeQuotesIfAny(String paramName) {
		if (paramName.startsWith(DOUBLE_QUOTE_SYMBOL)) {
			paramName = paramName.substring(1);
			surroundWithQuotes = true;
		}
		if (paramName.endsWith(DOUBLE_QUOTE_SYMBOL)) {
			paramName = paramName.substring(0, paramName.lastIndexOf(DOUBLE_QUOTE_SYMBOL));
		}

		return paramName;
	}

	public List<String> getKeysToHash() {
		return keysToHash;
	}

	public String getHashingAlgo() {
		return hashingAlgo;
	}

	public String getDelimeter() {
		return delimeter;
	}

	public Integer getInputBuffer() {
		return inputBuffer;
	}

	public void setInputBuffer(Integer inputBuffer) {
		this.inputBuffer = inputBuffer;
	}

	public Boolean getVerbose() {
		return verbose;
	}

	public void setVerbose(Boolean verbose) {
		this.verbose = verbose;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public void setHashingAlgo(String hashingAlgo) {
		this.hashingAlgo = hashingAlgo;
	}
	

}
