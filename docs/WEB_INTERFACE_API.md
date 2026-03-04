# Web Interface API — документация для фронтенда

Базовый путь: `{BASE_URL}/v1/web`

Все методы возвращают JSON, кроме `GET /files/{fileId}` (возвращает бинарный файл).

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
  "userName": "telegram_username",
  "options": {
    "aspectRatio": "9:16",
    "duration": 10,
    "withSound": false,
    "mode": "std",
    "multiShots": false,
    "imageUrls": ["https://..."],
    "prompt": "Текстовое описание сцены",
    "multiShotRequestArray": [
      { "prompt": "Описание кадра", "duration": 5 }
    ]
  }
}
```

| Поле | Тип | Обязательно | Описание |
|------|-----|-------------|----------|
| userName | string | да | Telegram username пользователя (без @) |
| options.aspectRatio | string | нет | Соотношение сторон: `"9:16"`, `"16:9"` и др. |
| options.duration | number | нет | Длительность в секундах (по умолчанию 10) |
| options.withSound | boolean | нет | Добавлять ли звук |
| options.mode | string | нет | Режим: `"std"`, `"pro"` |
| options.multiShots | boolean | нет | Мультикадровый режим |
| options.imageUrls | string[] | нет | URL изображений (результат `/upload`) |
| options.prompt | string | да | Промпт для генерации |
| options.multiShotRequestArray | array | нет | Массив `{prompt, duration}` для multiShots |

### Response

**200 OK**
```json
{
  "taskId": "abc123-xyz-456"
}
```

**400 Bad Request** — текст ошибки парсинга JSON

**500 Internal Server Error** — ошибка постановки задачи

---

## 4. Генерация Sora 2

**`POST /v1/web/sora2`**

Запускает генерацию видео через модель Sora 2.

### Request

- **Content-Type:** `application/json`
- **Body:**

```json
{
  "userName": "telegram_username",
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
| userName | string | да | Telegram username пользователя |
| options.prompt | string | да | Промпт для генерации |
| options.aspectRatio | string | нет | `"9:16"`, `"16:9"` |
| options.nFrames | string | нет | Количество кадров (по умолчанию "10") |
| options.imageUrls | string[] | нет | URL изображений для image-to-video |

### Response

**200 OK**
```json
{
  "taskId": "abc123-xyz-456"
}
```

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
  "userName": "telegram_username",
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
| userName | string | да | Telegram username пользователя |
| options.customMode | boolean | нет | Режим кастомизации |
| options.prompt | string | да | Описание желаемой музыки |
| options.instrumental | boolean | нет | Инструментальная композиция |
| options.audioWeight | number | нет | Вес аудио (если применимо) |
| options.genre | string | нет | Жанр: `"pop"`, `"rock"` и др. |

### Response

**200 OK**
```json
{
  "taskId": "abc123-xyz-456"
}
```

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
  "userName": "telegram_username",
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
| userName | string | да | Telegram username пользователя |
| options.prompt | string | да | Промпт для генерации |
| options.imageInput | string[] | нет | URL изображений (результат `/upload`) |
| options.aspectRatio | string | нет | Соотношение сторон |
| options.resolution | string | нет | Разрешение (по умолчанию "2K") |
| options.outputFormat | string | нет | Формат: `"png"`, `"jpg"` и др. |

### Response

**200 OK**
```json
{
  "taskId": "abc123-xyz-456"
}
```

**400 / 500** — см. Kling

---

## 7. Получение результата задачи

**`GET /v1/web/result`**

Возвращает результат завершённой генерации по `userName` и `taskId`. Запрос имеет смысл только после того, как генерация завершена (успешно или с ошибкой).

### Request

- **Query параметры:**
  - `userName` (string) — Telegram username пользователя
  - `taskId` (string) — ID задачи из ответа методов generate

### Response

**200 OK**
```json
{
  "links": [
    "https://cdn.example.com/result/video123.mp4"
  ],
  "options": {
    "aspectRatio": "9:16",
    "duration": 10,
    "prompt": "Описание сцены",
    ...
  }
}
```

| Поле | Описание |
|------|----------|
| links | Массив URL на результат (видео/аудио/изображения) |
| options | Опции, с которыми была запущена генерация |

**404 Not Found** — задача не найдена, ещё не завершена или `userName` не совпадает

> Результат можно запросить только один раз. После успешного ответа он удаляется с сервера.

---

## Типовой сценарий

1. **Загрузка изображений** (если нужны): `POST /upload` → получить `urls`
2. **Запуск генерации**: `POST /kling` (или `/sora2`, `/suno`, `/nanobanana`) с `userName`, `options` (в т.ч. `imageUrls`/`imageInput` из шага 1)
3. **Получение `taskId`** из ответа
4. **Опрос результата**: `GET /result?userName=...&taskId=...` (polling или по событию)
5. **Отображение/сохранение** ссылок из `response.links`

---

## Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успех |
| 400 | Некорректный запрос (валидация, JSON) |
| 404 | Ресурс не найден (файл, результат задачи) |
| 500 | Внутренняя ошибка сервера |
