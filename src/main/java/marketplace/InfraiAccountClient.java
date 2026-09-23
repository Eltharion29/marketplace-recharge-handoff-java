package marketplace;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfraiAccountClient {
    private static final Pattern OK = Pattern.compile("\\\"ok\\\"\\s*:\\s*(true|false)");
    private static final Pattern ERROR_CODE = Pattern.compile("\\\"code\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern MESSAGE_ID = Pattern.compile("\\\"message_id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private final HttpClient http;
    private final MarketplaceConfig config;

    public InfraiAccountClient(HttpClient http, MarketplaceConfig config) {
        this.http = http;
        this.config = config;
    }

    public String balanceEnvelope() throws IOException, InterruptedException {
        return send("GET", "/account/balance", null).body();
    }

    public void configureAutoRecharge(long triggerBalance, long rechargeAmount) throws IOException, InterruptedException {
        String body = "{\"trigger_balance\":" + triggerBalance + ",\"recharge_amount\":" + rechargeAmount + "}";
        requireSuccess(send("PUT", "/account/autorecharge/configure", body));
    }

    public String sendBuyerUpdate(String buyerEmail, String orderId, String assetTitle) throws IOException, InterruptedException {
        String body = "{\"to\":\"" + json(buyerEmail) + "\",\"subject\":\"" + json("Order " + orderId + " is being handed off")
            + "\",\"body\":\"" + json("Your seller asset, " + assetTitle + ", is reserved while the balance is refreshed.") + "\"}";
        HttpResponse<String> response = send("POST", "/email/send", body);
        requireSuccess(response);
        Matcher match = MESSAGE_ID.matcher(response.body());
        if (!match.find()) {
            throw new InfraiException("Email response did not include message_id.", response.statusCode());
        }
        return match.group(1);
    }

    private HttpResponse<String> send(String method, String path, String body) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(config.baseUrl() + path))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + config.apiKey())
                .header("Accept", "application/json");
            if (body != null) {
                builder.header("Content-Type", "application/json");
                builder.method(method, HttpRequest.BodyPublishers.ofString(body));
            } else {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 429 || attempt == 2) {
                return response;
            }
            waitBeforeRetry(response, attempt);
        }
        throw new IOException("No response returned.");
    }

    private static void waitBeforeRetry(HttpResponse<String> response, int attempt) throws InterruptedException {
        Optional<String> retryAfter = response.headers().firstValue("Retry-After");
        long delayMillis = retryAfter.map(InfraiAccountClient::secondsToMillis).orElse(250L << attempt);
        Thread.sleep(delayMillis);
    }

    private static long secondsToMillis(String value) {
        try {
            return Math.max(0, Long.parseLong(value) * 1000L);
        } catch (NumberFormatException ignored) {
            return 250L;
        }
    }

    private static void requireSuccess(HttpResponse<String> response) {
        Matcher ok = OK.matcher(response.body());
        if (!ok.find()) {
            throw new InfraiException("Response was not an Infrai envelope.", response.statusCode());
        }
        if (!Boolean.parseBoolean(ok.group(1))) {
            Matcher code = ERROR_CODE.matcher(response.body());
            String detail = code.find() ? code.group(1) : "request rejected";
            throw new InfraiException(detail, response.statusCode());
        }
        if (response.statusCode() >= 500) {
            throw new InfraiException("Request could not be completed.", response.statusCode());
        }
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public static final class InfraiException extends RuntimeException {
        private final int status;

        public InfraiException(String message, int status) {
            super(message);
            this.status = status;
        }

        public int status() {
            return status;
        }
    }
}
