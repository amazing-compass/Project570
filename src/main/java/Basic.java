import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Basic {


    //Sequences
    static String s1;
    static String s2;
    //Alignments
    static String a1;
    static String a2;

    //alignment cost
    static int cost = 0;

    static int GAP_PENALTY = 30;

    static int[][] MIS_MATCH = {{0,110,48,94},{110,0,118,48},{48,118,0,110},{94,48,110,0}};

    //map to get index of character
    static HashMap<Character, Integer> map;


    public static void main(String[] args) {

        String input = args[0];
        String output = args[1];

        //Read input file
        readFromFile(input);

        //map to get index of character
        map = new HashMap<>();
        map.put('A', 0);
        map.put('C', 1);
        map.put('G', 2);
        map.put('T', 3);


        System.gc();

        //Get memory and time before execution
        double beforeUsedMem = getMemoryInKB();
        double startTime = getTimeInMilliseconds();

        //Get alignment
        getAlignment(s1, s2);

        //Get memory and time after execution
        double afterUsedMem = getMemoryInKB();
        double endTime = getTimeInMilliseconds();
        double totalUsage =  afterUsedMem-beforeUsedMem;
        double totalTime =  endTime - startTime;

        //Write to output file
        writeToFile(output, (float) totalTime, (float) totalUsage);

        System.gc();

    }

    //Get alignment
    public static void getAlignment(String x, String y){

        int m = x.length();
        int n = y.length();

        //dp table
        int[][] dp = new int[m+1][n+1];


        //initialize dp table
        for(int i=0;i<m+1;i++){
            dp[i][0] = i*GAP_PENALTY;
        }

        for(int i=0;i<n+1;i++){
            dp[0][i] = i*GAP_PENALTY;
        }

        //fill dp table
        for(int i=1;i<m+1;i++){
            for(int j=1;j<n+1;j++){
                int cost = MIS_MATCH[map.get(s1.charAt(i-1))][map.get(s2.charAt(j-1))];
                dp[i][j] = Math.min(dp[i-1][j-1]+cost,Math.min(dp[i-1][j]+GAP_PENALTY,dp[i][j-1]+GAP_PENALTY));
            }
        }

        //top down approach to get alignment/Backtrack to get the alignment result
        int i = x.length();
        int j = y.length();

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        //
        while(i>0 || j>0){
            if(i >= 1 && j >= 1 && dp[i][j] == dp[i-1][j-1]+MIS_MATCH[map.get(x.charAt(i-1))][map.get(y.charAt(j-1))]){
                sb1.append(x.charAt(i-1));
                sb2.append(y.charAt(j-1));
                i--;
                j--;
                cost += MIS_MATCH[map.get(x.charAt(i))][map.get(y.charAt(j))];
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
        //Get the reverse result
        // Store the aligned sequences in global variables
        a1 = reverseString(sb1.toString());
        a2 = reverseString(sb2.toString());
    }


    static String reverseString(String s){
        return (new StringBuilder(s).reverse().toString());
    }

    ////Read input data from the file
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

    //Write the result to the output file
    public static void writeToFile(String outputFilePath, float time, float memory) {
        try {
            FileWriter myWriter = new FileWriter(outputFilePath);

            myWriter.write(cost+"\n");
            myWriter.write(a1 + "\n");
            myWriter.write(a2 + "\n");
            myWriter.write("Time: " + time + " ms\n");
            myWriter.write("Memory: " + memory + " KB\n");

            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //insert the string into the specified position
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



