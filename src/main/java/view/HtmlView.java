package view;

public class HtmlView {
    public String wrapPage(String title, String body) {
        return "<html><body>" +
                "<h1>" + title + "</h1>" +
                body +
                "</body></html>";
    }

    public String homePage() {
        String body = """
                <ul>
                    <li><a href="/agents">Agents</a></li>
                    <li><a href="/facilities">Facilities</a></li>
                    <li><a href="/missions">Missions</a></li>
                    <li><a href="/briefs">Briefs</a></li>
                    <li><a href="/audit">Audit Log</a></li>
                </ul>
                """;

        return wrapPage("Top Secret Database", body);
    }
    public String agentsPage(String agentText) {
        String body = "<pre>" + agentText + "</pre>"
                + "<p><a href=\"/\">Back to Home</a></p>";
        return wrapPage("Agents", body);
    }
    public String facilitiesPage(String facilityText) {
        String body = "<pre>" + facilityText + "</pre>"
                + "<p><a href=\"/\">Back to Home</a></p>";
        return wrapPage("Facilities", body);
    }
    public String missionsPage(String missionText) {
        String body = "<pre>" + missionText + "</pre>"
                + "<p><a href=\"/\">Back to Home</a></p>";
        return wrapPage("Missions", body);
    }
    public String briefsPage(String briefText) {
        String body = "<pre>" + briefText + "</pre>"
                + "<p><a href=\"/\">Back to Home</a></p>";
        return wrapPage("Briefs", body);
    }
    public String auditPage(String auditText) {
        String body = "<pre>" + auditText + "</pre>"
                + "<p><a href=\"/\">Back to Home</a></p>";
        return wrapPage("Audit Log", body);
    }

    public String linkedListPage(String title, String listText, String path) {
        StringBuilder body = new StringBuilder();

        body.append("<ol>");

        if (listText == null || listText.isBlank()) {
            body.append("<li>No records available.</li>");
        } else {
            String[] lines = listText.split("\\R");

            for (String line : lines) {
                String trimmed = line.trim();

                if (trimmed.isBlank()) {
                    continue;
                }

                int dotIndex = trimmed.indexOf(".");

                if (dotIndex > 0) {
                    String number = trimmed.substring(0, dotIndex).trim();
                    String label = trimmed.substring(dotIndex + 1).trim();

                    body.append("<li>");
                    body.append("<a href=\"")
                            .append(path)
                            .append("?number=")
                            .append(number)
                            .append("\">")
                            .append(escapeHtml(label))
                            .append("</a>");
                    body.append("</li>");
                } else {
                    body.append("<li>")
                            .append(escapeHtml(trimmed))
                            .append("</li>");
                }
            }
        }

        body.append("</ol>");
        body.append("<p><a href=\"/\">Back to Home</a></p>");

        return wrapPage(title, body.toString());
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

}
