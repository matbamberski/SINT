import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class CSVReader {

    private String csvFile = "src/main/resources/stats.csv";
    private File csv;
    private Map<String, List<Integer>> stats = new HashMap<>();
    private String delimiter = ";";

    public CSVReader() {
        this.csvFile = csvFile;
        csv = new File(csvFile);
        if (csv.exists())
            readCsv();
    }

    private void readCsv() {
        String line = "";

        try(BufferedReader br = new BufferedReader(new FileReader(csvFile))){

            while ((line = br.readLine()) != null) {
                String[] domain = line.split(delimiter);
                stats.put(domain[0], Arrays.asList(Integer.parseInt(domain[1]), Integer.parseInt(domain[2]), Integer.parseInt(domain[3])));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToCsv(String domain, List<String> values){
        try {
            FileWriter fw = new FileWriter(csvFile);
            writeLine(fw, domain, values);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeLine(Writer w, String domain, List<String> values) throws IOException{

        StringBuilder sb = new StringBuilder();
        sb.append(domain);
        for (String value : values) {
            sb.append(delimiter);
            sb.append(value);
        }
        sb.append("\n");
        w.append(sb.toString());
    }

    public void saveStats(String domain, int reqCounter, int byteReqCounter, int byteRecCounter){
        if (stats.containsKey(domain)){
            List<Integer> hostStats = new ArrayList<>();
            hostStats.add(stats.get(domain).get(0) + reqCounter);
            hostStats.add(stats.get(domain).get(1) + byteReqCounter);
            hostStats.add(stats.get(domain).get(2) + byteRecCounter);
            stats.put(domain, hostStats);
        } else {
            List<Integer> hostStats = new ArrayList<>();
            hostStats.add(reqCounter);
            hostStats.add(byteReqCounter);
            hostStats.add(byteRecCounter);
            stats.put(domain, hostStats);
        }
        stats.forEach((s, integers) -> {
            readCsv();
            List<String> listStrings = new ArrayList<>();
            for (Integer value: integers){
                listStrings.add(Integer.toString(value));
            }
            saveToCsv(s, listStrings);
        });
    }

}
