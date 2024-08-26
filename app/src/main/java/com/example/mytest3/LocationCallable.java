package com.example.mytest3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class LocationCallable implements Callable<ArrayList<String[]>> {
    public static String get_pos(String endpoint, String encoding, Map<String, String> headers) throws IOException {
        final int TIMEOUT_MILLIS = 0;// タイムアウトミリ秒：0は無限
        StringBuilder sb = new StringBuilder();
        HttpURLConnection httpConn = null;
        BufferedReader br = null;
        InputStream is = null;
        InputStreamReader isr = null;
        try {
            URL url = new URL(endpoint);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(TIMEOUT_MILLIS);// 接続にかかる時間
            httpConn.setReadTimeout(TIMEOUT_MILLIS);// データの読み込みにかかる時間
            httpConn.setRequestMethod("GET");// HTTPメソッド
            httpConn.setUseCaches(false);// キャッシュ利用
            httpConn.setDoOutput(false);// リクエストのボディの送信を許可(GETのときはfalse,POSTのときはtrueにする)
            httpConn.setDoInput(true);// レスポンスのボディの受信を許可
            // HTTPヘッダをセット
            if (headers != null) {
                for (String key : headers.keySet()) {
                    httpConn.setRequestProperty(key, headers.get(key));// HTTPヘッダをセット
                }
            }
            httpConn.connect();
            final int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = httpConn.getInputStream();
                isr = new InputStreamReader(is, encoding);
                br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } else {
                System.out.println("responseCode is not HTTP_OK");
            }
        } finally {
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (is != null) {
                is.close();
            }
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
        return sb.toString();
    }
    public static ArrayList<String[]> get_pos_info(String sb) throws JsonProcessingException {
        class MyComparableObject implements Comparable<MyComparableObject>{
            private final String Name;
            private final double Score;
            public MyComparableObject(String Name,double Score) {
                this.Name=Name;
                this.Score= Score;
            }
            public String getName(){
                return this.Name;
            }
            public  int getScore(){
                return ((int) this.Score);
            }
            public int compareTo(MyComparableObject passedObj) {
                return 0;
            }
        }
        String pos="現在地特定失敗";
        String percent="0";
        List<String> Candidates = new ArrayList<>();
        List<Double> Scores = new ArrayList<>();
        ArrayList<String[]> return2DList = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json_result = mapper.readTree(sb);
        JsonNode Result = json_result.get("ResultSet").get("Result");
        if(Result.isArray()){
            for(JsonNode object_node : Result){
                JsonNode json_Result = mapper.readTree(object_node.toString());
                    /*　例　System.out.println(object_node);
                    {
                      "Name" : "セイコーマート新川3条店",
                      "Uid" : "b4ace94de546735eca697355e1d1db82cc0ef0e4",
                      "Category" : "セイコーマート",
                      "Label" : "セイコーマート新川3条店",
                      "Where" : "札幌市北区",
                      "Combined" : "札幌市北区のセイコーマート新川3条店付近",
                      "Score" : 4.746632159614693E-252
                     }
                    */
                Candidates.add(json_Result.get("Name").asText());
                Scores.add(json_Result.get("Score").asDouble());
            }
            List<MyComparableObject> Candidates_Scores = new ArrayList<>();
            for (int i=0;i<Candidates.size();i++) {
                Candidates_Scores.add(new MyComparableObject(Candidates.get(i), Scores.get(i)));
            }
            Collections.sort(Candidates_Scores);
            //System.out.println(Candidates_Scores.get(0).getName());
            for(MyComparableObject item:Candidates_Scores) {
                pos = item.getName();
                percent = String.valueOf(item.getScore());
                return2DList.add(new String[]{pos,percent});
            }
        }
        return return2DList;
    }
    private final double lat;
    private final double lon;
    private final String ApiID;
    private ArrayList<String[]> pos_info;
    public LocationCallable(double latitude,double longitude,String apiID) {
        lat=latitude;
        lon=longitude;
        ApiID=apiID;
    }

    @Override
    public ArrayList<String[]> call() throws InterruptedException {
        Thread thread=new Thread(() -> {
            Map<String,String> headers = new HashMap<>();
            headers.put("X-Example-Header","Example-Value");
            try {
                String GeoResult =get_pos("https://map.yahooapis.jp/placeinfo/V1/get?lat="+lat+"&lon="+lon+"&appid="+ApiID+"&output=json","UTF-8",headers);
                pos_info = get_pos_info(GeoResult);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        thread.join();
        return pos_info;
    }
}
