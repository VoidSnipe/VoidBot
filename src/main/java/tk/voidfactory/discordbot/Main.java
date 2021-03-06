package tk.voidfactory.discordbot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import tk.voidfactory.discordbot.data.SyncChannelSet;

public class Main {

    public static void main(String[] args) throws Exception {
        JDABuilder shardBuilder = new JDABuilder(AccountType.BOT)
                .setToken(System.getenv("API_TOKEN"));
        shardBuilder.addEventListener(new MainEngine(Settings.PREFIX));
        shardBuilder.addEventListener(new tk.voidfactory.discordbot.music.Main());
        SyncChannelSet.init();

        for (int i = 0; i < 10; i++) {
            shardBuilder.useSharding(i, 10)
                    .build();
        }
    }
}
