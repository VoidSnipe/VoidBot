package tk.voidfactory.discordbot.data;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.json.*;

import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.function.Predicate;

public class WorldData {
    private static Boolean ready = false;
    private static Date cetus, arrival, departure;

    private static Date getEntryDate(JSONObject root, String key, String type, Predicate<JSONObject> condition) {
        JSONArray missions = root.getJSONArray(type);
        JSONObject mission;
        int i = 0;
        do {
            mission = missions.getJSONObject(i++);
        } while (!condition.test(mission) && i < missions.length());
        return new Date(mission.getJSONObject(key).getJSONObject("$date").getLong("$numberLong"));
    }

    private static String getLocalized(long n, String singular, String less, String more) {
        if (n == 0) return "";
        int t = (int)(n % ((n%100)<20?20:10));
        switch (t) {
            case 1: return n + " " + singular;
            case 2: case 3: case 4: return n + " " + less;
            default: return n + " " + more;
        }
    }
    private static synchronized void update() {
        JSONObject worldstate = new JSONObject();
        try {
            worldstate = JSONReader.readJsonFromUrl("http://content.warframe.com/dynamic/worldState.php");
        } catch (IOException e) {
            e.printStackTrace();
        }
        cetus = getEntryDate(worldstate, "Expiry", "SyndicateMissions",
                jsonObject -> jsonObject.getString("Tag").equals("CetusSyndicate"));

        arrival = getEntryDate(worldstate, "Activation", "VoidTraders", o -> true);
        departure = getEntryDate(worldstate, "Expiry", "VoidTraders", o -> true);
        ready = true;
    }

    public static synchronized MessageEmbed baro() {
        long now = new Date().getTime();
        if (!ready || now>departure.getTime()) update();
        long baro;
        EmbedBuilder builder = new EmbedBuilder();
        if (now>arrival.getTime()) {
            builder.setTitle("Баро уезжает через");
            baro = departure.getTime() - now;
        } else {
            builder.setTitle("Баро приезжает через");
            baro = arrival.getTime() - now;
        }
        baro /= 1000;
        long seconds, minutes, hours, days;
        seconds = baro % 60; baro /= 60;
        minutes = baro % 60; baro /= 60;
        hours = baro % 24; baro /= 24;
        days = baro;

        builder.setColor(Color.CYAN);
        builder.getDescriptionBuilder()
                .append(getLocalized(days,"день","дня","дней")).append(" ")
                .append(getLocalized(hours,"час","часа","часов")).append(" ")
                .append(getLocalized(minutes,"минута","минуты","минут")).append(" ")
                .append(getLocalized(seconds,"секунда","секунды","секунд"));
        return builder.build();
    }

    public static synchronized MessageEmbed cycle() {
        long now = new Date().getTime();
        if (!ready || now>cetus.getTime()) update();
        EmbedBuilder builder = new EmbedBuilder();

        long time = (cetus.getTime() - now)/1000;
        long seconds = time % 60;
        time =  time / 60 % 150;
        builder
                .setTitle("Равнины эйдолона: "+(time>50?"ДЕНЬ":"НОЧЬ"))
                .setColor(time>50?Color.WHITE:Color.BLUE);
        StringBuilder sb = builder.getDescriptionBuilder()
                .append("\nДо наступления ").append(time>50?"ночи":"дня").append(": ");
        time = (time + (time<50?0:100)) % 150;
        long minutes = time % 60; time /= 60;
        sb
                .append(getLocalized(time,"час","часа","часов")).append(" ")
                .append(getLocalized(minutes,"минута","минуты","минут")).append(" ")
                .append(getLocalized(seconds,"секунда","секунды","секунд"));

        return builder.build();
    }
}
