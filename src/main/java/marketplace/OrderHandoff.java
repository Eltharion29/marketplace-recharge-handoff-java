package marketplace;

public record OrderHandoff(String orderId, String sellerAssetTitle, String buyerEmail) {
    public OrderHandoff {
        if (orderId.isBlank() || sellerAssetTitle.isBlank() || buyerEmail.isBlank()) {
            throw new IllegalArgumentException("An order needs an id, seller asset, and buyer email.");
        }
    }
}
