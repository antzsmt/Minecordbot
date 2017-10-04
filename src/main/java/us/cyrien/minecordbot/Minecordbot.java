package us.cyrien.minecordbot;

import com.jagrosh.jdautilities.waiter.EventWaiter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import us.cyrien.mcutils.Frame;
import us.cyrien.mcutils.config.ConfigManager;
import us.cyrien.mcutils.hook.PluginHook;
import us.cyrien.mcutils.logger.Logger;
import us.cyrien.minecordbot.accountSync.Authentication.AuthManager;
import us.cyrien.minecordbot.accountSync.Database;
import us.cyrien.minecordbot.accountSync.listener.UserConnectionListener;
import us.cyrien.minecordbot.chat.Messenger;
import us.cyrien.minecordbot.chat.listeners.mcListeners.*;
import us.cyrien.minecordbot.commands.discordCommand.ListCmd;
import us.cyrien.minecordbot.commands.minecraftCommand.*;
import us.cyrien.minecordbot.configuration.MCBConfigsManager;
import us.cyrien.minecordbot.entity.UpTimer;
import us.cyrien.minecordbot.handle.Metrics;
import us.cyrien.minecordbot.hooks.*;
import us.cyrien.minecordbot.localization.Locale;
import us.cyrien.minecordbot.localization.LocalizationFiles;

import javax.annotation.Nullable;

public class Minecordbot extends JavaPlugin {

    private static Minecordbot instance;

    private Messenger messenger;
    private AuthManager authManager;
    private UpTimer upTimer;
    private Bot bot;
    private MCBConfigsManager mcbConfigsManager;
    private EventWaiter eventWaiter;
    private Metrics metrics;

    @Override
    public void onEnable() {
        authManager = new AuthManager();
        eventWaiter = new EventWaiter();
        ConfigManager cfgManager = new ConfigManager(this);
        mcbConfigsManager = new MCBConfigsManager(cfgManager);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            Frame.main();
            postInit();
        }, 1L);
        if(mcbConfigsManager.setupConfigurations()) {
            Locale.init(mcbConfigsManager);
            bot = new Bot(this, eventWaiter);
            initDatabase();
            initInstances();
            initMCmds();
            initPluginHooks();
            initMListener();
        } else {
            this.getServer().shutdown();
        }
    }

    @Override
    public void onDisable() {
        if (bot != null)
            bot.shutdown();
    }

    //Framework stuff
    public void registerModule(Class module) {
        Frame.addModule(module);
    }

    public void registerMinecraftEventModule(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }


    /**
     * Register a {@link us.cyrien.mcutils.hook.PluginHook PluginHook} that allow the hook to be
     * used without being instantiated.
     * <p>
     * <p> Once the plugin hook have been registered. The hook can be called to a class like wise.
     * <pre>
     *          {@code
     *              @Hook
     *              private MCBHook hook;
     *              public void hook() {
     *                  if(hook != null)
     *                      Minecordbot mcb = hook.getPlugin();
     *              }
     *          }
     *     </pre>
     * </p>
     *
     * @param hook    Hook class that implements {@link PluginHook PluginHook}.
     * @param classes Classes that uses the hook.
     */
    public void registerMCPluginHook(Class hook, @Nullable Class... classes) {
        Frame.addHook(hook);
        for (Class c : classes)
            registerModule(c);
    }

    //private stuff/ initiation
    private void initDatabase() {
        Database.load();
    }

    private void initMListener() {
        registerMinecraftEventModule(new BroadcastCommandListener(this));
        registerMinecraftEventModule(new ChatListener(this));
        registerMinecraftEventModule(new CommandListener(this));
        registerMinecraftEventModule(new DeathListener(this));
        registerMinecraftEventModule(new MentionListener(this));
        registerMinecraftEventModule(new UserConnectionListener());
        registerMinecraftEventModule(new UserQuitJoinListener(this));
        registerMinecraftEventModule(new ListCmd(this));
        if (broadcastAvailable())
            registerMinecraftEventModule(new BroadcastListener(this));
        else
            Logger.bukkitWarn("Broadcast Listener is unsupported with " + Bukkit.getBukkitVersion());
    }

    private void initMCmds() {
        registerModule(Dcmd.class);
        registerModule(Dme.class);
        registerModule(ExeDCommand.class);
        registerModule(DSync.class);
        registerModule(DConfirm.class);
    }

    private void registerMCPluginHook(Class clazz) {
        registerMCPluginHook(clazz, HookContainer.class);
    }

    private void initPluginHooks() {
        registerMCPluginHook(MCBHook.class);
        registerMCPluginHook(GriefPreventionHook.class);
        registerMCPluginHook(VaultHook.class);
        registerMCPluginHook(MVHook.class);
        registerMCPluginHook(PermissionsExHook.class);
        registerMCPluginHook(EssentialsHook.class);
        registerMCPluginHook(mcMMOHook.class);
        registerMCPluginHook(SuperVanishHook.class);
    }

    private void postInit() {
        if (HookContainer.getEssentialsHook() != null)
            registerMinecraftEventModule(new HelpOpListener(this));
        if (HookContainer.getSuperVanishHook() != null)
            registerMinecraftEventModule(new SuperVanishListener(this));
    }

    private void initInstances() {
        messenger = new Messenger(this);
        LocalizationFiles localizationFiles = new LocalizationFiles(this, true);
        instance = this;
        upTimer = new UpTimer();
        metrics = new Metrics(this);
    }

    private boolean broadcastAvailable() {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        String formattedVersion = version.substring(version.lastIndexOf(46) + 1)
                .replaceAll("(_)([A-Z])\\w+", "").replaceAll("v", "");
        double parsed = Double.valueOf(formattedVersion.replaceAll("_", "."));
        return parsed >= 1.12;
    }

    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }

    public Bot getBot() {
        return bot;
    }

    public MCBConfigsManager getMcbConfigsManager() {
        return mcbConfigsManager;
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public static Minecordbot getInstance() {
        return instance;
    }

    public String getUpTime() {
        return upTimer.getCurrentUptime();
    }
}
