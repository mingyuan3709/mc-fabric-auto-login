package fun.yuanbai.autologin.event;

import fun.yuanbai.autologin.config.ConfigE;
import fun.yuanbai.autologin.util.EncryptionUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.Text;

import java.util.UUID;

import static fun.yuanbai.autologin.config.Configs.*;


public class AutoLoginHandler implements
        ClientReceiveMessageEvents.Game,
        ClientSendMessageEvents.Command,
        ClientPlayConnectionEvents.Join{   // 1. 多实现一个接口

    private static final long COOL_MS = 2_00L;
    private static long lastAutoLogin = 0L;

    /* ---------- 关键：一进服就执行 ---------- */
    @Override
    public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        tryAutoLogin(client);   // 立即试一次
    }

    /* ---------- 原聊天监听保留，作为“兜底/重试” ---------- */
    @Override
    public void onReceiveGameMessage(Text message, boolean overlay) {
        // 如果收到任何“需要登录”的提示，也再试一次
        if (message.getString().contains("/login")) {
            tryAutoLogin(MinecraftClient.getInstance());
        }
    }

    /* ---------- 公共抽取 ---------- */
    private void tryAutoLogin(MinecraftClient client) {
        LOGGER.info("[AutoLogin] 登录方法");
        if (client.player == null || client.getNetworkHandler() == null) return;
        if (!isAutoLogin()) return;

        long now = System.currentTimeMillis();
        if (now - lastAutoLogin < COOL_MS) return;

        UUID uuid = client.player.getGameProfile().getId();
        String serverIp = client.getNetworkHandler().getServerInfo().address;
        String password = getPassword(uuid, serverIp);
        if (password == null) return;

        try {
            password = EncryptionUtil.decrypt(password);
        } catch (Exception ignore) { return; }
        if (password.isBlank()) return;

        client.player.networkHandler.sendPacket(new  CommandExecutionC2SPacket("login " + password));
        lastAutoLogin = now;
    }

    @Override
    public void onSendCommandMessage(String command) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.player.networkHandler.getServerInfo() != null && command.contains("login")&&isAutoLogin()) {

            // 获取玩家UUID
            UUID uuid = client.player.getGameProfile().getId();

            String serverIp = client.player.networkHandler.getServerInfo().address;


            String[] parts = command.split(" ");

            if (parts.length > 1) {
                String password = parts[1];

                if (password != null && password.length() >= 4) {

                    ConfigE.PlayerServerInfo playerServerInfo = new ConfigE.PlayerServerInfo();

                    playerServerInfo.setPlayerUUID(uuid);

                    playerServerInfo.setServerIP(serverIp);

                    playerServerInfo.setPassword(EncryptionUtil.encrypt(password));

                    updateOrCreatePlayerServerInfo(playerServerInfo);

                }

            }

        }
    }

}
