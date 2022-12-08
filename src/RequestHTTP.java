import java.io.*;
import java.net.*;

public class RequestHTTP {


    public static String Request(Query query) throws IOException {

        //Creating the  GET request
        URL url = new URL(query.getQuestionUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        //testing if the request is successful or not
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();

            //we read the input while everything is read from the input stream
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            //close the connection and return the request response 
            in.close();
            return content.toString();

        } else 
            return null;
    }
}
