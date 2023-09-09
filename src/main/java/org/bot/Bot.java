package org.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.GetChatAdministrators;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.*;

public class Bot {
    private static final Map<String, User> users = new HashMap<>();
    private static final Map<Long, Set<Integer>> chatUserIdMap = new HashMap<>();
    private static final TelegramBot bot = new TelegramBot(""); // Put your bot token here

    public static void main(String[] args) {

        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                if (update.message() != null && update.message().text() != null) {

                    long chatId = update.message().chat().id();
                    String message = update.message().text();

                    if (message.equals("/register")) {
                        User user = update.message().from();
                        String userKey = """
                                %s - %s""".formatted(chatId, user.id());
                        boolean isUserAlreadyIncluded = users.containsKey(userKey);
                        SendMessage response;

                        if (isUserAlreadyIncluded) {
                            response = new SendMessage(chatId, """
                                    %s ты уже зарегистрировался :) проявляй активность в чате, я за тобой слежу :)""".formatted(user.firstName()));
                        } else  {
                            users.put(userKey, user);
                            response = new SendMessage(chatId, """
                                    %s добавлен в список""".formatted(user.firstName()));
                        }
                        bot.execute(response);
                    }

                    if (message.equals("/start")) {
                        GetChatAdministrators administrators = new GetChatAdministrators(chatId);
                        List<ChatMember> admins = bot.execute(administrators).administrators();
                        if (admins != null) {
                            for (ChatMember admin : admins) {
                                SendMessage response = new SendMessage(chatId, admin.user().firstName());
                                bot.execute(response);
//                                if (admin.user().id().equals(update.message().from().id())) {
//                                    SendMessage response = new SendMessage(chatId, admin.user().firstName());
//                                    bot.execute(response);
//                                    break;
//                                }
                            }
                        }
                    }


                    int userId = Math.toIntExact(update.message().from().id());

                    if (chatUserIdMap.containsKey(chatId)) {
                        chatUserIdMap.get(chatId).add(userId);
                    }

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (chatUserIdMap.containsKey(chatId) && !chatUserIdMap.get(chatId).contains(userId)) {
                                bot.execute(new SendMessage(chatId, "User with ID: " + userId + " hasn't checked in"));
                            }
                        }
                    }, 24 * 60 * 60 * 1000);
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}