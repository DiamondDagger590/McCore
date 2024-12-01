package com.diamonddagger590.mccore.chat;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import org.jetbrains.annotations.NotNull;

/**
 * This task will automatically expire a {@link ChatResponse} after a period of seconds
 * provided by {@link ChatResponse#getResponseWaitTime()}.
 */
public class ChatResponseExpireTask extends ExpireableCoreTask {

    private final CorePlugin plugin;
    private final ChatResponse chatResponse;

    public ChatResponseExpireTask(@NotNull CorePlugin corePlugin, @NotNull ChatResponse chatResponse) {
        super(corePlugin, 0, chatResponse.getResponseWaitTime());
        this.plugin = corePlugin;
        this.chatResponse = chatResponse;
    }

    @Override
    protected void onTaskExpire() {
        plugin.getChatResponseManager().removePendingResponse(chatResponse);
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected void onDelayComplete() {

    }

    @Override
    protected void onIntervalStart() {

    }

    @Override
    protected void onIntervalComplete() {

    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }
}
