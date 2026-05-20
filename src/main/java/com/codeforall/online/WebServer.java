package com.codeforall.online;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class WebServer {

  private ServerSocket serverSocket;
  private Socket clientSocket;
  private int PORT_NUMBER = 8095;
  private BufferedReader in;
  private OutputStream out;
  private final String resources = "src/main/www";

  public void start() throws IOException {

    serverSocket = new ServerSocket(PORT_NUMBER);
    System.out.println("Server created at port " + PORT_NUMBER);
    System.out.println("Listening at address: " + serverSocket.getInetAddress() + serverSocket.getLocalPort());

    while (true) {
      try {
        clientSocket = serverSocket.accept();
        System.out.println("Client connect at: " + clientSocket.getInetAddress() + clientSocket.getLocalPort());
        setupStreams();
        respond();

      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        closeStreams();
      }
    }
  }

  public void setupStreams() {

    try {
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      out = new BufferedOutputStream(clientSocket.getOutputStream());

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  public void closeStreams() throws IOException {
    if (in != null) {
      in.close();
    }
    if (out != null) {
      out.close();
    }
    if (clientSocket != null) {
      clientSocket.close();
    }
  }

  public String readHttpHeader() {
    String request;
    StringBuilder httpRequest = new StringBuilder();

    try {
      while ((request = in.readLine()) != null && !request.isEmpty()) {
        httpRequest.append(request).append("\r\n");
      }
      return httpRequest.toString();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getContentType(String fileName) {

    if (fileName.endsWith(".html")) {
      return "text/html; charset=UTF-8";
    } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
      return "image/jpeg";
    } else if (fileName.endsWith(".png")) {
      return "image/png";
    } else if (fileName.endsWith(".ico")) {
      return "image/x-icon";
    }
    return null;
  }

  public void respond() throws IOException {

    String httpRequest = readHttpHeader();

    if (httpRequest.isEmpty()) {
      return;
    }

    String[] splitHeader = httpRequest.split("\r\n");
    String verbPathVersion = splitHeader[0];
    String verb = verbPathVersion.split(" ")[0];
    String path = verbPathVersion.split(" ")[1];
    String version = verbPathVersion.split(" ")[2];

    if (path.equals("/")) {
      path = "/index.html";
    }

    File requestedFile = new File(resources + path);

    if (!verb.equals("GET")) {
      byte[] body = "<h1>405 - Method Not Allowed</h1><p>Only GET is supported.</p>"
          .getBytes(StandardCharsets.UTF_8);

      String headers = "HTTP/1.1 405 Method Not Allowed\r\n" +
          "Content-Type: text/html; charset=UTF-8\r\n" +
          "Content-Length: " + body.length + "\r\n" +
          "Allow: GET\r\n" +
          "\r\n";

      out.write(headers.getBytes(StandardCharsets.UTF_8));
      out.write(body);
      out.flush();
      return;
    }

    if (!requestedFile.exists() || !requestedFile.isFile()) {

      File fileNotFound = new File(resources + "/404.html");
      byte[] content = Files.readAllBytes(fileNotFound.toPath());

      String headers = "HTTP/1.1 404 Not Found\r\n" +
          "Content-Type: text/html; charset=UTF-8\r\n" +
          "Content-Length: " + content.length + "\r\n" +
          "\r\n";

      out.write(headers.getBytes(StandardCharsets.UTF_8));
      out.write(content);
      out.flush();
      return;
    }

    byte[] fileContent = Files.readAllBytes(requestedFile.toPath());
    String contentType = getContentType(requestedFile.getName());

    String headers = "HTTP/1.1 200 Document Follows\r\n" +
        "Content-Type: " + contentType + "\r\n" +
        "Content-Length: " + fileContent.length + "\r\n" +
        "\r\n";

    out.write(headers.getBytes(StandardCharsets.UTF_8));
    out.write(fileContent);
    out.flush();
  }

  public static void main(String[] args) {
    WebServer webServer = new WebServer();
    try {
      webServer.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

}
