/*This file is for all IO for running the program.
 *Whenever a new format needs to be read, it needs to be done within here.
 * 
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class CSVReader {


    public static void main(String[]args) throws Exception{
        List<Row > data =  read(new Scanner(new File("iris.data")));
        for(int i=data.size()-1; i>=0 ; --i){
            int temp = (int)(Math.random()*i);
            System.out.println(temp);
            data.add(data.remove(temp));
        }
        writeData(data);
    }
    /**
     * Given a list of rows, print it out
     *
     * @param rowList
     */
    public static void writeData(List<Row> rowList) {
        try {
            FileWriter writer = new FileWriter("preProssedData.txt", false);
            PrintWriter printWriter = new PrintWriter(writer);
            NumberFormat formatter = new DecimalFormat("#0.00");
            for (int i = 0; i < rowList.size(); i++) {
                for (int j = 0; j < rowList.get(i).data.size(); ++j) {
                    printWriter.print(rowList.get(i).data.get(j) + ",");
                }
                printWriter.println(rowList.get(i).target);
            }
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //WHATEVER has to be replaced with whatever object ot store the database
    public static List<Row> read(Scanner kb) {
        List<Row> result = new ArrayList<>();

        while (kb.hasNext()) {
            // use comma as separator
            List<String> temp = parseLine(kb.nextLine());
            //parse the line of code here

            ArrayList<Double> data = new ArrayList<>();
            for (int i = 0; i < temp.size(); i++) {
                if (temp.get(i).matches("[-+]?\\d*\\.?\\d+")) {
                    data.add(Double.parseDouble(temp.get(i)));
                }
            }
            Row doubleList = new Row(data,temp.get(temp.size() - 1));
            result.add(doubleList);
        }
        return result;
    }


    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';

    public static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators) {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators, char customQuote) {

        List<String> result = new ArrayList<>();

        //if empty, return!
        if (cvsLine == null && cvsLine.isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }

}