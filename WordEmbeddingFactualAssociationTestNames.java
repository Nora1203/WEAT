import java.io.File;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

/*
 * @author Aylin Caliskan (aylinc@princeton.edu)
 */

public class WordEmbeddingFactualAssociationTestNames {
	public static void main(String[] args) throws Exception{


        // Filename to output results
        String outputFile = "out/word2vec1_normaldistribution_caseSensitivitytrue.txt";
        
        // Filename of census names statistics in CSV format
        String namesFile = "data/censusNames1990.csv";

        // Filename of semantic model to use
        String semanticModel = "lib/glove.840B.300d.txt";

        // Filename of list of names to test (separated by newlines)
        String namesToTestFilename= "data/testName.txt";

    	int wordDimension = 300;
    	String delimiter = " ";	//dimension delimiter in the word embeddings
    	boolean caseSensitive = true;
    	boolean checkWordPresence = true;
    	boolean getCentroid = true;
    	
 	
    	//Androgynous names - 1990 U.S. census data - http://www.census.gov/main/www/cen1990.html
		File namesToTestFile = new File(namesToTestFilename);
        List<String> namesList = Utils.readFileUTF8(namesToTestFile, true);
        String[] names = namesList.toArray(new String[namesList.size()]);
        
    	//Association attributes - http://projectimplicit.net/nosek/papers/harvesting.GroupDynamics.pdf
    	//Nosek, 2002 - Harvesting Implicit Group Attitudes and Beliefs From a Demonstration Web Site      
    	//female
    	String[] attributesFirstSet={"caress", "freedom", "health", "love", "peace", "cheer", "friend", "heaven", "loyal", "pleasure", "diamond", "gentle", "honest", "lucky", "rainbow", "diploma", "gift", "honor", "miracle", "sunrise", "family", "happy", "laughter", "paradise", "vacation"};
    	//male
    	String[] attributesSecondSet={"abuse", "crash", "filth", "murder", "sickness", "accident", "death", "grief", "poison", "stink", "assault", "disaster", "hatred", "pollute", "tragedy", "divorce", "jail", "poverty", "ugly", "cancer", "kill", "rotten", "vomit", "agony", "prison"};
        //input ends

        if (checkWordPresence == true) {
            //remove words from categories if they do not exist
            names = Utils.removeCategoryWordsIfNotInDictionary(names, semanticModel, wordDimension, delimiter, caseSensitive);
            attributesFirstSet = Utils.removeCategoryWordsIfNotInDictionary(attributesFirstSet, semanticModel, wordDimension, delimiter, caseSensitive);
            attributesSecondSet = Utils.removeCategoryWordsIfNotInDictionary(attributesSecondSet, semanticModel, wordDimension, delimiter, caseSensitive);
        }

        double[] centroid = new double[wordDimension];
        if (getCentroid == true) {
            //concept1 centroid
            int counter=0;

            for (int i = 0; i < names.length; i++) {			
                double[] concept1Embedding = new double[wordDimension];
                concept1Embedding = Utils.getWordEmbedding(semanticModel, wordDimension, delimiter, names[i], caseSensitive);

                // If embedding[1] == 999, then the word does not exist in the embedding
                if (concept1Embedding[1] != 999) {
                    counter++;
                }
            
                for (int column=0; column < wordDimension; column++) {
                    centroid[column] = centroid[column] + concept1Embedding[column];
                }
            }

            for (int column=0; column < wordDimension; column++) {
                centroid[column] = centroid[column] / counter;
            }
        }

        String[] bothStereotypes = (String[])ArrayUtils.addAll(attributesFirstSet, attributesSecondSet);

        double[] meanConcept1Stereotype1 = new double[names.length];
        double[] meanConcept1Stereotype2 = new double[names.length];

        // Calculate mean similarity between names and male attributes
        for (int i = 0; i < names.length; i++) {
            double[] concept1Embedding = new double[wordDimension];
            concept1Embedding = Utils.getWordEmbedding(semanticModel, wordDimension, delimiter, names[i], caseSensitive);

            for (int j=0; j < attributesFirstSet.length; j++) {		
                double[] stereotype1Embedding = new double[wordDimension];
                stereotype1Embedding = Utils.getWordEmbedding(semanticModel, wordDimension, delimiter, attributesFirstSet[j], caseSensitive);
                double similarityCompatible = Utils.cosineSimilarity(concept1Embedding, stereotype1Embedding);
                meanConcept1Stereotype1[i] = meanConcept1Stereotype1[i] + similarityCompatible;
            }	
            meanConcept1Stereotype1[i] = meanConcept1Stereotype1[i] / (attributesFirstSet.length);	
        }

        // Calculate mean similarity between names and female attributes
        for (int i = 0; i < names.length; i++) {
            double[] concept1Embedding = new double[wordDimension];
            concept1Embedding = Utils.getWordEmbedding(semanticModel, wordDimension, delimiter, names[i], caseSensitive);

            for (int j = 0; j < attributesSecondSet.length; j++) {
                double[] stereotype2Embedding = new double[wordDimension];
                stereotype2Embedding = Utils.getWordEmbedding(semanticModel, wordDimension, delimiter, attributesSecondSet[j], caseSensitive);
                double similarityCompatible = Utils.cosineSimilarity(concept1Embedding, stereotype2Embedding);
                meanConcept1Stereotype2[i] = meanConcept1Stereotype2[i] + similarityCompatible;
            }		
            meanConcept1Stereotype2[i] = meanConcept1Stereotype2[i]/(attributesSecondSet.length );
        }
            
        // Calculate mean similarity between names and all attributes,
        double[][] concept1NullMatrix = new double[names.length][bothStereotypes.length];

        for (int i = 0; i< names.length; i++) {		
            double[] concept1Embedding = new double[wordDimension];					
            concept1Embedding = Utils.getWordEmbedding(semanticModel, wordDimension, delimiter, names[i], caseSensitive);
                                    
            for (int j = 0; j < bothStereotypes.length; j++) {								
                double similarityCompatible; 							
                double[] nullEmbedding = new double[wordDimension];				
                nullEmbedding = Utils.getWordEmbedding(semanticModel, wordDimension, delimiter, bothStereotypes[j], caseSensitive);				
                similarityCompatible = Utils.cosineSimilarity(concept1Embedding, nullEmbedding);				
                concept1NullMatrix[i][j] = similarityCompatible;
            }		
        }
                
        // Calculate size of effect between gendered similarities
        for (int i = 0; i < names.length; i++) {
            double percentage = Utils.calculateWomenNamePercentage(namesFile, names[i].toLowerCase());
            double[] nullDistributionConcept1 = new double[bothStereotypes.length];
            
            for (int iter=0; iter < bothStereotypes.length; iter++) {
                nullDistributionConcept1[iter] = concept1NullMatrix[i][iter];
            }

            double[] concept1Embedding = Utils.getWordEmbedding(semanticModel, wordDimension, delimiter, names[i], caseSensitive);
            int frequency = Utils.getWordFrequencyOrder(semanticModel, wordDimension, delimiter, names[i], caseSensitive);

            System.out.println(names[i] +", " + percentage +", "+Utils.effectSize(nullDistributionConcept1,meanConcept1Stereotype1[i] - meanConcept1Stereotype2[i] )  
            +", "+Utils.cosineSimilarity(concept1Embedding, centroid)+", " + frequency);	
            
            Utils.writeFile(names[i] +", " + percentage +", "+Utils.effectSize(nullDistributionConcept1,meanConcept1Stereotype1[i] - meanConcept1Stereotype2[i] ) 
            +", "+Utils.cosineSimilarity(concept1Embedding, centroid)+", " + frequency +"\n", outputFile, true);										
            
        }	    		
    }	
}
