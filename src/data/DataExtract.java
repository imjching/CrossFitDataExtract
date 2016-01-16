package data;

import au.com.bytecode.opencsv.CSVWriter;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.HttpConnection;

/**
 * CrossFit Affiliate Data Extractor
 *
 * @package	data
 * @author	Jay <imjching@hotmail.com>
 * @copyright	Copyright (C) 2016, Jay <imjching@hotmail.com>
 * @license	Modified BSD License (refer to LICENSE)
 */
public class DataExtract {

    public static final int THREAD_COUNT = 28;
    public static final String MAP_DATA = "cfaffmapdata-v2.js";
    private Map<Integer, CrossFitEntry> entries = new LinkedHashMap<>(); // iterate order
    private List<String[]> datalist = new LinkedList<>();
    //http://map.crossfit.com/affinfo.php?a=9825&t=0
    //http://map.crossfit.com/getAffiliateInfo.php?aid=3
    //v1: Start: 0, End: 9825
    //v2: Start: 0, End: 16029 with a total of 12140 entries

    public void load() {
        System.out.println("[LOAD] Data Extractor started.");
        long start_time = System.currentTimeMillis();
        parseMapEntries();
        extractData();
        saveToCSV();
        long end_time = System.currentTimeMillis();
        System.out.println("[END] Time Taken: " + (end_time - start_time) + " milliseconds.");
    }

    private void parseMapEntries() {
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(MAP_DATA))) {
                String line = reader.readLine();
                if (line != null) { // One line only
                    String[] data = line.split("=");
                    for (int i = 0; i < data.length; i++) {
                        String[] ent = data[i].split(",");
                        int idd = Integer.parseInt(ent[3].replace("\"", ""));
                        entries.put(idd, new CrossFitEntry(idd, ent[0], ent[1]));
                    }
                }
            }
        } catch (IOException | NumberFormatException ex) {
            System.out.println("[ERROR] parseMapEntries failed. Reason: " + ex.getMessage());
        }
        System.out.println(String.format("[PARSE] Added %d entries.", entries.size()));
    }

    private void extractData() {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<Void>> handles = new ArrayList<>();
        Future<Void> handle;
        for (final Entry<Integer, CrossFitEntry> ent : entries.entrySet()) {
            handle = executorService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    System.out.println(ent.getKey());
                    JsonNode doc = HttpConnection.connect("https://map.crossfit.com/getAffiliateInfo.php?aid=" + ent.getKey()).get();

                    CrossFitEntry cfe = ent.getValue();
                    cfe.setName(doc.get("name").asText());
                    cfe.setUrl(doc.get("website").asText());
                    cfe.setAddress(doc.get("address").asText() + ", " + doc.get("city").asText() + ", " + doc.get("state").asText() + ", " + doc.get("zip").asText() + ", " + doc.get("country").asText());
                    cfe.setPhone(doc.get("phone").asText());
                    cfe.setCFKids(doc.get("cfkids").asText());

                    return null;
                }
            });
            handles.add(handle);
        }

        for (Future<Void> h : handles) {
            try {
                h.get();
            } catch (InterruptedException | ExecutionException ex) {
                System.out.println("[ERROR] extractData failed. Reason: " + ex.getMessage());
            }
        }
        executorService.shutdownNow();

        // For CSV usage only
        datalist.add(new String[]{"id", "name", "address", "phone", "url", "latitude", "longitude", "cfkids"}); // header
        for (CrossFitEntry cf : entries.values()) {
            datalist.add(cf.toCSVString());
        }
    }

    private void saveToCSV() {
        try {
            try (CSVWriter writer = new CSVWriter(new FileWriter("entries.csv"))) {
                writer.writeAll(datalist);
            }
        } catch (IOException ex) {
            System.out.println("[ERROR] saveToCSV failed. Reason: " + ex.getMessage());
        }
        System.out.println(String.format("[SAVED] Added %d entries.", datalist.size()));
    }
}
