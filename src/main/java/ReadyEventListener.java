import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

import java.io.File;

public class ReadyEventListener implements IListener<ReadyEvent> {

    //@Override
    public void handle(ReadyEvent event) {
        try {
            event.getClient().changeUsername(BotProperties.instance().get("bot_name"));
            event.getClient().changeAvatar(Image.forFile(new File("resources/avatar.png")));
        } catch (RateLimitException | DiscordException e) {
            e.printStackTrace();
        }
    }
}
