// Name: Mwahhid Majeed
// Description: Creating a Naive Bayes Classifier to filter emails into spam and non-spam categories.

import java.io.InputStream;
import java.util.*;

public class NaiveBayesClassifier {

    private static final Set<String> vocabulary = new HashSet<>();
    private static final Map<String, Integer> spamWords = new HashMap<>();
    private static final Map<String, Integer> hamWords = new HashMap<>();
    private static int spamNum = 0;
    private static int hamNum = 0;
    private static int totalEmails = 0;
    private static int correctlyClassified = 0;

    public static void main(String[] args) {
        // Prompt user for valid filenames
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter spam training file name:");
        String file1Name = scan.nextLine();
        System.out.println("Enter ham training file name:");
        String file2Name = scan.nextLine();
        System.out.println("Enter spam testing file name:");
        String file3Name = scan.nextLine();
        System.out.println("Enter ham testing file name:");
        String file4Name = scan.nextLine();

        InputStream is1 = NaiveBayesClassifier.class.getResourceAsStream(file1Name);
        if (is1 == null) {
            System.err.println("Bad filename: " + file1Name);
            System.exit(1);
        }

        InputStream is2 = NaiveBayesClassifier.class.getResourceAsStream(file2Name);
        if (is2 == null) {
            System.err.println("Bad filename: " + file2Name);
            System.exit(1);
        }

        InputStream is3 = NaiveBayesClassifier.class.getResourceAsStream(file3Name);
        if (is3 == null) {
            System.err.println("Bad filename: " + file3Name);
            System.exit(1);
        }

        InputStream is4 = NaiveBayesClassifier.class.getResourceAsStream(file4Name);
        if (is4 == null) {
            System.err.println("Bad filename: " + file4Name);
            System.exit(1);
        }

        // SPAM TRAINING
        Scanner s = new Scanner(is1);
        trainSpam(s);

        // NOT-SPAM(HAM) TRAINING
        s = new Scanner(is2);
        trainHam(s);

        // set word frequencies to 0 for words that were not seen by either spam or ham
        for (String w : vocabulary) {
            if (!spamWords.containsKey(w)) spamWords.put(w, 0);
            if (!hamWords.containsKey(w)) hamWords.put(w, 0);
        }

        // SPAM TESTING
        s = new Scanner(is3);
        testEmails(s, true);

        // NOT-SPAM(HAM) TESTING
        s = new Scanner(is4);
        testEmails(s, false);

        System.out.printf("Total: %d/%d emails classified correctly.", correctlyClassified, totalEmails);
    }

    /**
     * Update frequencies of words appearing in spam emails
     */
    private static void trainSpam(Scanner s) {
        Set<String> trainWords = new HashSet<>();
        while (s.hasNextLine()) {
            String line = s.nextLine();

            if (line.contains("<SUBJECT>")) { // Indicates start of email
                trainWords = new HashSet<>();
                spamNum++;
                continue;

            } else if (line.contains("</BODY>")) { // Update the maps and vocabulary at end of email
                vocabulary.addAll(trainWords);
                for (String w : trainWords) {
                    if (spamWords.containsKey(w)) {
                        int freq = spamWords.get(w);
                        spamWords.put(w, freq + 1);
                    } else spamWords.put(w, 1);
                }
            } else if (line.contains("<") || line.isEmpty()) continue;

            line = line.toLowerCase();
            String[] words = line.split(" ");
            trainWords.addAll(Arrays.asList(words));
        }
    }

    /**
     * Update frequencies of words appearing in not-spam emails
     */
    private static void trainHam(Scanner s) {
        Set<String> trainWords = new HashSet<>();
        while (s.hasNextLine()) {
            String line = s.nextLine();

            if (line.contains("<SUBJECT>")) { // Indicates start of email
                trainWords = new HashSet<>();
                hamNum++;
                continue;
            } else if (line.contains("</BODY>")) { // Update the maps and vocabulary at end of email
                vocabulary.addAll(trainWords);
                for (String w : trainWords) {
                    if (hamWords.containsKey(w)) {
                        int freq = hamWords.get(w);
                        hamWords.put(w, freq + 1);
                    } else hamWords.put(w, 1);
                }
            } else if (line.contains("<") || line.isEmpty()) continue;

            line = line.toLowerCase();
            String[] words = line.split(" ");
            trainWords.addAll(Arrays.asList(words));
        }
    }

    /**
     * Runs Naive Bayes Classifier on emails and classifies them either as spam or not-spam(ham).
     */
    private static void testEmails(Scanner s, boolean spamTest) {
        Set<String> testWords = new HashSet<>();
        double probSpamEmail = Math.log((double) spamNum / (spamNum + hamNum));
        double probHamEmail = Math.log((double) hamNum / (spamNum + hamNum));
        int messageNum = 0;
        int trueFeatures = 0;

        while (s.hasNextLine()) {
            String line = s.nextLine();

            if (line.contains("<SUBJECT>")) { // Reset variables at the start of a new email
                probSpamEmail = Math.log((double) spamNum / (spamNum + hamNum));
                probHamEmail = Math.log((double) hamNum / (spamNum + hamNum));
                testWords = new HashSet<>();
                trueFeatures = 0;
                messageNum++;
                totalEmails++;
                continue;
            } else if (line.contains("</BODY>")) { // Do all the calculations for the email at its end
                for (String w : vocabulary) {
                    double wSpamProb;
                    double wHamProb;
                    if (testWords.contains(w)) {
                        trueFeatures++;
                        wSpamProb = Math.log((spamWords.get(w) + 1.0) / (spamNum + 2.0));
                        wHamProb = Math.log((hamWords.get(w) + 1.0) / (hamNum + 2.0));
                    } else {
                        wSpamProb = Math.log((spamNum - spamWords.get(w) + 1.0) / (spamNum + 2.0));
                        wHamProb = Math.log((hamNum - hamWords.get(w) + 1.0) / (hamNum + 2.0));
                    }
                    probSpamEmail += wSpamProb;
                    probHamEmail += wHamProb;
                }

                System.out.printf("TEST %s %d/%d features true %.3f %.3f ", messageNum, trueFeatures,
                        vocabulary.size(), probSpamEmail, probHamEmail);

                //Prints output correctly by ensuring whether it is checking the spam emails or the ham emails
                if (spamTest) {
                    if (probSpamEmail > probHamEmail) {
                        System.out.println("spam right");
                        correctlyClassified++;
                    } else System.out.println("ham wrong");
                } else {
                    if (probSpamEmail < probHamEmail) {
                        System.out.println("ham right");
                        correctlyClassified++;
                    } else System.out.println("spam wrong");
                }

            } else if (line.contains("<") || line.isEmpty()) continue;

            line = line.toLowerCase();
            String[] words = line.split(" ");
            testWords.addAll(Arrays.asList(words));
        }
    }
}
