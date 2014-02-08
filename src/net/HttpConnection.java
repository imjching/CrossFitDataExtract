package net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * CrossFit Affiliates Data Extractor
 *
 * Concept loosely based on Jsoup HttpConnection class:
 * http://jsoup.org/apidocs/org/jsoup/helper/HttpConnection.html
 *
 * @package	net
 * @author	J.Ching <imjching@hotmail.com>
 * @copyright	Copyright (C) 2014, J.Ching <imjching@hotmail.com>
 * @license	Modified BSD License (refer to LICENSE)
 */
public class HttpConnection {

    private static final int TIMEOUT = 0; // infinity

    public static HttpConnection connect(String url) {
        HttpConnection con = new HttpConnection();
        con.url(url);
        return con;
    }

    private static String encodeUrl(String url) {
        return url.replaceAll(" ", "%20");
    }
    private Request req;
    private Response res;

    private HttpConnection() {
        req = new Request();
        res = new Response();
    }

    public HttpConnection url(String url) {
        Validate.notEmpty(url, "Must supply a valid URL");
        try {
            req.url(new URL(encodeUrl(url)));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed URL: " + url, e);
        }
        return this;
    }

    public String get() throws IOException {
        res = Response.execute(req);
        return res.getLine();
    }

    public static class Request {

        URL url;

        public URL url() {
            return url;
        }

        public void url(URL url) {
            Validate.notNull(url, "URL must not be null");
            this.url = url;
        }
    }

    public static class Response {

        private String line;

        static Response execute(Request req) throws IOException {
            Validate.notNull(req, "Request must not be null");
            String protocol = req.url().getProtocol();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw new MalformedURLException("Only http & https protocols supported");
            }
            HttpURLConnection conn = createConnection(req);
            Response res;
            try {
                conn.connect();

                int status = conn.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error fetching URL. Status: " + status + " URL: " + req.url().toString());
                }
                res = new Response();

                InputStream dataStream = null;
                BufferedReader reader = null;
                try {
                    dataStream = conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(dataStream)));

                    String combine = "";
                    StringBuilder sb = new StringBuilder();
                    while ((combine = reader.readLine()) != null) {
                        sb.append(combine).append(" ");
                    }

                    res.line = sb.substring(0, sb.length() - 1); // We have one line only, some two
                } finally {
                    if (dataStream != null) {
                        dataStream.close();
                    }
                    if (reader != null) {
                        reader.close();
                    }
                }
            } finally {
                // per Java's documentation, this is not necessary, and precludes keepalives. However in practise,
                // connection errors will not be released quickly enough and can cause a too many open files error.
                conn.disconnect();
            }
            return res;
        }

        public String getLine() {
            return line;
        }

        // set up connection defaults, and details from request
        private static HttpURLConnection createConnection(Request req) throws IOException {
            HttpURLConnection conn = (HttpURLConnection) req.url().openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false); // don't rely on native redirection support
            conn.setConnectTimeout(TIMEOUT); // infinity
            conn.setReadTimeout(TIMEOUT); // infinity
            return conn;
        }
    }
}
