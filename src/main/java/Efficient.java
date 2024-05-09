import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Efficient {

    //sequence
    static String s1;
    static String s2;

    //alignment
    static String a1;
    static String a2;
    static int cost = 0;

    static int GAP_PENALTY = 30;

    // Cost matrix for mismatches
//    static final int[][] MIS_MATCH = {
//            {0, 110, 48, 94},
//            {110, 0, 118, 48},
//            {48, 118, 0, 110},
//            {94, 48, 110, 0}
//    };
//    static HashMap<Character, Integer> map;

    static Map<Character, Map<Character, Integer>> MIS_MATCH;
    public static void main(String[] args) {

        String input = args[0];
        String output = args[1];

        readFromFile(input); // Read input from file

        // Initialize map for character to integer mapping
        initializeMismatchMatrix();

        // Record memory usage and start time
        double beforeUsedMem = getMemoryInKB();
        double startTime = getTimeInMilliseconds();

        // Get the alignment
        String[] alignment = getAlignment(s1, s2);

        a1 = alignment[0];
        a2 = alignment[1];

        // Record memory usage and end time
        double afterUsedMem = getMemoryInKB();
        double endTime = getTimeInMilliseconds();
        double totalUsage =  afterUsedMem-beforeUsedMem;
        double totalTime =  endTime - startTime;

        // Write results to the output file
        writeToFile(output, (float) totalTime, (float) totalUsage);

    }

    public static void initializeMismatchMatrix() {
        MIS_MATCH = new HashMap<>();
        MIS_MATCH.put('A', new HashMap<>());
        MIS_MATCH.get('A').put('A', 0);
        MIS_MATCH.get('A').put('C', 110);
        MIS_MATCH.get('A').put('G', 48);
        MIS_MATCH.get('A').put('T', 94);

        MIS_MATCH.put('C', new HashMap<>());
        MIS_MATCH.get('C').put('A', 110);
        MIS_MATCH.get('C').put('C', 0);
        MIS_MATCH.get('C').put('G', 118);
        MIS_MATCH.get('C').put('T', 48);

        MIS_MATCH.put('G', new HashMap<>());
        MIS_MATCH.get('G').put('A', 48);
        MIS_MATCH.get('G').put('C', 118);
        MIS_MATCH.get('G').put('G', 0);
        MIS_MATCH.get('G').put('T', 110);

        MIS_MATCH.put('T', new HashMap<>());
        MIS_MATCH.get('T').put('A', 94);
        MIS_MATCH.get('T').put('C', 48);
        MIS_MATCH.get('T').put('G', 110);
        MIS_MATCH.get('T').put('T', 0);


    }

    static String reverseString(String s){
        return (new StringBuilder(s).reverse().toString());
    }


    //// Get the alignment of two strings
    static String[] getAlignment(String x, String y){
        // Base case: if either string's length is less than or equal to 2
        if(x.length() <= 2 || y.length() <= 2){
            baseCaseAlignment(x, y);
            return new String[]{a1, a2};
        }

        // Divide the strings into left and right halves
        String xLeft = x.substring(0, x.length()/2);
        String xRight = x.substring(x.length()/2);

        // Get the cost of alignment for the left and right halves
        int[] forwardCost = getCost(xLeft, y);
        int[] backwardCost = getCost(reverseString(xRight), reverseString(y));

        // Find the split index with minimum cost
        int splitIndex = -1;
        int minCost = Integer.MAX_VALUE;


        for(int i = 0; i < forwardCost.length; i++){
            if(forwardCost[i] + backwardCost[forwardCost.length-i-1] < minCost){
                minCost = forwardCost[i] + backwardCost[forwardCost.length-i-1];
                splitIndex = i;
            }
        }

        // Recursively get alignments for left and right halves
        String[] left = getAlignment(xLeft, y.substring(0, splitIndex));
        String[] right = getAlignment(xRight, y.substring(splitIndex));

        // Combine the alignments of left and right halves
        return new String[]{left[0]+right[0], left[1]+right[1]};


    }



    // Get the alignment for base cases
    static void baseCaseAlignment(String x, String y){

        if(x.equals(y)){
            a1 = x;
            a2 = y;
        }

        int[][] dp = new int[x.length()+1][y.length()+1];

        //initialize dp
        for(int i=0;i<=x.length();i++){
            dp[i][0] = i*GAP_PENALTY;
        }
        for(int i=0;i<=y.length();i++){
            dp[0][i] = i*GAP_PENALTY;
        }

        for(int i=1;i<=x.length();i++){
            for(int j=1;j<=y.length();j++){
                int temp = MIS_MATCH.get(x.charAt(i-1)).get(y.charAt(j-1));
                dp[i][j] = Math.min(dp[i-1][j-1]+temp,Math.min(dp[i-1][j]+GAP_PENALTY,dp[i][j-1]+GAP_PENALTY));
            }
        }

        // Trace back to get the alignment
        int i = x.length();
        int j = y.length();

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        while(i>0 || j>0){
            if(i >= 1 && j >= 1 && dp[i][j] == dp[i-1][j-1]+MIS_MATCH.get(x.charAt(i-1)).get(y.charAt(j-1))){
                sb1.append(x.charAt(i-1));
                sb2.append(y.charAt(j-1));
                i--;
                j--;
                cost += MIS_MATCH.get(x.charAt(i)).get(y.charAt(j));
            }else if(i >=1 && dp[i][j] == dp[i-1][j]+GAP_PENALTY){
                sb1.append(x.charAt(i-1));
                sb2.append('_');
                i--;
                cost += GAP_PENALTY;
            }else{
                sb1.append('_');
                sb2.append(y.charAt(j-1));
                j--;
                cost += GAP_PENALTY;
            }
        }

        // Return the reversed alignments
        a1 = sb1.reverse().toString();
        a2 = sb2.reverse().toString();
        //return new String[]{sb1.reverse().toString(), sb2.reverse().toString()};
    }


    // Get the cost of alignment between two strings
    static int[] getCost(String x, String y){
        int[] prev = new int[y.length()+1];
        int[] curr = new int[y.length()+1];

        for(int i=0;i<=y.length();i++){
            prev[i] = i*GAP_PENALTY;
        }

        for(int i=1;i<=x.length();i++){
            curr[0] = i*GAP_PENALTY;
            for(int j=1;j<=y.length();j++){
                int cost = MIS_MATCH.get(x.charAt(i-1)).get(y.charAt(j-1));
                curr[j] = Math.min(prev[j-1]+cost,Math.min(prev[j]+GAP_PENALTY,curr[j-1]+GAP_PENALTY));
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        return prev;
    }

    public static void readFromFile(String path) {

        Pattern PATTERN = Pattern.compile("^\\d+$");

        try {
            Stream<String> lines = Files.lines(Paths.get(path));

            StringBuilder sb1 = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();

            List<Integer> group1 = new ArrayList<>();
            List<Integer> group2 = new ArrayList<>();
            int groupCounter = 0;

            for (String line : (Iterable<String>) lines::iterator) {
                if (PATTERN.matcher(line).matches()) {
                    if (groupCounter == 1) {
                        group1.add(Integer.parseInt(line));
                    } else {
                        group2.add(Integer.parseInt(line));
                    }
                } else {
                    if (groupCounter == 0) {
                        sb1 = new StringBuilder(line);
                    } else {
                        sb2 = new StringBuilder(line);
                    }
                    groupCounter++;
                }
            }
            //Iterate to get String s1 and s2
            s1 = getString(sb1, group1);
            s2 = getString(sb2, group2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void writeToFile(String outputFilePath, float time, float memory) {
        try {
            FileWriter myWriter = new FileWriter(outputFilePath);

            myWriter.write(cost+"\n");
            myWriter.write(a1 + "\n");
            myWriter.write(a2 + "\n");
            myWriter.write(time + "\n");
            myWriter.write(memory + "\n");

            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getString(StringBuilder sb, List<Integer> list){
        for(int i=0;i<list.size();i++){
            int index = list.get(i);
            String s = sb.toString();
            sb.insert(index+1, s);
        }
        return sb.toString();
    }


    private static double getMemoryInKB() {
        double total = Runtime.getRuntime().totalMemory();
        return (total-Runtime.getRuntime().freeMemory())/10e3;
    }
    private static double getTimeInMilliseconds() {
        return System.nanoTime()/10e6;
    }
}
