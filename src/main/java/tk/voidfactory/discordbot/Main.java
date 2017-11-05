package tk.voidfactory.discordbot;

import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import tk.voidfactory.discordbot.data.SyncChannelSet;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {
        JDABuilder shardBuilder = new JDABuilder(AccountType.BOT)
                .setToken(System.getenv("API_TOKEN"))
                .setReconnectQueue(new SessionReconnectQueue());
        shardBuilder.addEventListener(new MessageListener());
        shardBuilder.addEventListener(new tk.voidfactory.discordbot.music.Main());
        SyncChannelSet.init();
        HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(System.getenv("PORT"))), 0);
        server.createContext("/", exchange -> exchange.sendResponseHeaders(204, 0));
        server.setExecutor(null);
        server.start();

        for (int i = 0; i < 10; i++) {
            shardBuilder.useSharding(i, 10)
                    .buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
            Thread.sleep(5000); //sleep 5 seconds between each login
        }
    }
}
