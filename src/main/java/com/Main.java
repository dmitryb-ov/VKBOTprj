package com;

import com.vk.api.sdk.client.ApiRequest;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import com.vk.api.sdk.queries.groups.GroupsGetLongPollServerQuery;
import org.eclipse.jetty.server.Server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    private final static String PROPERTY_NAME = "config.properties";
    public static void main(String[] args) throws Exception {
        Properties properties = readProperties();
        HttpTransportClient client = new HttpTransportClient();
        VkApiClient apiClient = new VkApiClient(client);

        GroupActor actor = initVkApi(apiClient, properties);

        GroupsGetLongPollServerQuery query = apiClient.groups().getLongPollServer(actor);
        GetLongPollServerResponse data = query.execute();
        //System.out.println(data.getServer() + data.getKey() + data.getTs());
        BotRequestHandler botHandler = new BotRequestHandler(apiClient, actor);
        RequestHandler handler = new RequestHandler(botHandler);
        handler.handle(data.getServer(), data.getKey(), data.getTs());
/*
        Server server = new Server(8080);



        server.setHandler(new RequestHandler(botHandler));

        server.start();
        server.join();
*/
    }

    private static GroupActor initVkApi(VkApiClient apiClient, Properties properties) throws ClientException, ApiException {

        String token = properties.getProperty("token");
        int groupId = Integer.parseInt(properties.getProperty("groupId"));
        if (groupId == 0 || token == null) throw new RuntimeException("Params are not set");
        GroupActor actor = new GroupActor(groupId, token);


        return actor;
    }

    private static Properties readProperties() throws FileNotFoundException {
        InputStream in = Main.class.getClassLoader().getResourceAsStream(PROPERTY_NAME);

        if (in == null) {
            throw new FileNotFoundException("can't find a property file \"" + PROPERTY_NAME + "\"");
        }
        try {
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
