package tk.voidfactory.discordbot.data;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class PriceCheck {
    private class ItemData {
        String name, tag;
        LinkedList<Integer> sell = new LinkedList<>();
        LinkedList<Integer> buy = new LinkedList<>();

        @Override
        public String toString() {
            Integer[] sell = this.sell.toArray(new Integer[0]);
            Integer[] buy = this.buy.toArray(new Integer[0]);
            Arrays.sort(sell);
            Arrays.sort(buy, (o1, o2) -> o2 - o1);
            int i;
            for (i = 0; i < sell.length - 1; i++) {
                if (sell[i] * 1.3 > sell[i + 1]) break;
            }
            if (i == sell.length) i = 0;
            int j;
            for (j = 0; j < buy.length - 1; j++) {
                if (buy[j + 1] * 1.3 > buy[j]) break;
            }
            if (j == buy.length) j = 0;
            return "Покупка: " +
                    (buy.length == 0 ? "Нет данных" : buy[Integer.min(buy.length - 1, j + 5)]) + "-" + buy[j] +
                    "\n" +
                    "Продажа: " +
                    (sell.length == 0 ? "Нет данных" : sell[i] + "-" + sell[Integer.min(sell.length - 1, i + 5)]) +
                    "\n";
        }
    }

    private Queue<ItemData> data = new LinkedList<>();

    public PriceCheck(String... args) {
        String query = String.join(" ", args).toLowerCase();
        try {
            Consumer<Object> worker = o -> {
                JSONObject jso = (JSONObject) o;
                if (jso.getString("item_name").toLowerCase().startsWith(query)) {
                    ItemData item = new ItemData();
                    item.name = jso.getString("item_name");
                    item.tag = jso.getString("url_name");
                    data.add(item);
                    if (data.size() > 20) throw new IndexOutOfBoundsException();
                }
            };
            JSONArray itemlist_ru = JSONReader.readJsonFromUrl("https://api.warframe.market/v1/items", "language", "ru")
                    .getJSONObject("payload").getJSONObject("items").getJSONArray("ru");
            itemlist_ru.forEach(worker);
            JSONArray itemlist_en = JSONReader.readJsonFromUrl("https://api.warframe.market/v1/items", "language", "en")
                    .getJSONObject("payload").getJSONObject("items").getJSONArray("en");
            itemlist_en.forEach(worker);

        } catch (IOException ignored) {
        }
    }

    public PriceCheck process() {
        data.forEach(itemData -> {
            try {
                JSONArray orders =
                        JSONReader.readJsonFromUrl("https://api.warframe.market/v1/items/" + itemData.tag + "/orders", "platform", "pc")
                                .getJSONObject("payload").getJSONArray("orders");
                for (int i = 0; i < orders.length(); i++) {
                    JSONObject jso = orders.getJSONObject(i);
                    if (!jso.getJSONObject("user").getString("status").equals("ingame")) continue;
                    int price = jso.getInt("platinum");
                    if (jso.getString("order_type").equals("sell")) {
                        itemData.sell.add(price);
                    } else {
                        itemData.buy.add(price);
                    }
                }
            } catch (IOException ignored) {
            }
        });
        return this;
    }

    public MessageEmbed print() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.PINK);
        if (data.isEmpty()) throw new IndexOutOfBoundsException();
        data.forEach(itemData -> eb.addField(itemData.name, itemData.toString(), true));
        eb.setFooter("Данные с сайта https://warframe.market", null);
        return eb.build();
    }
}