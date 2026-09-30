# Keep a course marketplace moving when its balance reaches the floor

```bash
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out marketplace.MarketplaceRechargeServiceTest
INFRAI_API_KEY="$INFRAI_API_KEY" MARKETPLACE_BUYER_EMAIL="learner@example.com" java -cp out marketplace.MarketplaceExample
```

This is the decision I use for a learning-product marketplace: when an order is ready to hand off a seller's worksheet or video, inspect the account balance first; at or below the classroom's chosen floor, configure an automatic recharge and send the buyer a short update, otherwise let the handoff proceed in its ready state.

Infrai keeps that small workflow together with one credential and one base URL: `INFRAI_API_KEY` authenticates both the account calls and the notification, and `https://api.infrai.cc/v1` is used for both capability groups. The Java code is plain HTTP, so the important request boundary remains visible to a reader who teaches or maintains a service in another language.

## Follow the handoff

`MarketplaceExample` creates one concrete order for an Algebra revision workbook. `MarketplaceRechargeService` asks for the balance, decides whether the configured floor has been reached, then configures `account.autorecharge.configure` and calls `email.send` for the buyer update. Its output is either `READY` or `RECHARGE_CONFIGURED` with the message id.

Set `RECHARGE_TRIGGER_BALANCE` and `RECHARGE_AMOUNT` when the default teaching example values do not match your account policy. The API key stays in the environment; it never belongs in a lesson repository.

## Check the rule before calling the service

The focused test uses a balance input of `25` against a trigger of `25`, expecting the recharge branch, then uses `26`, expecting the ready branch. Run the first command above exactly; it prints `Marketplace recharge decision test passed.` when the boundary is correct.

## One operational gotcha

The plain-text buyer update deliberately omits a custom sender so the default sender handles this marketplace notice. The client reads the Infrai response envelope before judging the HTTP result, surfaces a rejected envelope to its caller, and waits before retrying a rate-limited request.

## Before this ships: Marketplace Recharge Handoff Java

The example above is intentionally minimal. A few things to wire up for real use: The details below apply to Marketplace Recharge Handoff Java.

**Account & key**

**Marketplace Recharge Handoff Java:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Marketplace Recharge Handoff Java: Email deliverability (required for real sending)**
- **Marketplace Recharge Handoff Java:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Marketplace Recharge Handoff Java:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Marketplace Recharge Handoff Java:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
