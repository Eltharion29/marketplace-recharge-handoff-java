package marketplace;

import java.net.http.HttpClient;

public final class MarketplaceExample {
    public static void main(String[] args) throws Exception {
        MarketplaceConfig config = MarketplaceConfig.fromEnvironment();
        InfraiAccountClient infrai = new InfraiAccountClient(HttpClient.newHttpClient(), config);
        MarketplaceRechargeService service = new MarketplaceRechargeService(infrai, config);

        OrderHandoff order = new OrderHandoff("course-order-1042", "Algebra revision workbook", config.buyerEmail());
        MarketplaceRechargeService.HandoffResult result = service.handOff(order);
        System.out.println(result.orderId() + " -> " + result.state() + " (" + result.detail() + ")");
    }
}
