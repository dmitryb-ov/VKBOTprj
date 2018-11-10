package com;

import com.google.gson.*;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class BotRequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BotRequestHandler.class);
    private final VkApiClient apiClient;
    private final int GROUP_IDENTIFICATOR = 2000000000;

    private final Gson gson;
    private final GroupActor actor;
    private final Random random = new Random();

    BotRequestHandler(VkApiClient apiClient, GroupActor actor) {
        this.apiClient = apiClient;
        this.actor = actor;
        this.gson = new GsonBuilder().create();
    }

    void handle(int userId, int peerId) {
        try {
            if (peerId < GROUP_IDENTIFICATOR) {
                apiClient.messages().send(actor).message("Привет здарова").userId(userId).randomId(random.nextInt()).execute();
            } else {
                JsonObject user = gson.fromJson(apiClient.users().get(actor).userIds(userId+"").executeAsString(), JsonObject.class);
                //{"response":[{"id":81792031,"first_name":"Руслан","last_name":"Зайнуллин"}]} - массив
                JsonObject userInfo = user.get("response").getAsJsonArray().get(0).getAsJsonObject();
                String firstName = userInfo.get("first_name").getAsString();
                int chatId = peerId - GROUP_IDENTIFICATOR;
                apiClient.messages().send(actor).message("*id" + userId + " (" + firstName + "), привет").chatId(chatId).randomId(random.nextInt()).execute();
            }
        } catch (ApiException e) {
            LOG.error("INVALID REQUEST", e);
        } catch (ClientException e) {
            LOG.error("NETWORK ERROR", e);
        }
    }
}
