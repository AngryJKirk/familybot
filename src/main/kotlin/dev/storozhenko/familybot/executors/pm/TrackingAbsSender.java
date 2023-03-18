package dev.storozhenko.familybot.executors.pm;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.stickers.AddStickerToSet;
import org.telegram.telegrambots.meta.api.methods.stickers.CreateNewStickerSet;
import org.telegram.telegrambots.meta.api.methods.stickers.SetStickerSetThumb;
import org.telegram.telegrambots.meta.api.methods.stickers.UploadStickerFile;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("TypeParameterHidesVisibleType")
public class TrackingAbsSender extends AbsSender {
    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>, Callback extends SentCallback<T>> void executeAsync(Method method, Callback callback) throws TelegramApiException {
        absSender.executeAsync(method, callback);
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> CompletableFuture<T> executeAsync(Method method) throws TelegramApiException {
        return absSender.executeAsync(method);
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
        T execute = absSender.execute(method);
        if (execute instanceof Message) {
            tracking.add((Message) execute);
        }
        return execute;
    }

    @Override
    public Message execute(SendDocument sendDocument) throws TelegramApiException {
        Message message = absSender.execute(sendDocument);
        tracking.add(message);
        return message;
    }

    @Override
    public Message execute(SendPhoto sendPhoto) throws TelegramApiException {
        Message message = absSender.execute(sendPhoto);
        tracking.add(message);
        return message;
    }

    @Override
    public Message execute(SendVideo sendVideo) throws TelegramApiException {
        Message message = absSender.execute(sendVideo);
        tracking.add(message);
        return message;
    }

    @Override
    public Message execute(SendVideoNote sendVideoNote) throws TelegramApiException {
        Message message = absSender.execute(sendVideoNote);
        tracking.add(message);
        return message;
    }

    @Override
    public Message execute(SendSticker sendSticker) throws TelegramApiException {
        Message message = absSender.execute(sendSticker);
        tracking.add(message);
        return message;
    }

    @Override
    public Message execute(SendAudio sendAudio) throws TelegramApiException {
        Message message = absSender.execute(sendAudio);
        tracking.add(message);
        return message;
    }

    @Override
    public Message execute(SendVoice sendVoice) throws TelegramApiException {
        Message message = absSender.execute(sendVoice);
        tracking.add(message);
        return message;
    }

    @Override
    public List<Message> execute(SendMediaGroup sendMediaGroup) throws TelegramApiException {
        return absSender.execute(sendMediaGroup);
    }

    @Override
    public Boolean execute(SetChatPhoto setChatPhoto) throws TelegramApiException {
        return absSender.execute(setChatPhoto);
    }

    @Override
    public Boolean execute(AddStickerToSet addStickerToSet) throws TelegramApiException {
        return absSender.execute(addStickerToSet);
    }

    @Override
    public Boolean execute(SetStickerSetThumb setStickerSetThumb) throws TelegramApiException {
        return absSender.execute(setStickerSetThumb);
    }

    @Override
    public Boolean execute(CreateNewStickerSet createNewStickerSet) throws TelegramApiException {
        return absSender.execute(createNewStickerSet);
    }

    @Override
    public File execute(UploadStickerFile uploadStickerFile) throws TelegramApiException {
        return absSender.execute(uploadStickerFile);
    }

    @Override
    public Serializable execute(EditMessageMedia editMessageMedia) throws TelegramApiException {
        return absSender.execute(editMessageMedia);
    }

    @Override
    public Message execute(SendAnimation sendAnimation) throws TelegramApiException {
        Message message = absSender.execute(sendAnimation);
        tracking.add(message);
        return message;
    }

    @Override
    public CompletableFuture<Message> executeAsync(SendDocument sendDocument) {
        return absSender.executeAsync(sendDocument);
    }

    @Override
    public CompletableFuture<Message> executeAsync(SendPhoto sendPhoto) {
        return absSender.executeAsync(sendPhoto);
    }

    @Override
    public CompletableFuture<Message> executeAsync(SendVideo sendVideo) {
        return absSender.executeAsync(sendVideo);
    }

    @Override
    public CompletableFuture<Message> executeAsync(SendVideoNote sendVideoNote) {
        return absSender.executeAsync(sendVideoNote);
    }

    @Override
    public CompletableFuture<Message> executeAsync(SendSticker sendSticker) {
        return absSender.executeAsync(sendSticker);
    }

    @Override
    public CompletableFuture<Message> executeAsync(SendAudio sendAudio) {
        return absSender.executeAsync(sendAudio);
    }

    @Override
    public CompletableFuture<Message> executeAsync(SendVoice sendVoice) {
        return absSender.executeAsync(sendVoice);
    }

    @Override
    public CompletableFuture<List<Message>> executeAsync(SendMediaGroup sendMediaGroup) {
        return absSender.executeAsync(sendMediaGroup);
    }

    @Override
    public CompletableFuture<Boolean> executeAsync(SetChatPhoto setChatPhoto) {
        return absSender.executeAsync(setChatPhoto);
    }

    @Override
    public CompletableFuture<Boolean> executeAsync(AddStickerToSet addStickerToSet) {
        return absSender.executeAsync(addStickerToSet);
    }

    @Override
    public CompletableFuture<Boolean> executeAsync(SetStickerSetThumb setStickerSetThumb) {
        return absSender.executeAsync(setStickerSetThumb);
    }

    @Override
    public CompletableFuture<Boolean> executeAsync(CreateNewStickerSet createNewStickerSet) {
        return absSender.executeAsync(createNewStickerSet);
    }

    @Override
    public CompletableFuture<File> executeAsync(UploadStickerFile uploadStickerFile) {
        return absSender.executeAsync(uploadStickerFile);
    }

    @Override
    public CompletableFuture<Serializable> executeAsync(EditMessageMedia editMessageMedia) {
        return absSender.executeAsync(editMessageMedia);
    }

    @Override
    public CompletableFuture<Message> executeAsync(SendAnimation sendAnimation) {
        return absSender.executeAsync(sendAnimation);
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>, Callback extends SentCallback<T>> void sendApiMethodAsync(Method method, Callback callback) {
        callProtectedMethod(absSender, "sendApiMethodAsync", method, callback);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> CompletableFuture<T> sendApiMethodAsync(Method method) {
        return (CompletableFuture<T>) callProtectedMethod(absSender, "sendApiMethodAsync", method);

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T sendApiMethod(Method method) throws TelegramApiException {
        return (T) callProtectedMethod(absSender, "sendApiMethod", method);
    }

    public static <T> Object callProtectedMethod(T object, String methodName, Object... args) {
        try {
            Class<?> cls = object.getClass();

            Class<?>[] parameterTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                parameterTypes[i] = args[i].getClass();
            }
            Method method = cls.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(object, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final AbsSender absSender;
    public final List<Message> tracking = new ArrayList<>();

    public TrackingAbsSender(AbsSender absSender) {
        this.absSender = absSender;
    }
}
