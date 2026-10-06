package kaptainwutax.tungsten.mixin;

import static kaptainwutax.tungsten.commandsystem.suggestionsapi.Filtering.FilteringMode.LOOSE;
import static kaptainwutax.tungsten.commandsystem.suggestionsapi.Filtering.FilteringMode.SLIGHTLY_LOOSE;
import static kaptainwutax.tungsten.commandsystem.suggestionsapi.Filtering.FilteringMode.STRICT;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import kaptainwutax.tungsten.TungstenMod;
import kaptainwutax.tungsten.commandsystem.CommandExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;

@Mixin(CommandSuggestions.class)
public abstract class MixinChatInputSuggestor {
    @Shadow private ParseResults<SharedSuggestionProvider> currentParse;
    @Shadow boolean keepSuggestions;
    @Shadow private CommandSuggestions.SuggestionsList suggestions;
	@Shadow private static int getLastWordIndex(String input) {
		throw new AssertionError();
	}
	@Shadow @Final EditBox input;
	
	@Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    @Final
    private boolean commandsOnly;
    
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^(@)");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    

    @Shadow
    public abstract void showSuggestions(boolean narrateFirstSuggestion);
    

    @Shadow
    protected abstract void updateUsageInfo(ParseResults<SharedSuggestionProvider> parse, Suggestions suggestions);

    @Inject(method = "sortSuggestions", at = @At("HEAD"), cancellable = true)
	private void tungsten$sortSuggestions(Suggestions suggestions, CallbackInfoReturnable<List<Suggestion>> ci) {
		String command = this.input.getValue().substring(0, this.input.getCursorPosition());

    	ci.cancel();
		// To make sorting command literals work
		if (command.startsWith("/") || command.startsWith(TungstenMod.getCommandPrefix()))
			command = command.substring(1);
		int startOfCurrentWord = getLastWordIndex(command);
		String remaining = command.substring(startOfCurrentWord);
		// To make sorting tags work
		if (remaining.startsWith("#"))
			remaining = remaining.substring(1);
		List<Suggestion> strictList = Lists.newArrayList();
		List<Suggestion> slightlyLooseList = Lists.newArrayList();
		List<Suggestion> looseList = Lists.newArrayList();
		List<Suggestion> veryLooseList = Lists.newArrayList();

		if (remaining.contains(":"))
			remaining = remaining.substring(remaining.indexOf(':') + 1);
		remaining = remaining.toLowerCase(Locale.ROOT);

		for(Suggestion suggestion : suggestions.getList()) {
			String suggestionText = suggestion.getText();
			if (suggestionText.contains(":"))
				suggestionText = suggestionText.substring(suggestionText.indexOf(':') + 1);
			suggestionText = suggestionText.toLowerCase(Locale.ROOT);

			if (STRICT.test(remaining, suggestionText))
				strictList.add(suggestion);
			else if (SLIGHTLY_LOOSE.test(remaining, suggestionText))
				slightlyLooseList.add(suggestion);
			else if (LOOSE.test(remaining, suggestionText))
				looseList.add(suggestion);
			else
				veryLooseList.add(suggestion);
		}

		strictList.addAll(slightlyLooseList);
		strictList.addAll(looseList);
		strictList.addAll(veryLooseList);
    	ci.setReturnValue(strictList);
	}


    @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true)
    private void tungsten$updateCommands(CallbackInfo ci) {
        String message = this.input.getValue();
        String prefix = TungstenMod.getCommandPrefix();
        if (!message.startsWith(prefix) || TungstenMod.mc.getConnection() == null) return;
        StringReader reader = new StringReader(message);
        reader.setCursor(prefix.length());
        this.currentParse = CommandExecutor.DISPATCHER.parse(reader, TungstenMod.mc.getConnection().getSuggestionsProvider());
        int cursor = this.input.getCursorPosition();
        if (cursor >= prefix.length() && (this.suggestions == null || !this.keepSuggestions)) {
            ParseResults<SharedSuggestionProvider> parse = this.currentParse;
            CompletableFuture<Suggestions> future = CommandExecutor.DISPATCHER.getCompletionSuggestions(parse, cursor);
            this.pendingSuggestions = future;
            future.thenAccept(result -> TungstenMod.mc.execute(() -> {
                if (this.pendingSuggestions == future) this.updateUsageInfo(parse, result);
            }));
        }
        ci.cancel();
    }

	 private int getLastPattern(String input, Pattern pattern) {
	        if (Strings.isNullOrEmpty(input)) {
	            return 0;
	        }
	        int i = 0;
	        Matcher matcher = pattern.matcher(input);
	        while (matcher.find()) {
	            i = matcher.end();
	        }
	        return i;
	    }
}