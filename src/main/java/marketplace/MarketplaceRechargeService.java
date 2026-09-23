package marketplace;

import java.io.IOException;

public final class MarketplaceRechargeService {
    private final InfraiAccountClient infrai;
    private final MarketplaceConfig config;

    public MarketplaceRechargeService(InfraiAccountClient infrai, MarketplaceConfig config) {
        this.infrai = infrai;
        this.config = config;
    }

    public HandoffResult handOff(OrderHandoff order) throws IOException, InterruptedException {
        String balance = infrai.balanceEnvelope();
        if (!balanceIndicatesLowFunds(balance, config.triggerBalance())) {
            return new HandoffResult(order.orderId(), "READY", "No recharge configuration was needed.");
        }
        infrai.configureAutoRecharge(config.triggerBalance(), config.rechargeAmount());
        String messageId = infrai.sendBuyerUpdate(order.buyerEmail(), order.orderId(), order.sellerAssetTitle());
        return new HandoffResult(order.orderId(), "RECHARGE_CONFIGURED", messageId);
    }

    static boolean balanceIndicatesLowFunds(String envelope, long triggerBalance) {
        String marker = "\"balance\":";
        int start = envelope.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Balance envelope did not include balance.");
        }
        int numberStart = start + marker.length();
        int numberEnd = numberStart;
        while (numberEnd < envelope.length() && Character.isDigit(envelope.charAt(numberEnd))) {
            numberEnd++;
        }
        long currentBalance = Long.parseLong(envelope.substring(numberStart, numberEnd));
        return currentBalance <= triggerBalance;
    }

    public record HandoffResult(String orderId, String state, String detail) { }
}
