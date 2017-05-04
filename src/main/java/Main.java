import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

public class Main {
    public static void main(String[] args) {
        String token = BotProperties.instance().get("token");

        ClientBuilder builder = new ClientBuilder(); // Creates a new client builder instance
        builder.withToken(token); // Sets the bot token for the client

        try {
            IDiscordClient client = builder.login(); // Builds the IDiscordClient instance and logs it in
            MarkovBot bot = new MarkovBot(client); // Creating the bot instance
        } catch (DiscordException e) { // Error occurred logging in
            System.err.println("Error occurred while logging in!");
            e.printStackTrace();
        }
    }
}
