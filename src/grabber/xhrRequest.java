package grabber;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class xhrRequest {

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) {
        xhrRequest http = new xhrRequest();
        try {
            System.out.println(http.sendPost("https://creativenovels.com/wp-admin/admin-ajax.php", "action=crn_chapter_list&view_id=9311"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> tapReadGetChapterList(int bookNumber) {
        xhrRequest http = new xhrRequest();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(http.sendPost("https://www.tapread.com/book/contents", "bookId=" + bookNumber));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject results = (JSONObject) jsonObject.get("result");
            JSONArray chapterObjects = (JSONArray) results.get("chapterList");
            Map<String, String> chapterMap = new LinkedHashMap<>();
            for (Object o : chapterObjects) {
                JSONObject slide = (JSONObject) o;
                String chapterId = String.valueOf(slide.get("chapterId"));
                String chapterName = String.valueOf(slide.get("chapterName"));
                String chapterLocked = String.valueOf(slide.get("lock"));
                if (chapterLocked.equals("0")) chapterMap.put(chapterId, chapterName);
            }
            return chapterMap;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String tapReadGetChapterContent(String paramenter) {
        xhrRequest http = new xhrRequest();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(http.sendPost("https://www.tapread.com/book/chapter", paramenter));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject results = (JSONObject) jsonObject.get("result");
            String content = (String) results.get("content");
            return content;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, String> webnovelGetChapterList(String url) {
        xhrRequest http = new xhrRequest();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(http.sendGet(url));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject data = (JSONObject) jsonObject.get("data");
            JSONArray volumeItems = (JSONArray) data.get("volumeItems");
            Map<String, String> chapterMap = new LinkedHashMap<>();

            for (Object o : volumeItems) {
                JSONObject chapterItem = (JSONObject) o;
                JSONArray chapterItems = (JSONArray) chapterItem.get("chapterItems");
                for (Object a : chapterItems) {
                    JSONObject slide = (JSONObject) a;
                    String chapterId = String.valueOf(slide.get("id"));
                    // Crude hotfix
                    String chapterName = String.valueOf(slide.get("name")).replaceAll("â€™", "\'");
                    String isVip = String.valueOf(slide.get("isVip"));
                    if (isVip.equals("0")) {
                        chapterMap.put(chapterId, chapterName);
                    }
                }
            }
            return chapterMap;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String wattpadGetChapterTextURL(String chapterId) {
        xhrRequest http = new xhrRequest();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(http.sendGet("https://www.wattpad.com/v4/parts/" + chapterId + "?fields=text_url"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject results = (JSONObject) jsonObject.get("text_url");
            return (String) results.get("text");
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // HTTP POST request
    public String sendPost(String url, String paramenter) throws IOException {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Accept-Charset", "UTF-8");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(paramenter);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    // HTTP GET request
    private String sendGet(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}

