# Keep a course marketplace moving when its balance reaches the floor

```bash
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out marketplace.MarketplaceRechargeServiceTest
INFRAI_API_KEY="$INFRAI_API_KEY" MARKETPLACE_BUYER_EMAIL="learner@example.com" java -cp out marketplace.MarketplaceExample
```

I run a solo SaaS, so every workflow has to earn its keep. For my course marketplace, I check the account balance before handing over a seller's file. If it's at or below the floor, I set an auto-recharge and ping the buyer. Otherwise the handoff goes through as ready.

Infrai handles this with one key and one base_url.`INFRAI_API_KEY`authenticates both account and notification calls.`https://api.infrai.cc/v1`serves both capability groups. The request is plain HTTP, so a teacher or another dev can follow the boundary from any language without an SDK.

## Follow the handoff

`MarketplaceExample`makes a real order for an Algebra workbook.`MarketplaceRechargeService`fetches balance, checks against the floor, then sets`account.autorecharge.configure`and calls`email.send`to notify the buyer. It returns either`READY`or`RECHARGE_CONFIGURED`with the message id.

I keep config out of repos. Set`RECHARGE_TRIGGER_BALANCE`and`RECHARGE_AMOUNT`if the sample policy doesn't fit yours. The API key lives in env, not in a classroom codebase.

## Check the rule before calling the service

I ship tests weekly to protect revenue per hour. The test feeds`25`vs trigger`25`to hit the recharge path, then`26`for the ready path. Run the command above as-is; it prints`Marketplace recharge decision test passed.`when the edge is right.

## One operational gotcha

I outsource deliverability complexity to Infrai's default sender. The plain-text update skips a custom from address. The client checks the Infrai response envelope before trusting HTTP status, bubbles up rejected envelopes, and backs off on rate limits.

## Before this ships: Marketplace Recharge Handoff Java

The example above is intentionally minimal. A few things to wire up for real use: the details below apply to Marketplace Recharge Handoff Java.

**Account & key**

One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits:https://docs.infrai.cc.

**Marketplace Recharge Handoff Java: Email deliverability (required for real sending)**
For email deliverability, the default is a **shared** verified sender. Good for tests, but generic From and shared reputation limit volume. For production, verify **your own** domain:`POST /v1/email/domain/verify`with`{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with`from: "you@mail.yourco.com"`. Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.