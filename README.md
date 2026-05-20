Vanilla Web Server

Simple practice project: a basic HTTP web server built in Java.

It handles HTTP **GET** requests and serves static files from a local `www/` directory.

The server:
- Returns `200 OK` for existing files
- Returns `404 Not Found` with a default page if a file is missing
- Serves HTML, images, and other static resources
- Uses raw sockets and manual HTTP response formatting

Key Concepts

- Client-server architecture
- HTTP request/response structure
- Parsing GET requests
- Serving static files from a document root
- Basic HTTP headers (`Content-Type`, `Content-Length`)
- Multithreaded request handling (if applicable in your version)

Run
java -jar target/server.jar
Start the server, then open in a browser:
http://localhost:8095/

Static files are served from:
src/main/www/
