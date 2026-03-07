# Web Interface API — документация для фронтенда

Базовый путь: `{BASE_URL}/v1/web`

Все методы возвращают JSON, кроме `GET /files/{fileId}` (возвращает бинарный файл).

**Общее:** все входные параметры (в т.ч. `userId` в body и query) принимаются как **строки**; маппинг на типы выполняется в бэкенде. Для `userId` допустима строка вида `"123456789"`.

При успешной постановке задачи на генерацию (`POST /kling`, `/sora2`, `/suno`, `/nanobanana`) в ответе возвращаются `taskId` и актуальный `balance` пользователя (после списания стоимости).

---

## 1. Загрузка изображений

**`POST /v1/web/upload`**

Загружает изображения и возвращает их публичные URL. Эти URL используются в полях `imageUrls` (Kling, Sora) или `imageInput` (NanoBanana) при запросах на генерацию.

### Request

- **Content-Type:** `multipart/form-data`
- **Поле:** `images` (массив файлов)

### Ограничения

- Форматы: JPEG, PNG, WebP
- Максимальный размер файла: 10 MB

### Response

**200 OK**
```json
{
  "urls": [
    "https://api.example.com/v1/web/files/550e8400-e29b-41d4-a716-446655440000",
    "https://api.example.com/v1/web/files/6ba7b810-9dad-11d1-80b4-00c04fd430c8"
  ]
}
```

**400 Bad Request** — текст ошибки (неверный формат, слишком большой файл и т.п.)

**500 Internal Server Error** — ошибка сервера

### Пример (JavaScript)

```javascript
const formData = new FormData();
formData.append('images', file1);
formData.append('images', file2);

const response = await fetch('/v1/web/upload', {
  method: 'POST',
  body: formData
});
const { urls } = await response.json();
```

---

## 2. Получение загруженного файла

**`GET /v1/web/files/{fileId}`**

Отдаёт содержимое файла по его ID. URL формируется как `{baseUrl}/v1/web/files/{fileId}`.

### Request

- **Path параметр:** `fileId` — UUID файла из ответа `/upload`

### Response

**200 OK** — бинарное содержимое файла (Content-Type: application/octet-stream)

**404 Not Found** — файл не найден или уже удалён

---

## 3. Генерация Kling

**`POST /v1/web/kling`**

Запускает генерацию видео через модель Kling 3.0.

### Request

- **Content-Type:** `application/json`
- **Body:**

```json
{
  "userId": "123456789",
  "options": {
    "aspectRatio": "9:16",
    "duration": 10,
    "withSound": false,
    "mode": "std",
    "imageUrls": ["https://..."],
    "prompt": "Текстовое описание сцены"
  }
}
```

| Поле | Тип | Обязательно | Описание |
|------|-----|-------------|----------|
| userId | string | да | User.telegramId (строка, напр. `"123456789"`) |
| options.aspectRatio | string | нет | Соотношение сторон: `"9:16"`, `"16:9"` и др. |
| options.duration | number | нет | Длительность в секундах (по умолчанию 10) |
| options.withSound | boolean | нет | Добавлять ли звук |
| options.mode | string | нет | Режим: `"std"`, `"pro"` |
| options.imageUrls | string[] | нет | URL изображений (результат `/upload`) |
| options.prompt | string | да | Промпт для генерации |

### Response

**200 OK**
```json
{
  "taskId": "abc123-xyz-456",
  "balance": 850
}
```

| Поле | Тип | Описание |
|------|-----|----------|
| taskId | string | ID задачи для опроса результата |
| balance | number | Текущий баланс пользователя (после списания стоимости генерации) |

**400 Bad Request** — текст ошибки парсинга JSON

**500 Internal Server Error** — ошибка постановки задачи (в т.ч. недостаточный баланс)

---

## 4. Генерация Sora 2

**`POST /v1/web/sora2`**

Запускает генерацию видео через модель Sora 2.

### Request

- **Content-Type:** `application/json`
- **Body:**

```json
{
  "userId": "123456789",
  "options": {
    "prompt": "Текстовое описание сцены",
    "aspectRatio": "9:16",
    "nFrames": "10",
    "imageUrls": ["https://..."]
  }
}
```

| Поле | Тип | Обязательно | Описание |
|------|-----|-------------|----------|
| userId | string | да | User.telegramId (строка) |
| options.prompt | string | да | Промпт для генерации |
| options.aspectRatio | string | нет | `"9:16"`, `"16:9"` |
| options.nFrames | string | нет | Количество кадров (по умолчанию "10") |
| options.imageUrls | string[] | нет | URL изображений для image-to-video |

### Response

**200 OK**
```json
{
  "taskId": "abc123-xyz-456",
  "balance": 850
}
```

| Поле | Тип | Описание |
|------|-----|----------|
| taskId | string | ID задачи для опроса результата |
| balance | number | Текущий баланс пользователя (после списания) |

**400 / 500** — см. Kling

---

## 5. Генерация Suno

**`POST /v1/web/suno`**

Запускает генерацию музыки через модель Suno V5.

### Request

- **Content-Type:** `application/json`
- **Body:**

```json
{
  "userId": "123456789",
  "options": {
    "customMode": false,
    "prompt": "Описание музыки",
    "instrumental": false,
    "audioWeight": null,
    "genre": "pop"
  }
}
```

| Поле | Тип | Обязательно | Описание |
|------|-----|-------------|----------|
| userId | string | да | User.telegramId (строка) |
| options.customMode | boolean | нет | Режим кастомизации |
| options.prompt | string | да | Описание желаемой музыки |
| options.instrumental | boolean | нет | Инструментальная композиция |
| options.audioWeight | number | нет | Вес аудио (если применимо) |
| options.genre | string | нет | Жанр: `"pop"`, `"rock"` и др. |

### Response

**200 OK**
```json
{
  "taskId": "abc123-xyz-456",
  "balance": 850
}
```

| Поле | Тип | Описание |
|------|-----|----------|
| taskId | string | ID задачи для опроса результата |
| balance | number | Текущий баланс пользователя (после списания) |

**400 / 500** — см. Kling

---

## 6. Генерация NanoBanana Pro

**`POST /v1/web/nanobanana`**

Запускает генерацию изображений через модель NanoBanana Pro.

### Request

- **Content-Type:** `application/json`
- **Body:**

```json
{
  "userId": "123456789",
  "options": {
    "prompt": "Описание изображения",
    "imageInput": ["https://..."],
    "aspectRatio": "9:16",
    "resolution": "2K",
    "outputFormat": "png"
  }
}
```

| Поле | Тип | Обязательно | Описание |
|------|-----|-------------|----------|
| userId | string | да | User.telegramId (строка) |
| options.prompt | string | да | Промпт для генерации |
| options.imageInput | string[] | нет | URL изображений (результат `/upload`) |
| options.aspectRatio | string | нет | Соотношение сторон |
| options.resolution | string | нет | Разрешение (по умолчанию "2K") |
| options.outputFormat | string | нет | Формат: `"png"`, `"jpg"` и др. |

### Response

**200 OK**
```json
{
  "taskId": "abc123-xyz-456",
  "balance": 850
}
```

| Поле | Тип | Описание |
|------|-----|----------|
| taskId | string | ID задачи для опроса результата |
| balance | number | Текущий баланс пользователя (после списания) |

**400 / 500** — см. Kling

---

## 7. Получение результата задачи

**`GET /v1/web/result`**

Возвращает результат завершённой генерации по `userId` и `taskId`. Запрос имеет смысл только после того, как генерация завершена (успешно или с ошибкой).

### Request

- **Query параметры:**
  - `userId` (string) — User.telegramId
  - `taskId` (string) — ID задачи из ответа методов generate

### Response

**200 OK**
```json
{
  "resultUrls": [
    "https://cdn.example.com/result/video123.mp4"
  ],
  "model": "KLING_3_0",
  "balanceChange": -115,
  "options": {
    "aspectRatio": "9:16",
    "duration": 10,
    "prompt": "Описание сцены",
    ...
  }
}
```

**200 OK** (SUNO_V5 — расширенная структура):
```json
{
  "resultUrls": [
    {
      "audioUrl": "https://cdn.example.com/audio/track1.mp3",
      "imageUrl": "https://cdn.example.com/cover/track1.jpg",
      "title": "My Song Title",
      "text": "Текст промпта, использованного для генерации"
    }
  ],
  "model": "SUNO_V5",
  "balanceChange": -50,
  "options": { "prompt": "...", ... }
}
```

| Поле | Описание |
|------|----------|
| resultUrls | Массив URL или объектов. Для Kling/Sora/NanoBanana — строки URL. Для SUNO_V5 — объекты `{audioUrl, imageUrl, title, text}`. |
| model | Модель генерации (`KLING_3_0`, `SORA_2`, `SUNO_V5`, `NANO_BANANA_PRO` и др.) |
| balanceChange | Цена генерации (отрицательное число — списание с баланса) |
| options | Опции, с которыми была запущена генерация |

**200 OK** (при ошибке задачи) — если задача завершилась с ошибкой:
```json
{
  "code": "E011",
  "description": "Операция отменена или не удалась (callback failed)"
}
```

**404 Not Found** — задача не найдена, ещё не завершена или `userId` не совпадает

> Результат можно запросить только один раз. После успешного ответа или ответа с ошибкой он удаляется с сервера.

---

## 7.1. Информация о пользователе

**`GET /v1/web/user`**

Возвращает баланс и статус амбассадора по `userId` (telegram ID).

### Request

- **Query параметры:**
  - `userId` (string) — User.telegramId

### Response

**200 OK**
```json
{
  "balance": 850,
  "ambassador": false
}
```

| Поле | Тип | Описание |
|------|-----|----------|
| balance | number | Текущий баланс пользователя |
| ambassador | boolean | Статус амбассадора (is_ambassador) |

**404 Not Found** — пользователь с таким `userId` не найден

**400 Bad Request** — `userId` не передан или невалиден

---

### Коды ошибок (ErrorCode)

| Код | Описание |
|-----|----------|
| E001 | Ошибка генерации видео (Kei AI API) |
| E002 | Ошибка генерации музыки (Kei AI API) |
| E003 | Ошибка генерации изображений (Kei AI API) |
| E004 | Недостаточный баланс |
| E005 | Невалидный запрос (JSON, параметры) |
| E006 | Ошибка загрузки файлов |
| E007 | Ошибка внешнего API (Kei AI) |
| E008 | Внутренняя ошибка сервера |
| E009 | Превышен лимит запросов |
| E010 | Ошибка при отправке результата в Telegram |
| E011 | Операция отменена или не удалась (callback failed) |

---

## 8. История операций

История разделена по типу контента. Каждый эндпоинт возвращает текущий баланс пользователя и список операций, отсортированный по дате по убыванию.

### 8.1. История видео (Sora, Kling)

**`GET /v1/web/history/video`**

### 8.2. История музыки (Suno)

**`GET /v1/web/history/music`**

### 8.3. История изображений (Nano Banana Pro)

**`GET /v1/web/history/image`**

### Request

- **Query параметры:**
  - `userId` (string) — User.telegramId

### Response

**200 OK**
```json
{
  "balance": 850,
  "items": [
    {
      "options": {
        "prompt": "Описание сцены",
        "mode": "pro",
        "aspectRatio": "9:16",
        ...
      },
      "balanceChange": -115,
      "date": "2025-03-05T14:30:00+03:00",
      "resultUrls": [
        "https://cdn.example.com/result/video123.mp4"
      ],
      "model": "KLING_3_0"
    }
  ]
}
```

| Поле | Описание |
|------|----------|
| balance | Текущий баланс пользователя |
| items | Список операций |

Элемент в `items`:

| Поле | Описание |
|------|----------|
| options | Опции запроса (параметры генерации), объект |
| balanceChange | Изменение баланса (отрицательное при списании) |
| date | Дата и время операции (ISO 8601) |
| resultUrls | URL или элементы результатов. Для SUNO_V5 — объекты `{audioUrl, imageUrl, title, text}`; для остальных — строки URL. Пустой массив, если не применимо. |
| model | Модель генерации (`KLING_3_0`, `SORA_2`, `SORA_2_WITH_IMAGE`, `SUNO_V5`, `NANO_BANANA_PRO`) или `null` для не-генераций |

При отсутствии пользователя возвращается `{ "balance": null, "items": [] }`.

---

## Типовой сценарий

1. **Загрузка изображений** (если нужны): `POST /upload` → получить `urls`
2. **Запуск генерации**: `POST /kling` (или `/sora2`, `/suno`, `/nanobanana`) с `userId`, `options` (в т.ч. `imageUrls`/`imageInput` из шага 1)
3. **Получение `taskId` и `balance`** из ответа — обновите отображение баланса на фронте
4. **Опрос результата**: `GET /result?userId=...&taskId=...` (polling или по событию)
5. **Отображение/сохранение** ссылок из `response.resultUrls`
6. **История** — по типу контента: `GET /history/video`, `/history/music`, `/history/image` с `userId`; в ответе — `balance` и `items`

---

## Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успех |
| 400 | Некорректный запрос (валидация, JSON) |
| 404 | Ресурс не найден (файл, результат задачи) |
| 500 | Внутренняя ошибка сервера (в т.ч. недостаточный баланс при генерации) |
