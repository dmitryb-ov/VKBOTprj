package com;

import com.google.gson.*;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.queries.users.UserField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.http.HttpClient;

import java.io.IOException;
import java.util.Random;

public class BotRequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BotRequestHandler.class);
    private final VkApiClient apiClient;
    private final int GROUP_IDENTIFICATOR = 2000000000;

    private final Gson gson;
    private final GroupActor actor;
    //private final Random random = new Random();

    BotRequestHandler(VkApiClient apiClient, GroupActor actor) {
        this.apiClient = apiClient;
        this.actor = actor;
        this.gson = new GsonBuilder().create();
    }

    /*
    private JsonObject getUser(int userId) throws ClientException {
        JsonObject user = gson.fromJson(apiClient.users().get(actor).userIds(userId+"").executeAsString(), JsonObject.class);
        //{"response":[{"id":81792031,"first_name":"Руслан","last_name":"Зайнуллин"}]} - массив
        JsonObject userInfo = user.get("response").getAsJsonArray().get(0).getAsJsonObject();
        return userInfo;
    }
*/


    void handle(JsonObject object) {
        try {
            String[] msgWords = object.get("text").getAsString().split(" ");
            if (msgWords[0].equals( "цитген")) {
                FunctionHandler h = new CitgenHandler(apiClient, actor, object);
                h.handle();
            }

                //String firstName = userInfo.get("first_name").getAsString();
                //apiClient.messages().send(actor).message("*id" + userId + " (" + firstName + "), привет").chatId(chatId).randomId(random.nextInt()).execute();
                //apiClient.messages().send(actor).message(object.toString()).chatId(chatId).randomId(random.nextInt()).execute();


        } catch (ApiException e) {
            LOG.error("INVALID REQUEST", e);
        } catch (ClientException e) {
            LOG.error("NETWORK ERROR", e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
