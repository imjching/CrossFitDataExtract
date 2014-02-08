package data;

import au.com.bytecode.opencsv.CSVWriter;
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
 * @author	J.Ching <imjching@hotmail.com>
 * @copyright	Copyright (C) 2014, J.Ching <imjching@hotmail.com>
 * @license	Modified BSD License (refer to LICENSE)
 */
public class DataExtract {

    public static final int THREAD_COUNT = 32;
    public static final String MAP_DATA = "cfaffmapdata.js";
    private Map<Integer, CrossFitEntry> entries = new LinkedHashMap<>(); // iterate order
    private List<String[]> datalist = new LinkedList<>();
    //http://map.crossfit.com/affinfo.php?a=9825&t=0 
    //Start: 0, End: 9825

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
                    String[] data = line.substring(15, line.length() - 2).split(","); // Remove variable args
                    for (int i = 0; i < data.length; i += 3) {
                        int idd = Integer.parseInt(data[i]);
                        //System.out.println(idd);
                        entries.put(idd, new CrossFitEntry(idd, data[i + 1], data[i + 2]));
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
                    String[] doc = HttpConnection.connect("http://map.crossfit.com/affinfo.php?a=" + ent.getKey() + "&t=0").get().split("<br />");
                    String[] link = doc[0].split("\" target=\"_blank\">");
                    CrossFitEntry cfe = ent.getValue();
                    cfe.setName(link[0].substring(12));
                    cfe.setUrl(link[1].substring(0, link[1].length() - 8));
                    cfe.setAddress(doc[1] + ", " + doc[2]);
                    cfe.setPhone(doc[3]);                    
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
        datalist.add(new String[]{"id", "name", "address", "phone", "url", "latitude", "longitude"}); // header
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
