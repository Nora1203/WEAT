import java.util.Arrays;
import java.util.Scanner;

/* Reference
Caliskan, Aylin, 2017, "Replication Data for: WEFAT and WEAT",
https://doi.org/10.7910/DVN/DX4VWP, Harvard Dataverse, V2, UNF:6:C9yaa+UeGfFmtuHz734iKw== [fileUNF]
 */

public class WordEmbeddingAssociationTest {
	public static void main(String[] args) throws Exception{
		
/*Pick a number for bias type to test:
type 1:  biasType = language-bias
*/
		
		// create a scanner so we can read the command-line input
	    Scanner scanner = new Scanner(System.in);

	    //  prompt for the bias type to be tested
	    System.out.println("Pick a number for bias type to test:" + "\n" +
	    		"type 1:  biasType = language-bias" + "\n");

	    
	    // get their input as an integer	    
		int arg1 = scanner.nextInt();
		
		if (args.length > 0) {
		    try {
		    	
		    	arg1 = Integer.parseInt(args[0]);
		    } catch (NumberFormatException e) {
		        System.err.println("Argument" + args[0] + " must be an integer.");
		        System.exit(1);
		    }
		}
		
    	int bias =arg1;

		
		//input parameters
    	String semanticModel="lib/glove.840B.300d.txt";
    //	String semanticModel="models/GoogleNews-vectors-negative300.txt";

    	int wordDimension =300;
    	String delimiter =" ";	//dimension delimiter in the word embeddings
    	boolean caseSensitive=true; //prefer case sensitivity
    	boolean checkWordPresence=true;
    	String distribution = "normal";
    //	String distribution = "empirical";
    	String outputFile= "out/word2vec"+bias+"_"+distribution+"distribution_caseSensitivity"+caseSensitive+".txt" ;
    	int iterations=0;
    	
    	System.out.println("Generating results for bias"+bias);
    	
    	if(distribution.equals("empirical")){
    		iterations = 1000000;
    	}
    	if(distribution.equals("normal")){
    		iterations = 1000;
    	}
    	
    	String biasType = null;
    	String[] target1 = null;
    	String[] target2 = null;
    	String[] attribute1 = null;
    	String[] attribute2 = null;
    	
    	
    	
        switch (bias) {
			case 1:
				biasType = "language-bias";

				String[] female = {"woman", "mother"};
				target1 = female;

				String[] male = {"man", "father"};
				target2 = male;

				String[] pleasant = {"health", "happy"};
				attribute1 = pleasant;

				String[] unpleasant = {"pollute", "tragedy"};
				attribute2 = unpleasant;
				break;
		}


		if(checkWordPresence == true){
		//remove words from categories if they do not exist
		target1 = Utils.removeCategoryWordsIfNotInDictionary(target1, semanticModel, wordDimension, delimiter, caseSensitive);
		target2 = Utils.removeCategoryWordsIfNotInDictionary(target2, semanticModel, wordDimension, delimiter, caseSensitive);
		attribute1 = Utils.removeCategoryWordsIfNotInDictionary(attribute1, semanticModel, wordDimension, delimiter, caseSensitive);
		attribute2 = Utils.removeCategoryWordsIfNotInDictionary(attribute2, semanticModel, wordDimension, delimiter, caseSensitive);
		}
		
	    
		Utils.writeFile("Target1: " + Arrays.toString(target1) + "\n", outputFile, true);
		Utils.writeFile("Target2: " + Arrays.toString(target2) + "\n", outputFile, true);
		Utils.writeFile("Attributes1:" + Arrays.toString(attribute1) + "\n", outputFile, true);
		Utils.writeFile("Attributes2: " + Arrays.toString(attribute2) + "\n", outputFile, true);

		
		double results[] = Utils.getPValueAndEffect(target1, target2, attribute1, attribute2,  caseSensitive,  semanticModel,  wordDimension,  delimiter, distribution, iterations); 
		System.out.println(biasType + ": p-value: "+ results[0] +"  ---  effectSize: "+ results[1] );			
		Utils.writeFile(biasType + ": p-value: "+ results[0] +" , effectSize: "+ results[1] + "\n"+ "\n", outputFile, true);					

    	}
	}
