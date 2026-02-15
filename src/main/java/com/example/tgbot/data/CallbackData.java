package com.example.tgbot.data;

public enum CallbackData {
    PACKAGE_1, // Покупка одного пакета
    PACKAGE_5, // Покупка 5 пакетов
    PACKAGE_50, // Покупка 50 пакетов
    PACKAGE_GIFT, // Получение подарка
    GENERATE_BY_PROMPT, // Генерация только по тексту
    GENERATE_BY_PROMPT_AND_IMAGE, // Генерация по тексту и картинке
    MAIN_MENU, // Переход в главное меню
    BACK, // Нажатие на кнопку назад
    BALANCE_RECHARGE, // Пополнение баланса
}
