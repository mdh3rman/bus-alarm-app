package com.example.h3rman.busappalarm;

/**
 * Created by h3rman on 5/7/2016.
 */
import android.content.ContentValues;
import android.util.Log;
import android.util.StringBuilderPrinter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class ServiceHandler {

    static String response = null;
    String charset = "UTF-8";
    HttpURLConnection conn;
    DataOutputStream wr;
    StringBuilder result;
    URL urlObj;
    StringBuilder sbParams;
    String paramsString;

    public final static int GET = 1;
    public final static int POST = 2;

    public ServiceHandler(){

    }
    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * */
    public String makeServiceCall(String url, int method) {
        return this.makeServiceCall(url, method, null);
    }

    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     * */
    public String makeServiceCall(String urlString, int method,
                                  ContentValues params) {

        sbParams = new StringBuilder();
        int i = 0;
        for(String key : params.keySet()){
            try{
                if (i != 0){
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(params.getAsString(key),charset));
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            i++;
        }

        if (method == POST) {
            try{
                urlObj = new URL(urlString);
                conn = (HttpURLConnection) urlObj.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept-charset", charset);
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(150000);
                conn.connect();
                paramsString = sbParams.toString();

                wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(paramsString);
                wr.flush();
                wr.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        else if (method == GET){
            if(sbParams.length() != 0){
                urlString += "?" + sbParams.toString();
            }

            try{
                urlObj = new URL(urlString);
                Log.d("GET service at ",urlObj.toString());
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.ncs.com.sg",8080));
                conn = (HttpURLConnection) urlObj.openConnection(proxy);
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept-Charset", charset);
                conn.setConnectTimeout(15000);
                conn.connect();
                Log.d("Connection Result: ", "Success");
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        try{
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                result.append(line);
            }
            Log.d("JSON Parser", "result: " + result.toString());
        } catch (IOException e){
            e.printStackTrace();
        }
        conn.disconnect();

        return result.toString();
         /*try {


            // http client
            HttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            // Checking http request method type
            if (method == POST) {
                HttpPost httpPost = new HttpPost(url);
                // adding post params
                if (params != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                }

                httpResponse = httpClient.execute(httpPost);

            } else if (method == GET) {
                // appending params to url
                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }
                HttpGet httpGet = new HttpGet(url);
                Log.d("Request URL: ",url);
                httpGet.addHeader("accept","application/json");
                httpGet.addHeader("AccountKey", "SUoJf+QUbPhGMRjOF9/4FQ==");
                httpGet.addHeader("UniqueUserID", "21e564ee-52c1-4bbb-9242-819ce7adc22a");
                httpResponse = httpClient.execute(httpGet);
                Log.d("Response Status: ", httpResponse.getStatusLine().toString());

            }
            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;*/

    }
}
