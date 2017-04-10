import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by zaher on 4/9/2017.
 */
public class FuzzyK_NNClassifier {
    static List<Double> min; //the minimum value in each column of the data is stored in the respective position
    static List<Double> max; //the maximum value in each column of the data is stored in the respective position
    private List<Row> trainingData;
    private List<Row> testingData;
    int k;

    public static void main(String[] args) {

        System.out.println("How much of the data to use for testing? (value between 0% and 99%)");
        Scanner kb= new Scanner(System.in);
        double testing = (double)kb.nextInt()/100;

        System.out.println("Input the k: ");
        int k=kb.nextInt();

        FuzzyK_NNClassifier classifier = new FuzzyK_NNClassifier("iris.data", testing, k);

    }

    /**
     * @param fileName The file name to read the data from
     * @param testing  the percentage of the data to use for testing.
     * @param k        the k to use for the algorithm
     */
    public FuzzyK_NNClassifier(String fileName, double testing, int k) {
        testingData = new ArrayList<>();
        this.k = k;
        try {

            List<Row> rows = CSVReader.read(new Scanner(new File(fileName)));
            rows = normalizeData(rows);

            int forTesting = (int) (testing * rows.size());
            for (int i = 0; i < forTesting; ++i) {
                testingData.add(rows.get(0));
                rows.remove(0);
            }
            trainingData = rows;
            writeData(testingData);

            System.out.println("Testing data size: " + testingData.size());
            System.out.println("Training data size: " + trainingData.size());

            System.out.println(trainAndTest());


            ArrayList<Double> doubles = new ArrayList<>();
            doubles.add(5.1);
            doubles.add(3.0);
            doubles.add(1.4);
            doubles.add(0.2);

            Row x = normalizeRow(new Row(doubles));


            findTarget(find_class(x));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * trains the data on the training set and returns the percentage of successful predictions on the testing set
     *
     * @return
     */
    public double trainAndTest() {
        double i = 0;
        for (Row row : testingData) {
            String oldTarget = row.target;
            Row temp = find_class(row);
//            System.out.println(row+"\n"++"\n");
            if (temp.kNNRows.get(temp.kNNRows.size()-1).target.equals(oldTarget)) {
                ++i;
            }
        }
        System.out.println("i "+i);
        return i / testingData.size();
    }

    /**
     * Given the dataset, this calculates the min and max for every columns
     *
     * @param rowList
     */
    public static void calcMinMax(List<Row> rowList) {
        if (rowList == null || rowList.size() < 1) {
            return;
        }
        min = new ArrayList<>(rowList.get(0).data);
        max = new ArrayList<>(rowList.get(0).data);

        for (int i = 1; i < rowList.size(); ++i) {
            for (int j = 0; j < rowList.get(i).data.size(); ++j) {
                if (max.get(j) < rowList.get(i).data.get(j))
                    max.set(j, rowList.get(i).data.get(j));
                if (min.get(j) > rowList.get(i).data.get(j))
                    min.set(j, rowList.get(i).data.get(j));
            }
        }
    }

    /**
     * Given a list of rows, return it normalized
     *
     * @param rowList
     * @return List of the normalized data
     */
    public static List<Row> normalizeData(List<Row> rowList) {
        calcMinMax(rowList);
        for (Row r: rowList) {
            normalizeRow(r);
        }
        return rowList;
    }

    private static Row normalizeRow(Row r){
        for (int j = 0; j < r.data.size(); ++j) {
            r.data.set(j, ((r.data.get(j) - min.get(j)) / (max.get(j) - min.get(j))));
        }
        return r;
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

    /**
     * @param x the unlabeled row
     * @return Row with the updated KNNRows value in it
     */
    public Row find_class(Row x) {
        if (trainingData == null || trainingData.size() == 0) {
            System.out.println("OUTCH");
            return null;
        }

        x.kNNRows = new ArrayList<Row>();
        x.distance = new ArrayList<Double>();

        addSorted(x, trainingData.get(0));

        for (int i = 1; i < trainingData.size(); ++i) {
            if (x.distance.get(0) > getDistance(x, trainingData.get(i))) {
                addSorted(x, trainingData.get(i));
            }
        }
        return x;
    }

    /**
     * Add the into Row into the kNN array of the from Row
     * It also updates the distance
     *
     * @param from
     * @param into
     */
    private void addSorted(Row from, Row into) {
        double distance = getDistance(from, into);
        int size = from.kNNRows.size();
        int i;
        for (i = 0; i < size; ++i) {
            if (from.distance.get(i) < distance) {
                break;
            }
        }
        from.kNNRows.add(i, into);
        from.distance.add(i, distance);
        if (from.kNNRows.size() > k) {
            from.kNNRows.remove(from.kNNRows.size() - 1);
        }
    }

    /**
     * Given an unlabeled row, find the t
     *
     * @param row
     */
    public static void findTarget(Row row) {
        List<String> targets = new ArrayList<>();
        List<Integer> counters = new ArrayList<>();
        targets.add(row.kNNRows.get(0).target);
        counters.add(1);
        for (int i = 1; i < row.kNNRows.size(); ++i) {
            if (targets.contains(row.kNNRows.get(i).target)) {
                counters.set(targets.indexOf(row.kNNRows.get(i).target), counters.get(targets.indexOf(row.kNNRows.get(i).target)) + 1);
            } else {
                targets.add(row.kNNRows.get(i).target);
                counters.add(1);
            }
        }
        int max = 0;
        for (int i = 0; i < targets.size(); i++) {
            NumberFormat formatter = new DecimalFormat("#0.00");
            //if ((double) counters.get(i) / row.kNNRows.length)>())
            System.out.println("Class " + targets.get(i)
                    + " = " + formatter.format((double) counters.get(i) / row.kNNRows.size()));
        }
    }

    /**
     * Given two rows, return the distance between them
     *
     * @param row1
     * @param row2
     * @return
     */
    public static double getDistance(Row row1, Row row2) {
        double distance = 0;
        for (int i = 0; i < row1.data.size(); ++i) {
            distance += Math.pow((row1.data.get(i) - row2.data.get(i)), 2);
        }
        return distance;
    }
}
