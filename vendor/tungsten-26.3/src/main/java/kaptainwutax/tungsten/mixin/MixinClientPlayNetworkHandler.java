package kaptainwutax.tungsten.mixin;

import java.util.ArrayList;
import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import kaptainwutax.tungsten.Debug;
import kaptainwutax.tungsten.TungstenMod;
import kaptainwutax.tungsten.TungstenModDataContainer;
import kaptainwutax.tungsten.commandsystem.Command;
import kaptainwutax.tungsten.commandsystem.CommandExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonPacketListenerImpl {
	
	@Shadow
    private ClientLevel level;

    @Shadow
    public abstract void sendChat(String content);

    @Unique
    private boolean ignoreChatMessage;

    @Unique
    private boolean worldNotNull;
    
    protected MixinClientPlayNetworkHandler(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }
    
    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (ignoreChatMessage) return;
		String prefix = TungstenMod.getCommandPrefix();
        if (message.startsWith(prefix)) {
            try {
            	if (message.contains("|")) {
//            		CommandExecutor.dispatch(message.split(";")[0].substring(prefix.length()));
                	Collection<Command> commands = new ArrayList<>(TungstenMod.getCommandExecutor().allCommands());
                	commands.removeIf((command) -> !message.contains(command.getName()));
                	TungstenMod.getCommandExecutor().executeRecursive(commands.toArray(new Command[commands.size()]), message.split("|"), 0, () -> {
                    }, ex -> Debug.logWarning(ex.getMessage()));
            	} else CommandExecutor.dispatch(message.substring(prefix.length()));
            } catch (CommandSyntaxException e) {
                Debug.logWarning(e.getMessage());
            } catch (IllegalArgumentException e) {
                Debug.logWarning(e.getMessage());
            }

            this.minecraft.gui.hud.getChat().addRecentChat(message);
            ci.cancel();
        }
    }

    // Respect authoritative server corrections; stop the predicted path before applying them.
    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void tungsten$correctPosition(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        TungstenModDataContainer.PATHFINDER.stop.set(true);
        TungstenModDataContainer.EXECUTOR.stop = true;
    }
}
