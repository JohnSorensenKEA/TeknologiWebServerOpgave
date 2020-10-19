import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class Main {

    public static void main(String[] args){
        boolean go_on = true;
        String httpRequestLine;
        ServerSocket welcomeSocket = null;

        System.out.println("Starter serveren, skal lige finde nøglen først");
        try{
             welcomeSocket = new ServerSocket(8081);
            System.out.println("Fandt den!");
            System.out.println("-------------------");
            System.out.println();
        }
        catch (IOException e){
            System.out.println("Beklager jeg kan ikke finde min nøgle.");
            System.exit(69);
        }

        while (go_on){
            try{
                boolean wrongRequest = false;
                
                //BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Så nu venter vi bare på at nogen ønsker noget...");

                Socket connectionSocket = welcomeSocket.accept();
                System.out.println("Der er kunder i butikken!");
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                httpRequestLine = inFromClient.readLine();
                StringTokenizer tokenizer = new StringTokenizer(httpRequestLine);
                String requestMethod = tokenizer.nextToken();

                if(!requestMethod.equals("GET")){
                    if(requestMethod.equals("POST")
                            || requestMethod.equals("HEAD")
                            || requestMethod.equals("PUT")
                            || requestMethod.equals("DELETE")
                            || requestMethod.equals("TRACE")
                            || requestMethod.equals("OPTIONS")
                            || requestMethod.equals("CONNECT")
                            || requestMethod.equals("PATCH")){

                        wrongRequest = true;
                    }
                    else {
                        wrongRequest = false;
                    }
                }

                String request = tokenizer.nextToken();

                System.out.println("Kunde efterspørgsel: "+httpRequestLine);
                if(wrongRequest){
                    request = "/error400.html";
                }
                else if(!requestMethod.equals("GET")){
                    request = "/error404.html";
                }
                else if(request.equals("/")){
                    request = "/index.html";
                }

                String path = "src/resources";
                File file = new File(path + request);
                if (!file.exists()){
                    file = new File(path + "/error404.html");
                }


                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", new Locale("en","DK"));
                Date date = new Date();
                String currentDate = sdf.format(date);

                String[] str = file.getName().split("\\.");
                String fileType = str[str.length - 1];
                long fileSize = file.length();

                FileInputStream fromFile = new FileInputStream(file);
                outToClient.writeBytes("HTTP/1.1 200 ok"+ "\n");
                outToClient.writeBytes("Date: " + currentDate + "\n");
                outToClient.writeBytes("Content-Length: "+fileSize+ "\n");

                if(fileType.equals("html")) {
                    outToClient.writeBytes("Content-Type: text/html; charset=ISO-8859-1"+ "\n");
                }
                else if(fileType.equals("jpg")){
                    outToClient.writeBytes("Content-Type: image/jpeg"+ "\n");
                }
                else if(fileType.equals("gif")){
                    outToClient.writeBytes("Content-Type: image/gif"+ "\n");
                }

                outToClient.writeBytes("\n");

                int packageX = 30;

                long numberOfBytes = fileSize/packageX;
                long leftOverBits = fileSize%packageX;

                byte[] b = new byte[packageX];
                for(long l = 0; l < numberOfBytes; l++){
                    fromFile.read(b);
                    outToClient.write(b);
                }

                int byteX;
                boolean cont;
                if(leftOverBits > 0){
                    cont = true;
                }
                else{
                    cont = false;
                }
                while(cont){
                    byteX = fromFile.read();
                    if(byteX == -1){
                        cont = false;
                    }
                    else {
                        outToClient.writeByte((byte) byteX);
                    }
                }
                connectionSocket.close();
                String[] str2 = file.getName().split("/");
                String str3 = str2[str2.length - 1];
                System.out.println("Kunde serveret     : " + str3);
                System.out.println();
            }
            catch (IOException e){
                System.out.println("Ups, det ser ud som om noget gik galt.");
            }

        }
    }
}