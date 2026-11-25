package com.yuelengm.nhkantan.market.view;

/** Filters the private snapshot to a single expiry chain before ModularUI string serialization. */
public final class OptionChainView {

    private OptionChainView() {}

    public static String filter(String snapshot, String underlying, int day, long gameTick) {
        com.google.gson.JsonObject view = new com.google.gson.JsonParser().parse(snapshot)
            .getAsJsonObject();
        com.google.gson.JsonArray chain = new com.google.gson.JsonArray();
        com.google.gson.JsonObject quotes = new com.google.gson.JsonObject();
        com.google.gson.JsonArray all = view.has("contracts") ? view.getAsJsonArray("contracts")
            : new com.google.gson.JsonArray();
        long selectedExpiry = Long.MAX_VALUE;
        if (underlying != null && day >= 1 && day <= 14) {
            for (com.google.gson.JsonElement element : all) {
                com.google.gson.JsonObject contract = element.getAsJsonObject();
                long expiry = contract.get("expiryTick")
                    .getAsLong();
                long remaining = expiry - gameTick;
                if (contract.get("underlying")
                    .getAsString()
                    .equals(underlying) && remaining > 0
                    && (remaining + 23999) / 24000 == day) selectedExpiry = Math.min(selectedExpiry, expiry);
            }
            for (com.google.gson.JsonElement element : all) {
                com.google.gson.JsonObject contract = element.getAsJsonObject();
                if (!contract.get("underlying")
                    .getAsString()
                    .equals(underlying)
                    || contract.get("expiryTick")
                        .getAsLong() != selectedExpiry)
                    continue;
                chain.add(element);
                String id = contract.get("id")
                    .getAsString();
                if (view.has("quotes") && view.getAsJsonObject("quotes")
                    .has(id))
                    quotes.add(
                        id,
                        view.getAsJsonObject("quotes")
                            .get(id));
            }
        }
        com.google.gson.JsonObject heldContracts = new com.google.gson.JsonObject();
        com.google.gson.JsonObject heldQuotes = new com.google.gson.JsonObject();
        com.google.gson.JsonArray holdings = new com.google.gson.JsonArray();
        if (underlying != null && view.has("holdingContracts")) {
            for (java.util.Map.Entry<String, com.google.gson.JsonElement> item : view
                .getAsJsonObject("holdingContracts")
                .entrySet()) {
                com.google.gson.JsonObject contract = item.getValue()
                    .getAsJsonObject();
                long remaining = contract.get("expiryTick")
                    .getAsLong() - gameTick;
                if (contract.get("underlying")
                    .getAsString()
                    .equals(underlying) && remaining > 0
                    && (remaining + 23999) / 24000 == day) {
                    heldContracts.add(item.getKey(), item.getValue());
                    if (view.has("holdingQuotes") && view.getAsJsonObject("holdingQuotes")
                        .has(item.getKey()))
                        heldQuotes.add(
                            item.getKey(),
                            view.getAsJsonObject("holdingQuotes")
                                .get(item.getKey()));
                }
            }
            if (view.has("options"))
                for (com.google.gson.JsonElement item : view.getAsJsonArray("options")) if (heldContracts.has(
                    item.getAsJsonObject()
                        .get("contract")
                        .getAsString()))
                    holdings.add(item);
        }
        view.add("holdingContracts", heldContracts);
        view.add("holdingQuotes", heldQuotes);
        view.add("options", holdings);
        view.add("contracts", chain);
        view.add("quotes", quotes);
        view.remove("notifications"); // Notifications use their own bounded, acknowledged channel.
        return view.toString();
    }

    /**
     * Summary-page view: keeps every open holding (all underlyings and expiries) and the contracts/quotes they need,
     * but drops the tradable chain, private order state and fields the summary page never reads, so the payload stays
     * well below the GUI string budget.
     */
    public static String filterSummary(String snapshot) {
        com.google.gson.JsonObject view = new com.google.gson.JsonParser().parse(snapshot)
            .getAsJsonObject();
        view.remove("contracts");
        view.remove("quotes");
        view.remove("notifications");
        view.remove("orders");
        view.remove("protections");
        view.remove("closing");
        trimHoldings(view);
        trimObjects(view, "holdingQuotes", "contract", "validUntilMillis", "unitCents", "theoretical", "delta");
        trimObjects(view, "holdingContracts", "id");
        return view.toString();
    }

    private static void trimHoldings(com.google.gson.JsonObject view) {
        if (!view.has("options") || !view.get("options")
            .isJsonArray()) return;
        for (com.google.gson.JsonElement element : view.getAsJsonArray("options")) if (element.isJsonObject()) {
            com.google.gson.JsonObject holding = element.getAsJsonObject();
            holding.remove("owner");
            holding.remove("payoutCents");
        }
    }

    private static void trimObjects(com.google.gson.JsonObject view, String name, String... fields) {
        if (!view.has(name) || !view.get(name)
            .isJsonObject()) return;
        for (java.util.Map.Entry<String, com.google.gson.JsonElement> entry : view.getAsJsonObject(name)
            .entrySet()) {
            if (!entry.getValue()
                .isJsonObject()) continue;
            com.google.gson.JsonObject value = entry.getValue()
                .getAsJsonObject();
            for (String field : fields) value.remove(field);
        }
    }
}
