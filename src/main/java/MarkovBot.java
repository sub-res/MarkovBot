import sx.blah.discord.api.IDiscordClient;

public class MarkovBot {
    public IDiscordClient client;

    public MarkovBot(IDiscordClient client) {
        this.client = client;
        client.getDispatcher().registerListener(new MessageEventListener());
        client.getDispatcher().registerListener(new ReadyEventListener());
    }
}
