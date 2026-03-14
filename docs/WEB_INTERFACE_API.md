# Web Interface API — документация для фронтенда

Базовый путь: `{BASE_URL}/v1/web`

Все методы возвращают JSON, кроме `GET /files/{fileId}` (возвращает бинарный файл).

**Общее:** все входные параметры (в т.ч. `userId` в body и query) принимаются как **строки**; маппинг на типы выполняется в бэкенде. Для `userId` допустима строка вида `"123456789"`.

При успешной постановке задачи на генерацию (`POST /kling`, `/kling-motion-control`, `/sora2`, `/suno`, `/nanobanana`) в ответе возвращаются `taskId` и актуальный `balance` пользователя (после списания стоимости).

---

## 1. Загрузка файлов

**`POST /v1/web/upload`**

Загружает файлы (изображения и видео) и возвращает их публичные URL. Эти URL используются в полях `imageUrls` (Kling, Sora), `imageInput` (NanoBanana), а также `inputUrls` и `videoUrls` (Kling Motion Control) при запросах на генерацию.

### Request

- **Content-Type:** `multipart/form-data`
- **Поле:** `files` (массив файлов)

### Ограничения

- Форматы: JPEG, PNG, WebP (изображения); MP4, MOV (видео — для Kling Motion Control)
- Максимальный размер: изображения — 10 МБ, видео — 100 МБ
- Длительность видео: 3–30 секунд

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

URL формируется как `{baseUrl}{endpointVersion}/v1/web/files/{fileId}`, где:
- `baseUrl` — `web.uploaded-files-base-url` (по умолчанию из `telegram.bot.webhook-base-url`)
- `endpointVersion` — `telegram.bot.version-endpoint`: `dev-webhook` для версии разработки, `release-webhook` для релиза (может быть пустым)

**400 Bad Request** — текст ошибки (неверный формат, слишком большой файл и т.п.)

**500 Internal Server Error** — ошибка сервера

### Пример (JavaScript)

```javascript
const formData = new FormData();
formData.append('files', file1);
formData.append('files', file2);

const response = await fetch('/v1/web/upload', {
  method: 'POST',
  body: formData
});
const { urls } = await response.json();
```

---

## 2. Получение загруженного файла

**`GET /v1/web/files/{fileId}`**

Отдаёт содержимое файла по его ID. Полный URL формируется как `{baseUrl}{endpointVersion}/v1/web/files/{fileId}` (`endpointVersion`: `dev-webhook` или `release-webhook`, см. раздел «Загрузка файлов»).

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

**400 Bad Request** — текст ошибки (парсинг JSON, валидация `userId`, валидация длительности видео для Motion Control и т.п.)

При ошибке постановки задачи возвращается JSON `{code, description}` и соответствующий HTTP-статус:

| HTTP | Код | Описание |
|------|-----|----------|
| 402 | E004 | Недостаточно средств на балансе. Пожалуйста, пополните баланс. |
| 400 | E005, E006 | Некорректные данные или ошибка загрузки файлов |
| 429 | E009 | Слишком много запросов |
| 502 | E001, E002, E003, E007, E010, E011 | Ошибка генерации или сервис временно недоступен |
| 500 | E008 | Внутренняя ошибка сервера |

---

## 3.1. Генерация Kling 3.0 Motion Control

**`POST /v1/web/kling-motion-control`**

Запускает генерацию видео через Kling 3.0 Motion Control (kie.ai) — перенос движения из референсного видео на изображение персонажа. Доступно только из web-интерфейса.

### Request

- **Content-Type:** `application/json`
- **Body:**

```json
{
  "userId": "123456789",
  "options": {
    "inputUrls": ["https://..."],
    "videoUrls": ["https://..."],
    "prompt": "Описание желаемого результата",
    "characterOrientation": "video",
    "mode": "720p"
  }
}
```

| Поле | Тип | Обязательно | Описание |
|------|-----|-------------|----------|
| userId | string | да | User.telegramId (строка) |
| options.inputUrls | string[] | да | URL референсного изображения персонажа (результат `/upload`, JPEG/PNG/JPG) |
| options.videoUrls | string[] | да | URL референсного видео с движением (результат `/upload`, MP4/MOV, 3–30 сек) |
| options.prompt | string | нет | Текстовое описание (0–2500 символов) |
| options.characterOrientation | string | нет | `"image"` — ориентация по изображению (видео 3–10 сек); `"video"` — по видео (3–30 сек). По умолчанию `"video"` |
| options.mode | string | нет | Разрешение: `"720p"` (std) или `"1080p"` (pro). По умолчанию `"720p"` |

### Response

**200 OK**
```json
{
  "taskId": "abc123-xyz-456",
  "balance": 850
}
```

**400 Bad Request** — при неверной длительности видео:
- orientation «image» и видео > 10 сек: `"При orientation «image» видео должно быть 3–10 секунд. Ваше видео — X сек."`
- orientation «video» и видео не в диапазоне 3–30 сек: `"Видео должно быть 3–30 секунд. Ваше видео — X сек."`
- не удалось прочитать длительность: `"Не удалось определить длительность видео. Проверьте формат файла (MP4, MOV)."`

**400 / 500** — см. Kling

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

**200 OK** (Kling, Kling Motion Control, Sora, NanoBanana — объекты с полем `url`):
```json
{
  "resultUrls": [
    { "url": "https://cdn.example.com/result/video123.mp4" }
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
      "text": "Текст музыкального трека (lyrics)"
    }
  ],
  "model": "SUNO_V5",
  "balanceChange": -50,
  "options": { "prompt": "...", ... }
}
```

| Поле | Описание |
|------|----------|
| resultUrls | Массив объектов. Для Kling/Kling Motion Control/Sora/NanoBanana — `[{url}]`. Для SUNO_V5 — `[{audioUrl, imageUrl, title, text}]` (text — текст трека/lyrics). |
| model | Модель генерации (`KLING_3_0`, `KLING_3_MOTION_CONTROL`, `SORA_2`, `SUNO_V5`, `NANO_BANANA_PRO` и др.) |
| balanceChange | Цена генерации (отрицательное число — списание с баланса) |
| options | Опции, с которыми была запущена генерация |

**200 OK** (при ошибке задачи) — если задача завершилась с ошибкой:
```json
{
  "code": "E011",
  "description": "Генерация не завершилась. Попробуйте снова."
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

## Механика обработки ошибок

Ниже описано для каждого типа запроса: **что вызывает ошибку**, **во что мы её оборачиваем** и **что возвращаем на фронтенд**.

### Общая схема

- **ErrorResponseDTO** — JSON `{ "code": "E00X", "description": "Описание" }`. Используется для бизнес-ошибок.
- **Текстовая ошибка** — строка в теле ответа (для валидации, парсинга).
- **Пустое тело** — 404/500 без JSON (для некоторых случаев).

---

### POST /upload — загрузка файлов

| Причина | Обработка | Ответ на фронтенд |
|---------|-----------|-------------------|
| Недопустимый формат файла | `IllegalArgumentException` | **400** — текст: `"Недопустимый формат файла. Разрешены: изображения (JPEG, PNG, WebP), видео (MP4, MOV)."` |
| Изображение превышает 10 МБ | `IllegalArgumentException` | **400** — текст: `"Изображение слишком большое. Максимальный размер — 10 МБ. Ваш файл — X МБ."` |
| Видео превышает 100 МБ | `IllegalArgumentException` | **400** — текст: `"Видео слишком большое. Максимальный размер — 100 МБ. Ваш файл — X МБ."` |
| Видео не 3–30 сек | `IllegalArgumentException` | **400** — текст: `"Видео должно быть длительностью 3–30 секунд. Ваше видео — X сек."` |
| Не удалось прочитать длительность видео | `IllegalArgumentException` | **400** — текст: `"Не удалось определить длительность видео. Проверьте формат файла (MP4, MOV)."` |
| Spring multipart отклоняет (файл > 100 МБ) | `MaxUploadSizeExceededException` | **400** — текст: `"Файл слишком большой. Изображения — до 10 МБ, видео — до 100 МБ."` |
| Любое другое исключение (IO, security) | `Exception` | **500** — пустое тело |

---

### GET /files/{fileId} — получение файла

| Причина | Обработка | Ответ на фронтенд |
|---------|-----------|-------------------|
| Файл не найден или удалён | `resource == null \|\| !exists()` | **404** — пустое тело |
| Любое исключение при раздаче | `Exception` | **500** — пустое тело |

---

### POST /kling, /kling-motion-control, /sora2, /suno, /nanobanana — постановка задачи на генерацию

| Причина | Обработка | Ответ на фронтенд |
|---------|-----------|-------------------|
| Невалидный JSON в body | `JsonProcessingException` | **400** — текст из `e.getMessage()` |
| `userId` отсутствует или не число | `IllegalArgumentException` | **400** — текст: `"userId is required"` или `"userId must be a valid number"` |
| Валидация (длительность видео для Motion Control и т.п.) | `IllegalArgumentException` | **400** — текст сообщения об ошибке |
| Недостаточно баланса | `SubmitOutcome.fail(E004)` | **402** — `{ "code": "E004", "description": "Недостаточно средств на балансе" }` |
| Сервис генерации недоступен или ошибка при создании задачи | `SubmitOutcome.fail(E007)` | **502** — `{ "code": "E007", "description": "Сервис генерации временно недоступен. Пожалуйста, обратитесь в поддержку." }` |
| Необработанное исключение в цепочке | `SubmitOutcome.fail(E008)` | **500** — `{ "code": "E008", "description": "Произошла внутренняя ошибка. Пожалуйста, обратитесь в поддержку." }` |

E005, E006, E009 при постановке задачи из веб-интерфейса не используются (E005/E006 — чат, E009 — rate limit в чате).

---

### GET /result — получение результата задачи

| Причина | Обработка | Ответ на фронтенд |
|---------|-----------|-------------------|
| Задача успешно завершена | `WebGenerateResponse` | **200** — `{ resultUrls, model, balanceChange, options }` |
| Задача завершилась с ошибкой (callback) | `ErrorResponseDTO` из `TaskErrorRegistry` | **200** — `{ "code": "E00X", "description": "..." }` |
| Задача не найдена / ещё в работе / другой userId | `Optional.empty()` | **404** — пустое тело |
| `userId` невалиден | `IllegalArgumentException` | **400** — текст: `"userId is required"` или `"userId must be a valid number"` |

Коды в `getTaskResult`: E001 (видео), E002 (музыка), E003 (изображения), E007 (parse failure), E011 (callback failed) — см. CallbackHandler.

---

### GET /user — информация о пользователе

| Причина | Обработка | Ответ на фронтенд |
|---------|-----------|-------------------|
| Пользователь найден | `Map` с balance, ambassador | **200** — `{ "balance": 850, "ambassador": false }` |
| Пользователь не найден | `Optional.empty()` | **404** — пустое тело |
| `userId` невалиден | `IllegalArgumentException` | **400** — текст ошибки |

---

### GET /history/video, /history/music, /history/image — история операций

| Причина | Обработка | Ответ на фронтенд |
|---------|-----------|-------------------|
| Успех | `HistoryResponseDTO` | **200** — `{ "balance": 850, "items": [...] }` |
| `userId` невалиден | `IllegalArgumentException` | **400** — текст ошибки |

При отсутствии пользователя: `{ "balance": null, "items": [] }` (200 OK).

---

### Маппинг ErrorCode → HTTP и body

При использовании `ErrorResponseDTO` (SubmitOutcome, getTaskResult):

| ErrorCode | HTTP | Тело |
|-----------|------|------|
| E004 | 402 | `{ "code": "E004", "description": "Недостаточно средств на балансе. Пожалуйста, пополните баланс." }` |
| E005, E006 | 400 | `{ "code": "E005/E006", "description": "..." }` |
| E009 | 429 | `{ "code": "E009", "description": "Слишком много запросов. Подождите немного" }` |
| E001, E002, E003, E007, E010, E011 | 502 | `{ "code": "E00X", "description": "..." }` — см. таблицу кодов ниже |
| E008 | 500 | `{ "code": "E008", "description": "Произошла внутренняя ошибка. Пожалуйста, обратитесь в поддержку." }` |

---

### Рекомендации для фронтенда

1. Проверять HTTP-статус; при 4xx/5xx — читать тело.
2. При 400/402/429/500/502 с JSON — парсить `code` и `description` для пользовательских сообщений.
3. При 400 с текстом (upload, parse, валидация) — показывать тело ответа как есть.
4. При 404 — отображать «ресурс не найден»; тело обычно пустое.

---

### Коды ошибок (ErrorCode)

| Код | Описание |
|-----|----------|
| E001 | Не удалось сгенерировать видео. Пожалуйста, обратитесь в поддержку. |
| E002 | Не удалось сгенерировать музыку. Пожалуйста, обратитесь в поддержку. |
| E003 | Не удалось сгенерировать изображение. Пожалуйста, обратитесь в поддержку. |
| E004 | Недостаточно средств на балансе. Пожалуйста, пополните баланс. |
| E005 | Проверьте введённые данные и попробуйте снова. |
| E006 | Не удалось загрузить файлы. Проверьте формат и размер. |
| E007 | Сервис генерации временно недоступен. Пожалуйста, обратитесь в поддержку. |
| E008 | Произошла внутренняя ошибка. Пожалуйста, обратитесь в поддержку. |
| E009 | Слишком много запросов. Подождите немного. |
| E010 | Не удалось отправить результат. Пожалуйста, обратитесь в поддержку. |
| E011 | Генерация не завершилась. Попробуйте снова. |

---

## 8. История операций

История разделена по типу контента. Каждый эндпоинт возвращает текущий баланс пользователя и список операций (успешных и в процессе), отсортированный по дате по убыванию. В список попадают записи со статусом **SUCCESS** и **PROCESSING** (генерации в процессе).

### 8.1. История видео (Sora, Kling, Kling Motion Control)

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
        { "url": "https://cdn.example.com/result/video123.mp4" }
      ],
      "model": "KLING_3_0",
      "status": "SUCCESS",
      "taskId": "abc123-xyz-456"
    },
    {
      "options": { "prompt": "...", ... },
      "balanceChange": null,
      "date": "2025-03-05T14:25:00+03:00",
      "resultUrls": [],
      "model": "SORA_2",
      "status": "PROCESSING",
      "taskId": "def456-uvw-789"
    }
  ]
}
```

| Поле | Описание |
|------|----------|
| balance | Текущий баланс пользователя |
| items | Список операций (SUCCESS и PROCESSING), отсортирован по дате по убыванию |

Элемент в `items`:

| Поле | Описание |
|------|----------|
| options | Опции запроса (параметры генерации), объект |
| balanceChange | Изменение баланса (отрицательное при списании). null для записей со статусом PROCESSING |
| date | Дата и время операции (ISO 8601) |
| resultUrls | Массив объектов. Для Kling/Kling Motion Control/Sora/NanoBanana — `[{url}]`; для SUNO_V5 — `[{audioUrl, imageUrl, title, text}]`. Пустой массив для PROCESSING |
| model | Модель генерации (`KLING_3_0`, `KLING_3_MOTION_CONTROL`, `SORA_2`, `SORA_2_WITH_IMAGE`, `SUNO_V5`, `NANO_BANANA_PRO`) или `null` для не-генераций |
| status | Статус генерации: `SUCCESS`, `PROCESSING`. `null` для старых записей |
| taskId | ID задачи для опроса результата. `null` для старых записей |

При отсутствии пользователя возвращается `{ "balance": null, "items": [] }`.

**Жизненный цикл записи генерации (статусы):**
- `REQUESTED` — запрос отправлен, ожидание подтверждения
- `PROCESSING` — генерация в процессе (есть `taskId`)
- `SUCCESS` — генерация завершена, есть `resultUrls`
- `FAILED` — ошибка на любом этапе

В API истории возвращаются только записи со статусом `SUCCESS` и `PROCESSING`. По `taskId` можно опрашивать результат через `GET /result`.

---

## Типовой сценарий

1. **Загрузка файлов** (если нужны): `POST /upload` с полем `files` → получить `urls`
2. **Запуск генерации**: `POST /kling` (или `/kling-motion-control`, `/sora2`, `/suno`, `/nanobanana`) с `userId`, `options` (в т.ч. `imageUrls`/`imageInput`/`inputUrls`+`videoUrls` из шага 1)
3. **Получение `taskId` и `balance`** из ответа — обновите отображение баланса на фронте
4. **Опрос результата**: `GET /result?userId=...&taskId=...` (polling или по событию)
5. **Отображение/сохранение** ссылок из `response.resultUrls`
6. **История** — по типу контента: `GET /history/video`, `/history/music`, `/history/image` с `userId`; в ответе — `balance` и `items` (с полями `status`, `taskId` для отображения завершённых и текущих генераций)

---

## Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успех |
| 400 | Некорректный запрос (валидация, JSON, параметры) |
| 402 | Недостаточный баланс (E004) |
| 404 | Ресурс не найден (файл, результат задачи, пользователь) |
| 429 | Превышен лимит запросов (E009) |
| 500 | Внутренняя ошибка сервера (E008) |
| 502 | Ошибка генерации или сервис временно недоступен (E001, E002, E003, E007, E010, E011) |
