package eubr.atmosphere.tma.execute.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eubr.atmosphere.tma.entity.qualitymodel.ActionRule;
import eubr.atmosphere.tma.entity.qualitymodel.Actuator;
import eubr.atmosphere.tma.entity.qualitymodel.Configuration;

public class RestServices {

    private static int messageId = 0;

    private static final Logger LOGGER = LoggerFactory.getLogger(RestServices.class);

    public static void requestRestService(Actuator actuator, ActionRule action) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        // Reference: https://www.baeldung.com/java-http-request
        String jsonPayload = getJsonObject(action);
        LOGGER.info(jsonPayload);

        byte[] pubKeyBytes = Base64.getDecoder().decode(actuator.getPubKey());
        PublicKey pubKey = KeyManager.getPublicKey(pubKeyBytes);

        byte[] payload = encryptMessage(actuator, jsonPayload, pubKey);

        // TODO: It still needs to add the action
        URL url = new URL(actuator.getAddress() + "act");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.write(payload);
        out.flush();
        out.close();

        String privateKeyPath = PropertiesManager.getInstance().getProperty("privateKeyPath");
        PrivateKey privKeyExecutor = KeyManager.getPrivateKey(privateKeyPath);
        handleResponse(con, pubKey, privKeyExecutor);
        con.disconnect();
    }

    private static void handleResponse(HttpURLConnection con, PublicKey pubKey, PrivateKey privKeyExecutor)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        int i = 0;
        String decryptedMessage = "";
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
            //LOGGER.info(inputLine);
            if (i == 0) {
                // data
                byte[] encMessage = Base64.getDecoder().decode(inputLine);
                //LOGGER.info("byteArray.length: {} ", encMessage.length);
                decryptedMessage = KeyManager.decrypt(encMessage, privKeyExecutor);
            }
            if (i == 1) {
                // signature
                //LOGGER.info("signature: {}", inputLine);
                LOGGER.info("valid? {}", SignatureManager.verifySignature(
                        decryptedMessage.getBytes(), Base64.getDecoder().decode(inputLine), pubKey));
            }
            i++;
        }
        in.close();
    }

    private static String getJsonObject(ActionRule actionRule) {
        JsonObject jsonObject = new JsonObject();
        JsonArray configurationJson = new JsonArray();
        for (Configuration config : actionRule.getConfigurations()) {
            JsonObject configJson = new JsonObject();
            configJson.addProperty("keyName", config.getKeyName());
            configJson.addProperty("value", config.getValue());
            configurationJson.add(configJson);
        }

        jsonObject.addProperty("resourceId", actionRule.getResource().getResourceId());
        jsonObject.addProperty("messageId", messageId++);
        jsonObject.addProperty("timestamp", Calendar.getInstance().getTimeInMillis());
        jsonObject.addProperty("action", actionRule.getActionName());
        jsonObject.add("configuration", configurationJson);
        return jsonObject.toString();
    }

    private static byte[] encryptMessage(Actuator actuator, String message, PublicKey pubKey) {
        byte[] encryptedMessage = KeyManager.encrypt(message, pubKey);
        //LOGGER.info(encryptedMessage.toString());
        return encryptedMessage;
    }
}
