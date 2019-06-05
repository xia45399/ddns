package com.summer.ddns.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtil {
    private static final int TIMEOUT = 10 * 1000;
    private static final int cache = 10 * 1024;
    private static final String TEMP = ".temp";

    /**
     * @param uri
     * @param param post内容格式为param1=value¶m2=value2¶m3=value3
     * @return
     * @throws Exception
     */
    public static String post(String uri, String param) throws Exception {
        // System.out.println("post请求:" + uri + "," + param);
        InputStream inputStream = null;
        BufferedReader in = null;
        InputStreamReader inputStreamReader = null;
        OutputStream outputStream = null;
        OutputStreamWriter out = null;
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(uri);
            URLConnection httpConnection = url.openConnection();
            httpConnection.setConnectTimeout(TIMEOUT);
            httpConnection.setDoOutput(true);
            httpConnection.setUseCaches(false);
            outputStream = httpConnection.getOutputStream();
            out = new OutputStreamWriter(outputStream, "UTF-8");
            out.write(param);
            out.flush();

            inputStream = httpConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            in = new BufferedReader(inputStreamReader);
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } finally {
            if (inputStream != null) inputStream.close();
            if (in != null) in.close();
            if (inputStreamReader != null) inputStreamReader.close();
            if (outputStream != null) outputStream.close();
            if (out != null) out.close();
        }
        return result.toString();
    }

    public static String get(String uri) throws Exception {
        // System.out.println("get请求:" + uri);

        InputStream inputStream = null;
        BufferedReader in = null;
        InputStreamReader inputStreamReader = null;
        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL(uri);
            URLConnection httpConnection = url.openConnection();
            httpConnection.setConnectTimeout(TIMEOUT);
            httpConnection.setDoOutput(true);
            httpConnection.setUseCaches(false);

            inputStream = httpConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            in = new BufferedReader(inputStreamReader);
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } finally {
            if (inputStream != null) inputStream.close();
            if (in != null) in.close();
            if (inputStreamReader != null) inputStreamReader.close();
        }
        return result.toString();
    }

}
