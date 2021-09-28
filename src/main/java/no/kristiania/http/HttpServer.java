package no.kristiania.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer {

    private final ServerSocket serverSocket;
    private Path rootDirectory;
    private List<String> roles = new ArrayList<>();
    private List<Person>people;

    public HttpServer(int serverPort) throws IOException {
        serverSocket = new ServerSocket(serverPort);

        new Thread(this::handleClients).start();
    }
    private void handleClients(){
        while(true) {
            handleClient();
        }

    }

    private void handleClient() {
        try {
            Socket clientSocket = serverSocket.accept();

            String[] requestLine = HttpMessage.readLine(clientSocket).split(" ");
            String requestTarget = requestLine[1];

            int questionPos = requestTarget.indexOf('?');
            String fileTarget;
            String query = null;
            if (questionPos != -1) {
                fileTarget = requestTarget.substring(0, questionPos);
                query = requestTarget.substring(questionPos + 1);
            } else {
                fileTarget = requestTarget;
            }

            if (fileTarget.equals("/hello")) {
                String yourName = "world";
                if (query != null) {

                    // "parser" ut queryparameterene --> henter dem ut
                    // splitter

                    Map<String, String> queryMap = new HashMap<>();
                    for (String queryParameter : query.split("&")) {
                        int equalPos = queryParameter.indexOf('=');
                        String parameterName = queryParameter.substring(0, equalPos);
                        String parameterValue = queryParameter.substring(equalPos + 1);
                        queryMap.put(parameterName, parameterValue);

                    }

                    yourName = queryMap.get("lastName") + ", " + queryMap.get("firstName");
                }
                String responseText = "<p>Hello " + yourName + "</p>";

                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: " + responseText.length() + "\r\n" +
                        "Content-Type: " + "text/html" + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        responseText;
                clientSocket.getOutputStream().write(response.getBytes());

            }else if (fileTarget.equals("/api/roleOptions"));{
                String responseText = " ";

                int value = 1;


                for (String role: roles){
                    responseText += "<option value=" + (value++) + ">" + role + "<option>";
                }

                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: " + responseText.length() + "\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        responseText;
                clientSocket.getOutputStream().write(response.getBytes());

            } else {

                if (rootDirectory != null && Files.exists(rootDirectory.resolve(fileTarget.substring(1)))) {
                    String responseText = Files.readString(rootDirectory.resolve(fileTarget.substring(1)));

                    String contentType = "text/plain";
                    if (requestTarget.endsWith(".html")) {
                        contentType = "text/html";
                    }
                    String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: " + responseText.length() + "\r\n" +
                            "Content-Type: " + contentType + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n" +
                            responseText;
                    clientSocket.getOutputStream().write(response.getBytes());
                    return;
                }


                String responseText = "File not found: " + requestTarget;

                String response = "HTTP/1.1 404 Not found\r\n" +
                        "Content-Length: " + responseText.length() + "\r\n" +
                        "\r\n" +
                        responseText;
                clientSocket.getOutputStream().write(response.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer(1962);

        HttpServer.setRoles(List.of("Student", "Teaching Assistent", "Teatcher"));
        //linker til denne siden:
        //http://localhost:1962/index.html
        httpServer.setRoot(Paths.get("."));
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void setRoot(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public static void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<Person> getPeople() {
        return people;
    }
}
