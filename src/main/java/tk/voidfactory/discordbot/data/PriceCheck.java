package tk.voidfactory.discordbot.data;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.ranges.RangeException;

import javax.net.ssl.SSLHandshakeException;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class PriceCheck {
    private class ItemData {
        String name, tag;
        int
                maxsell = Integer.MIN_VALUE,
                minsell = Integer.MAX_VALUE,
                maxbuy = Integer.MIN_VALUE,
                minbuy = Integer.MAX_VALUE;

        @Override
        public String toString() {
            return "Покупка: " + (maxbuy == Integer.MIN_VALUE ? "N/A" : minbuy + " - " + maxbuy) +
                    "\nПродажа: " + (maxsell == Integer.MIN_VALUE ? "N/A" : minsell + " - " + maxsell);
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
                    if (data.size()>20) throw new IndexOutOfBoundsException();
                }
            };
            JSONArray itemlist_ru = JSONReader.readJsonFromUrl("https://api.warframe.market/v1/items","language","ru")
                    .getJSONObject("payload").getJSONObject("items").getJSONArray("ru");
            itemlist_ru.forEach(worker);
            JSONArray itemlist_en = JSONReader.readJsonFromUrl("https://api.warframe.market/v1/items","language","en")
                    .getJSONObject("payload").getJSONObject("items").getJSONArray("en");
            itemlist_en.forEach(worker);

        } catch (IOException ignored){
        }
    }

    public PriceCheck process() {
        data.forEach(itemData -> {
            try {
                JSONArray orders =
                        JSONReader.readJsonFromUrl("https://api.warframe.market/v1/items/" + itemData.tag + "/orders","platform","pc")
                                .getJSONObject("payload").getJSONArray("orders");
                for (int i = Integer.max(0, orders.length() - 100); i < orders.length(); i++) {
                    JSONObject jso = orders.getJSONObject(i);
                    int price = jso.getInt("platinum");
                    if (jso.getString("order_type").equals("sell")) {
                        if (price > itemData.maxsell) itemData.maxsell = price;
                        if (price < itemData.minsell) itemData.minsell = price;
                    } else {
                        if (price > itemData.maxbuy) itemData.maxbuy = price;
                        if (price < itemData.minbuy) itemData.minbuy = price;
                    }
                }
            } catch (IOException ignored) {
            }
        });
        return this;
    }

    public void print(TextChannel channel) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.PINK);
        if (data.isEmpty()) throw new IndexOutOfBoundsException();
        data.forEach(itemData -> eb.addField(itemData.name, itemData.toString(), true));
        eb.setFooter("Данные с сайта https://warframe.market", null);
        channel.sendMessage(eb.build()).queue();
    }
}