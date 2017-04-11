
import java.util.ArrayList;

/**
 * Created by zaher on 4/9/2017.
 */
public class Row {
    ArrayList<Double> data;
    String target;
    ArrayList<Row> kNNRows;
    ArrayList<Double> distance;
    public Row(ArrayList<Double> data, String target){
        this.data=data;
        this.target=target;
    }
    public Row(Row n){
        this.data=n.data;
        this.target=n.target;
    }
    public Row(ArrayList<Double> data){
        this.data=data;
    }
    public String toString(){

        return data.toString()+" targe: "+target;
    }
}
