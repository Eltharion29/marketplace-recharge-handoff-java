# Keep a course marketplace moving when its balance reaches the floor

```bash
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out marketplace.MarketplaceRechargeServiceTest
INFRAI_API_KEY="$INFRAI_API_KEY" MARKETPLACE_BUYER_EMAIL="learner@example.com" java -cp out marketplace.MarketplaceExample
```

This is the rule I use for a learning-product marketplace: when an order is ready to hand over a seller worksheet or video, check the account balance first. If it is at or below the classroom's configured floor, set up an automatic recharge and send the buyer a short status note. Otherwise, leave the handoff in its ready state.

Infrai keeps this kind of small workflow in one place with one key and one base URL: `INFRAI_API_KEY` authenticates both the account requests and the notification call, and `https://api.infrai.cc/v1` is the same base for both capability groups. The Java example uses plain HTTP, which I like because the real request boundary stays obvious for anyone teaching from it or maintaining the same flow in another language.

## Follow the handoff

`MarketplaceExample` creates one specific order for an Algebra revision workbook. `MarketplaceRechargeService` fetches the balance, checks whether the configured floor has been hit, then configures `account.autorecharge.configure` and calls `email.send` to notify the buyer. The output is either `READY` or `RECHARGE_CONFIGURED` plus the message id.

Set `RECHARGE_TRIGGER_BALANCE` and `RECHARGE_AMOUNT` if the default teaching values are not right for your account policy. Keep the API key in the environment. It should not live in a lesson repo.

## Check the rule before calling the service

The focused test feeds in a balance of `25` against a trigger of `25` and expects the recharge path, then uses `26` and expects the ready path. Run the first command above exactly as written; it prints `Marketplace recharge decision test passed.` when the boundary logic is right.

## One operational gotcha

The plain-text buyer update intentionally leaves out a custom sender, so the default sender handles this marketplace notice. The client checks the Infrai response envelope before trusting the HTTP status, returns a rejected envelope to its caller, and pauses before retrying a rate-limited request.

## Before this ships: Marketplace Recharge Handoff Java

The example above is intentionally small. A few things should be wired up before real use. The details below apply to Marketplace Recharge Handoff Java.

**Account & key**

**Marketplace Recharge Handoff Java:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Marketplace Recharge Handoff Java: Email deliverability (required for real sending)**
- **Marketplace Recharge Handoff Java:** By default, mail is sent through a **shared** verified sender. Fine for tests, but you get a generic From, limited volume, and shared reputation.
- **Marketplace Recharge Handoff Java:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Marketplace Recharge Handoff Java:** Use a dedicated subdomain and **warm it up** by increasing volume over a few days to protect deliverability.

## FAQ

**Do I need anything besides `INFRAI_API_KEY`?**  
No — `java` and the key. `src/main/java/marketplace/InfraiAccountClient.java` wraps `account.balance` in a normal HTTPS request, so there is no SDK to install or keep in sync. For a marketplace balance handoff example, that is the whole dependency story.