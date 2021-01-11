import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PriceParser {


    private static final String COMMA_DELIMITER = ",";
    private List<List<String>> records;

    public PriceParser(){
        records = new ArrayList<>();
    }

    public List<List<String>> parse(){
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("prices.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> headers = records.remove(0);
        int count = 0;
        List<List<String>> trimmedRecords = new ArrayList<>();
        //trim forecast data
        for(List<String> record: records){
            if(record.get(6).contains("FORECAST")){
                records = records.subList(0, count);
                break;
            }else{
                String[] dataToKeep = new String[2];
                dataToKeep[0] = record.get(0);
                dataToKeep[1] = record.get(1);
                trimmedRecords.add(Arrays.asList(dataToKeep));
            }
            count++;
        }
        return trimmedRecords;
    }

    public static void main(String[] args) {
        PriceParser p = new PriceParser();
        System.out.println(p.parse().toString());
    }


}
