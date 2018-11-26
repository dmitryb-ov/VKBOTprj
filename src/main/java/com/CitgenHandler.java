package com;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.queries.users.UserField;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.imageio.ImageIO;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Random;

public class CitgenHandler extends Applet implements FunctionHandler {
    //final String IMAGE_NAME = "citate_background.png";
    private final int GROUP_IDENTIFICATOR = 2000000000;

    private final int WIDTH = 640;
    private final int HEIGHT = 400;
    private final String FONT = "DejaVuSans.ttf"; //"Arial Black" ArialBlack.ttf?
    private final String TITLE = "Цитаты великих людей:";
    private final int TITLE_X_POS = 20;
    private final int TITLE_Y_POS = 61;
    private final int TEXT_X_POS = 220;
    private final String COPYLEFT = "(c)";
    private final String LION = "lion.png";
    private String SAVE_IMAGE_NAME = "citgen_image.png";
    private int TEXT_Y_POS(int height, int linesCount){ return (int) Math.round(Math.max(height/2-20*(0.75*linesCount), 80)) + 16;};
    private final int AUTH_POS(int width, int addSym, int length){ return width-(11*(4+addSym+length));}

    //JsonObject userInfo;
    JsonObject object;
    Gson gson;
    VkApiClient apiClient;
    GroupActor actor;
    private final Random random = new Random();
    //Image img;
    // Graphics graphics;
    public CitgenHandler(VkApiClient apiClient, GroupActor actor, JsonObject object) {
        //this.userInfo = userInfo;
        this.object = object;
        this.gson = new Gson();
        this.apiClient = apiClient;
        this.actor = actor;
       // this.img = ImageIO.read(new File(IMAGE_NAME));;
        // graphics = img.getGraphics()
    }
    private JsonObject getUser(int userId) throws ClientException {
        JsonObject user = gson.fromJson(apiClient.users().get(actor).userIds(userId+"").fields(UserField.PHOTO_200).executeAsString(), JsonObject.class);
        //{"response":[{"id":81792031,"first_name":"Руслан","last_name":"Зайнуллин"}]} - массив
        JsonObject userInfo = user.get("response").getAsJsonArray().get(0).getAsJsonObject();
        return userInfo;
    }

    private void sendMessage(String message, String filelink) throws ClientException, ApiException {
        int peerId = object.getAsJsonPrimitive("peer_id").getAsInt();
        if (peerId < GROUP_IDENTIFICATOR) {
            apiClient.messages().send(actor).message(message).attachment(filelink).userId(peerId).randomId(random.nextInt()).execute();
        } else {
            int chatId = peerId - GROUP_IDENTIFICATOR;
            apiClient.messages().send(actor).message(message).attachment(filelink).chatId(chatId).randomId(random.nextInt()).execute();
        }
    }

    @Override
    public void handle() throws IOException, ClientException, ApiException {
        sendMessage("принято","");

        StringBuilder authorNameSB = new StringBuilder();
        String[] msgWords = object.get("text").getAsString().split(" ");
        for (int i = 1; i < msgWords.length; i++) {
            authorNameSB.append(msgWords[i]).append(" ");
        }
        if (authorNameSB.length() != 0)
        authorNameSB.deleteCharAt(authorNameSB.length()-1);

        StringBuilder cit = new StringBuilder();
        JsonArray fwdMessages = object.get("fwd_messages").getAsJsonArray();
        for (JsonElement i: fwdMessages) {
            JsonObject message = i.getAsJsonObject();
            cit.append(message.get("text").getAsString()).append("\n");
        }
        int linesCount = cit.toString().split("\n").length;

        int authorId = object.getAsJsonPrimitive("from_id").getAsInt();
        int personId = fwdMessages.get(0).getAsJsonObject().getAsJsonPrimitive("from_id").getAsInt();
        JsonObject userInfo = getUser(personId);

        String authorName = authorNameSB.toString();
        if (authorName.equals("")) {
            authorName = userInfo.get("first_name").getAsString() + " " + userInfo.get("last_name").getAsString();
        }

        String add = authorId == personId ? "Самоцитген" : "";

        String avalink = userInfo.get("photo_200").getAsString();
        BufferedImage ava = ImageIO.read(new URL(avalink));
        //BufferedImage ava = authorId == personId ? ImageIO.read(new URL(avalink)): ImageIO.read(new File(LION));

        File image = createImg(ava, cit.toString(), linesCount, authorName, add);
        Photo photoData = upload(image);
        String photoId = "photo"+photoData.getOwnerId()+"_"+photoData.getId();
        //photo3424324_325246421 тип того

        sendMessage("", photoId);

        image.delete();
    }

    private File createImg(BufferedImage ava, String text, int linesCount, String author, String add) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font(FONT, Font.PLAIN, 43));
        graphics.drawString(TITLE, TITLE_X_POS, TITLE_Y_POS);
        graphics.drawImage(ava, 12, 80, 200, ava.getHeight(), null);
        graphics.setFont(new Font(FONT, Font.PLAIN, 18));
        drawString(graphics, text, TEXT_X_POS,  TEXT_Y_POS(HEIGHT, linesCount));
        graphics.drawString(COPYLEFT + add+ " " + author, AUTH_POS(WIDTH, add.length(), author.length()), 354+16);
        File image = new File(SAVE_IMAGE_NAME);
        ImageIO.write(bufferedImage, "png", image);
        return image;
    }

    private Photo upload(File photo) throws ClientException, ApiException, IOException {
        String url = apiClient.photos().getMessagesUploadServer(actor).peerId(object.getAsJsonPrimitive("peer_id").getAsInt()).execute().getUploadUrl();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
// This attaches the file to the POST:
        builder.addBinaryBody(
                "file",
                new FileInputStream(photo),
                ContentType.MULTIPART_FORM_DATA,
                photo.getName());

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        JsonObject requestJson = gson.fromJson(rd, JsonObject.class);


        List<Photo> photoList = apiClient.photos().saveMessagesPhoto(actor,requestJson.get("photo").getAsString()).server(requestJson.getAsJsonPrimitive("server").getAsInt()).hash(requestJson.get("hash").getAsString()).execute();

       return photoList.get(0);
    }

    public void drawString(Graphics2D graphics, String text, int x, int y) {
        for (String line : text.split("\n")){
            graphics.drawString(line, x, y);
            y += graphics.getFontMetrics().getHeight();
        }
    }



}
