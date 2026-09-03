package controller;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.CredentialManager;
import model.LoginService;
import view.HtmlView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WebServer {
    private static final int PORT = 3002;
    private static final Set<String> activeSessions = new HashSet<>();

    public static void start() throws IOException {
        LoginService loginService = new CredentialManager();

        if (!loginService.credentialsExist()) {
            System.out.println("Error: Web view cannot start because no user accounts exist.");
            System.out.println("Run ./gradlew run first and create credentials.");
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        HtmlView htmlView = new HtmlView();

        server.createContext("/", exchange -> {
            if (!isLoggedIn(exchange)) {
                redirect(exchange, "/login");
                return;
            }

            sendHtml(exchange, htmlView.homePage());
        });

        server.createContext("/login", exchange -> {
            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendHtml(exchange, loginPage(""));
                return;
            }

            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Map<String, String> form = readForm(exchange);

                String username = form.getOrDefault("username", "");
                String password = form.getOrDefault("password", "");

                try {
                    if (loginService.login(username, password)) {
                        ProgramControl.setCurrentUsername(username);

                        String sessionId = UUID.randomUUID().toString();
                        activeSessions.add(sessionId);

                        Headers headers = exchange.getResponseHeaders();
                        headers.add("Set-Cookie", "SESSION=" + sessionId + "; Path=/; HttpOnly");

                        redirect(exchange, "/");
                    } else {
                        sendHtml(exchange, loginPage("Invalid username or password."));
                    }
                } catch (IOException e) {
                    sendHtml(exchange, loginPage("Error checking credentials."));
                }

                return;
            }

            sendHtml(exchange, loginPage("Unsupported request method."));
        });

        server.createContext("/agents", exchange -> {
            if (!requireLogin(exchange)) {
                return;
            }

            String number = getQueryParam(exchange, "number");

            if (number != null) {
                String detailText = ProgramControl.displayAgent(number);
                String body = "<pre>" + escapeHtml(detailText) + "</pre>" +
                        "<p><a href=\"/agents\">Back to Agents</a></p>" +
                        "<p><a href=\"/\">Back to Home</a></p>";

                sendHtml(exchange, htmlView.wrapPage("Agent Details", body));
            } else {
                sendHtml(exchange, htmlView.linkedListPage(
                        "Agents",
                        ProgramControl.listAgents(),
                        "/agents"
                ));
            }
        });

        server.createContext("/facilities", exchange -> {
            if (!requireLogin(exchange)) {
                return;
            }

            String number = getQueryParam(exchange, "number");

            if (number != null) {
                String detailText = ProgramControl.displayFacility(number);
                String body = "<pre>" + escapeHtml(detailText) + "</pre>" +
                        "<p><a href=\"/facilities\">Back to Facilities</a></p>" +
                        "<p><a href=\"/\">Back to Home</a></p>";

                sendHtml(exchange, htmlView.wrapPage("Facility Details", body));
            } else {
                sendHtml(exchange, htmlView.linkedListPage(
                        "Facilities",
                        ProgramControl.listFacilities(),
                        "/facilities"
                ));
            }
        });

        server.createContext("/missions", exchange -> {
            if (!requireLogin(exchange)) {
                return;
            }

            String number = getQueryParam(exchange, "number");

            if (number != null) {
                String detailText = ProgramControl.displayMission(number);
                String body = "<pre>" + escapeHtml(detailText) + "</pre>" +
                        "<p><a href=\"/missions\">Back to Missions</a></p>" +
                        "<p><a href=\"/\">Back to Home</a></p>";

                sendHtml(exchange, htmlView.wrapPage("Mission Details", body));
            } else {
                sendHtml(exchange, htmlView.linkedListPage(
                        "Missions",
                        ProgramControl.listMissions(),
                        "/missions"
                ));
            }
        });

        server.createContext("/briefs", exchange -> {
            if (!requireLogin(exchange)) {
                return;
            }

            String number = getQueryParam(exchange, "number");

            if (number != null) {
                String detailText = ProgramControl.displayBrief(number);
                String body = "<pre>" + escapeHtml(detailText) + "</pre>" +
                        "<p><a href=\"/briefs\">Back to Briefs</a></p>" +
                        "<p><a href=\"/\">Back to Home</a></p>";

                sendHtml(exchange, htmlView.wrapPage("Brief Details", body));
            } else {
                sendHtml(exchange, htmlView.linkedListPage(
                        "Briefs",
                        ProgramControl.listBriefs(),
                        "/briefs"
                ));
            }
        });

        server.createContext("/audit", exchange -> {
            if (!requireLogin(exchange)) {
                return;
            }

            sendHtml(exchange, htmlView.auditPage(ProgramControl.listAuditLogs()));
        });

        server.setExecutor(null);
        server.start();

        System.out.println("Web server started.");
        System.out.println("Open http://localhost:3002/");
    }//end of start

    private static boolean requireLogin(HttpExchange exchange) throws IOException {
        if (!isLoggedIn(exchange)) {
            redirect(exchange, "/login");
            return false;
        }

        return true;
    }

    private static boolean isLoggedIn(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");

        if (cookieHeader == null) {
            return false;
        }

        String[] cookies = cookieHeader.split(";");

        for (String cookie : cookies) {
            String trimmed = cookie.trim();

            if (trimmed.startsWith("SESSION=")) {
                String sessionId = trimmed.substring("SESSION=".length());
                return activeSessions.contains(sessionId);
            }
        }

        return false;
    }

    private static String loginPage(String errorMessage) {
        String errorHtml = "";

        if (errorMessage != null && !errorMessage.isBlank()) {
            errorHtml = "<p>" + escapeHtml(errorMessage) + "</p>";
        }

        return "<html><body>" +
                "<h1>Top Secret Login</h1>" +
                errorHtml +
                "<form method=\"post\" action=\"/login\">" +
                "<p>Username: <input type=\"text\" name=\"username\"></p>" +
                "<p>Password: <input type=\"password\" name=\"password\"></p>" +
                "<p><button type=\"submit\">Log in</button></p>" +
                "</form>" +
                "</body></html>";
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = new HashMap<>();

        if (body.isBlank()) {
            return form;
        }

        String[] pairs = body.split("&");

        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = "";

            if (parts.length == 2) {
                value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }

            form.put(key, value);
        }

        return form;
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().add("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private static void sendHtml(HttpExchange exchange, String html) throws IOException {
        byte[] response = html.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String getQueryParam(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getRawQuery();

        if (query == null || query.isBlank()) {
            return null;
        }

        String[] pairs = query.split("&");

        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);

            String currentKey = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);

            if (currentKey.equals(key)) {
                if (parts.length == 2) {
                    return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                }

                return "";
            }
        }

        return null;
    }

}//end of WebServer
