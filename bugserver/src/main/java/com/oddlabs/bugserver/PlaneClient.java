package com.oddlabs.bugserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Minimal client for the Plane REST API (https://developers.plane.so/api-reference): creates work items and attaches
 * files to them.
 */
final class PlaneClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String project_url;
    private final String api_key;
    private final String items_path;
    private final String attachments_path;
    private final String priority;
    private final List<String> labels;

    /**
     * @param base_url   Plane root, e.g. {@code https://plane.example.org}
     * @param items_path {@code work-items} on current Plane, {@code issues} on older self-hosted versions
     */
    PlaneClient(String base_url, String api_key, String workspace, String project_id, String items_path,
            String priority, List<String> labels) {
        this.project_url = stripSlash(base_url) + "/api/v1/workspaces/" + workspace + "/projects/" + project_id;
        this.api_key = api_key;
        this.items_path = items_path;
        this.attachments_path = items_path.equals("issues") ? "issue-attachments" : "attachments";
        this.priority = priority;
        this.labels = labels;
    }

    record WorkItem(String id, String sequence_id) {
    }

    WorkItem createWorkItem(String name, String description_html) throws IOException, InterruptedException {
        ObjectNode body = mapper.createObjectNode();
        body.put("name", name);
        body.put("description_html", description_html);
        body.put("priority", priority);
        if (!labels.isEmpty())
            body.putPOJO("labels", labels);
        JsonNode response = sendJson("POST", project_url + "/" + items_path + "/", body);
        return new WorkItem(response.path("id").asText(), response.path("sequence_id").asText());
    }

    /**
     * Plane stores attachments in S3: ask Plane for a presigned upload, post the file straight to storage, then tell
     * Plane the upload finished.
     */
    void attach(String work_item_id, String file_name, String mime_type,
            byte[] data) throws IOException, InterruptedException {
        String attachments_url = project_url + "/" + items_path + "/" + work_item_id + "/" + attachments_path + "/";
        ObjectNode body = mapper.createObjectNode();
        body.put("name", file_name);
        body.put("type", mime_type);
        body.put("size", data.length);
        JsonNode credentials = sendJson("POST", attachments_url, body);
        JsonNode upload_data = credentials.path("upload_data");
        String asset_id = credentials.path("asset_id").asText();
        if (!upload_data.hasNonNull("url") || asset_id.isEmpty())
            throw new IOException("Unexpected attachment response from Plane: " + credentials);

        uploadToStorage(upload_data.get("url").asText(), upload_data.path("fields"), file_name, mime_type, data);

        ObjectNode done = mapper.createObjectNode();
        done.put("is_uploaded", true);
        sendJson("PATCH", attachments_url + asset_id + "/", done);
    }

    private void uploadToStorage(String url, JsonNode fields, String file_name, String mime_type,
            byte[] data) throws IOException, InterruptedException {
        String boundary = "----bugserver" + UUID.randomUUID().toString().replace("-", "");
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length + 4096);
        for (Map.Entry<String, JsonNode> field : fields.properties()) {
            writeAscii(out,
                    "--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + field.getKey() + "\"\r\n\r\n");
            out.write(field.getValue().asText().getBytes(StandardCharsets.UTF_8));
            writeAscii(out, "\r\n");
        }
        // S3 requires the file to be the last part of the form.
        writeAscii(out,
                "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + file_name + "\"\r\nContent-Type: " + mime_type + "\r\n\r\n");
        out.write(data);
        writeAscii(out, "\r\n--" + boundary + "--\r\n");

        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).header("Content-Type",
                "multipart/form-data; boundary=" + boundary).POST(HttpRequest.BodyPublishers.ofByteArray(
                        out.toByteArray())).build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2)
            throw new IOException(
                    "Storage upload to " + url + " failed: " + response.statusCode() + " " + response.body());
    }

    private JsonNode sendJson(String method, String url, JsonNode body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        builder.timeout(TIMEOUT);
        builder.header("X-API-Key", api_key);
        builder.header("Content-Type", "application/json");
        builder.method(method, HttpRequest.BodyPublishers.ofByteArray(mapper.writeValueAsBytes(body)));
        HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2)
            throw new IOException(
                    "Plane " + method + " " + url + " failed: " + response.statusCode() + " " + response.body());
        return response.body().isBlank() ? mapper.createObjectNode() : mapper.readTree(response.body());
    }

    private static void writeAscii(ByteArrayOutputStream out, String s) {
        out.writeBytes(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String stripSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
