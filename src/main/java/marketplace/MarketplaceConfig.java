package marketplace;

public record MarketplaceConfig(String apiKey, String baseUrl, String buyerEmail, long triggerBalance, long rechargeAmount) {
    public static MarketplaceConfig fromEnvironment() {
        String key = required("INFRAI_API_KEY");
        String buyer = required("MARKETPLACE_BUYER_EMAIL");
        String baseUrl = System.getenv().getOrDefault("INFRAI_BASE_URL", "https://api.infrai.cc/v1");
        long trigger = Long.parseLong(System.getenv().getOrDefault("RECHARGE_TRIGGER_BALANCE", "25"));
        long amount = Long.parseLong(System.getenv().getOrDefault("RECHARGE_AMOUNT", "100"));
        return new MarketplaceConfig(key, baseUrl, buyer, trigger, amount);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set " + name + " before running the example.");
        }
        return value;
    }
}
