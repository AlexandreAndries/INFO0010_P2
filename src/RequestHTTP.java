import java.io.*;
import java.net.*;

public class RequestHTTP {

    public static String Request(Query query) throws IOException {

        URL url = new URL(query.getQuestionUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        //int status = connection.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        byte[] bytesResponse = new byte[content.length()];
        for (int i = 0; i < content.length(); i++) {
            bytesResponse[i] = (byte) content.charAt(i);
        }
        System.out.println(bytesResponse.length);

        return content.toString();
    }
}
