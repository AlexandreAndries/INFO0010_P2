package DnsTunneling;

/**
 * \file RequestHTTP.java
 *
 *
 * \brief INFO0010 Projet 2
 * \author Andries Alexandre s196948
 * \author Rotheudt Thomas s191895
 * \version 0.1
 * \date 11/12/2022
 *
 */

 /** Class description :
  *
  * RequestHTTP class is used to represent
  *
  */
import java.io.*;
import java.net.*;

// class RequestHTTP
public class RequestHTTP {
    /*------------------------------------------------------------------------*/
    /*- Constructor ----------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public static String Request(Query query) throws IOException {

        //Create the  GET request
        URL url = new URL(query.getQuestionUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Test if the request is successful or not
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();

            // Read the input while everything is read from the input stream
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Close the connection and return the request response
            in.close();
            return content.toString();

        } else
            return null;
    }
}
/*----------------------------------------------------------------------------*/
/*- END OF CLASS -------------------------------------------------------------*/
/*----------------------------------------------------------------------------*/
