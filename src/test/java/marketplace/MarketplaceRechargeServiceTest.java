package marketplace;

public final class MarketplaceRechargeServiceTest {
    public static void main(String[] args) {
        String belowTrigger = "{\"ok\":true,\"data\":{\"balance\":25}}";
        String aboveTrigger = "{\"ok\":true,\"data\":{\"balance\":26}}";

        require(MarketplaceRechargeService.balanceIndicatesLowFunds(belowTrigger, 25),
            "A balance at the trigger must configure the recharge before handoff.");
        require(!MarketplaceRechargeService.balanceIndicatesLowFunds(aboveTrigger, 25),
            "A balance above the trigger must leave the handoff ready.");
        System.out.println("Marketplace recharge decision test passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
