package com;

import com.google.gson.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.HttpResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RequestHandler {
    private final static String MESSAGE_TYPE = "message_new";
    //private final static String OK_BODY = "ok";

    private final BotRequestHandler botRequestHandler;
    private final Gson gson;

    RequestHandler(BotRequestHandler handler) {
        this.botRequestHandler = handler;
        this.gson = new GsonBuilder().create();
    }


    public void handle(String url, String key, int ts_int) throws IOException {
//{$server}?act=a_check&key={$key}&ts={$ts}&wait=25
        String ts = ts_int + "";
        HttpClient client = HttpClientBuilder.create().build();
        while (true) {
            //String url = server; // + "?act=a_check&key=" + key + "&ts" + ts + "&wait=50";
            HttpPost post = new HttpPost(url);
            post.setHeader("Referer", "https://vk.com");
            post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("act", "a_check"));
            urlParameters.add(new BasicNameValuePair("key", key));
            urlParameters.add(new BasicNameValuePair("ts", ts));
            urlParameters.add(new BasicNameValuePair("wait", "30"));
            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            JsonObject requestJson = gson.fromJson(rd, JsonObject.class);
            ts = requestJson.get("ts").getAsString();
            JsonArray updates = requestJson.get("updates").getAsJsonArray();
            /*
[{"type":"message_new","object":{"date":1541848293,"from_id":81792031,"id":0,"out":0,"peer_id":2000000001,"text":".","conversation_message_id":94,"fwd_messages":[],"important":false,"random_id":0,"attachments":[],"is_hidden":false},"group_id":172829412}]
массив
             */
            for (JsonElement i : updates) {
                JsonObject jsonObject = i.getAsJsonObject();
                String type = jsonObject.get("type").getAsString();
                if (type == null || type.isEmpty()) throw new IllegalArgumentException("No type in json");
                switch (type) {
                    case MESSAGE_TYPE:
                        JsonObject object = jsonObject.getAsJsonObject("object");
/*
json выглядит так
{"date":1541844781,"from_id":81792031,"id":7,"out":0,"peer_id":81792031,"text":"dfsf","conversation_message_id":7,"fwd_messages":[],"important":false,"random_id":0,"attachments":[],"is_hidden":false}
*/
                        //int userId = object.getAsJsonPrimitive("from_id").getAsInt();
                        //достеём айди пользователя
                        //int peerId = object.getAsJsonPrimitive("peer_id").getAsInt();
                        //если меньше 2ккк - айди пользователя, если больше - айди беседы + 2ккк
                        botRequestHandler.handle(object);
                        break;
                    default:
                        break;
                }
            }
        }

    }
}