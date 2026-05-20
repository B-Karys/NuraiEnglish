# WordLy — Руководство по запуску проекта

Пошаговая инструкция: от клонирования репозитория до работающего приложения.

---

## Что потребуется

| Инструмент | Версия | Зачем |
|---|---|---|
| [Git](https://git-scm.com/) | любая | клонирование репозитория |
| [Node.js](https://nodejs.org/) | 18 или выше | запуск почтового сервера |
| [Android Studio](https://developer.android.com/studio) | Hedgehog или новее | сборка и запуск приложения |
| Аккаунт [Firebase](https://console.firebase.google.com/) | — | база данных и авторизация |
| Аккаунт Gmail | — | отправка кодов подтверждения |

---

## Шаг 1. Клонирование репозитория

```bash
git clone https://github.com/ВАШ_АККАУНТ/НазваниеРепозитория.git
cd НазваниеРепозитория
```

Структура проекта:

```
НазваниеРепозитория/
├── app/          ← Android-приложение
├── backend/      ← Node.js почтовый сервер
├── SETUP.md      ← этот файл
└── GUIDE_RU.md   ← руководство пользователя
```

---

## Шаг 2. Настройка Firebase

### 2.1. Создайте проект

1. Перейдите на [console.firebase.google.com](https://console.firebase.google.com/)
2. **«Добавить проект»** → введите название → **«Создать проект»**

### 2.2. Подключите Android-приложение

1. В консоли нажмите иконку **Android**
2. Имя пакета: `com.example.nuraienglish` → **«Зарегистрировать приложение»**
3. Скачайте **`google-services.json`**
4. Замените файл-заглушку в проекте:
   ```
   app/google-services.json  ← заменить этим файлом
   ```

### 2.3. Включите Email/Password авторизацию

**Authentication → Sign-in method → «Адрес электронной почты и пароль»** → включить → **«Сохранить»**

### 2.4. Создайте Firestore

**Firestore Database → «Создать базу данных»** → выберите регион → **«Включить»**

### 2.5. Правила безопасности

**Firestore Database → Правила** → вставьте и нажмите **«Опубликовать»**:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

## Шаг 3. Почтовый сервер

Сервер отправляет коды подтверждения через Gmail SMTP.  
Выберите **один** из двух вариантов запуска:

---

### Вариант A — облако Render (рекомендуется, работает везде)

> Используйте этот вариант, если локальный запуск выдаёт ошибку
> `ECONNREFUSED` или `EHOSTUNREACH` — многие сети и провайдеры блокируют
> SMTP-порты 465/587.

#### A-1. Получите App Password Gmail

1. Включите двухэтапную аутентификацию в Google-аккаунте
2. Перейдите: [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
3. Название: `WordLy` → **«Создать»**
4. Скопируйте 16-значный код

#### A-2. Создайте аккаунт на Render

1. Откройте [render.com](https://render.com) → **«Get Started for Free»**
2. Войдите через GitHub

#### A-3. Разверните сервис

1. **New → Web Service**
2. Подключите ваш GitHub-репозиторий с проектом
3. Настройки сервиса:
   | Поле | Значение |
   |---|---|
   | Name | `wordly-mailer` |
   | Root Directory | `backend` |
   | Runtime | `Node` |
   | Build Command | `npm install` |
   | Start Command | `npm start` |
   | Instance Type | `Free` |
4. Нажмите **«Advanced»** → **«Add Environment Variable»** — добавьте:
   | Key | Value |
   |---|---|
   | `GMAIL_USER` | ваш\_адрес@gmail.com |
   | `GMAIL_PASS` | 16-значный App Password |
5. Нажмите **«Deploy Web Service»**
6. Подождите 2–3 минуты. Когда статус станет **Live**, скопируйте URL вида:
   ```
   https://wordly-mailer.onrender.com
   ```

#### A-4. Укажите Render URL в приложении

Файл: `app/src/main/java/com/example/nuraienglish/core/data/remote/VerificationRepository.kt`

```kotlin
// Замените на свой Render URL:
private val baseUrl = "https://wordly-mailer.onrender.com"
```

> При использовании Render HTTPS работает автоматически —
> изменять `network_security_config.xml` не нужно.

---

### Вариант B — локальный запуск (только для разработки на том же ПК)

> Требует, чтобы сеть не блокировала порт 587.

#### B-1. Получите App Password Gmail

(те же шаги, что в A-1)

#### B-2. Создайте `.env`

```bash
cd backend
cp .env.example .env
```

Заполните `.env`:

```env
PORT=3000
GMAIL_USER=ваш_адрес@gmail.com
GMAIL_PASS=abcdabcdabcdabcd
```

> `GMAIL_PASS` — 16-значный App Password без пробелов, не обычный пароль.

#### B-3. Запустите сервер

```bash
npm install
npm start
```

Результат:
```
WordLy mailer listening on http://localhost:3000
```

> Оставьте терминал открытым — сервер должен работать во время использования приложения.

#### B-4. Укажите адрес сервера в приложении

Файл: `app/src/main/java/com/example/nuraienglish/core/data/remote/VerificationRepository.kt`

```kotlin
private val baseUrl = "http://10.0.2.2:3000"
```

- **Эмулятор** — оставьте `10.0.2.2` (это localhost для эмулятора)
- **Реальное устройство** — замените на IP вашего компьютера:
  ```kotlin
  private val baseUrl = "http://192.168.1.XXX:3000"
  ```
  Узнать IP: `ipconfig` (Windows) или `ifconfig` (macOS/Linux)

---

## Шаг 4. Настройка приложения

### 4.1. Откройте проект в Android Studio

**File → Open** → выберите корневую папку проекта → дождитесь синхронизации Gradle.

### 4.3. Укажите email администратора

Файл: `app/src/main/java/com/example/nuraienglish/feature/home/HomeViewModel.kt`

```kotlin
private val ADMIN_EMAILS = setOf(
    "ваш_email@gmail.com"  // ← замените на свой email
)
```

### 4.4. Запустите приложение

Нажмите **Run ▶** → выберите устройство или эмулятор.

---

## Шаг 5. Первый вход как администратор

1. В приложении нажмите **«Регистрация»**
2. Введите email из `ADMIN_EMAILS`
3. Нажмите **«Отправить код подтверждения»** — проверьте почту
4. Введите 6-значный код → подтвердите
5. Введите имя и пароль → **«Создать аккаунт»**
6. На главном экране появится кнопка **«Admin Panel»**

---

## Шаг 6. Наполнение данными

**Admin Panel → вкладка «Seed Data» → «Create Sample Data»**

Создаст 4 готовых курса со всеми типами уроков и заданий.

Подробнее о ручном добавлении контента — в [`GUIDE_RU.md`](./GUIDE_RU.md).

---

## Частые проблемы

| Ошибка | Решение |
|---|---|
| `ECONNREFUSED` / `EHOSTUNREACH` при отправке кода | Сеть блокирует SMTP — используйте Вариант A (Render) |
| `Failed to send email` (локальный сервер) | Сервер не запущен или неверный App Password в `.env` |
| Код не приходит на почту, но сервер работает | Проверьте спам; убедитесь, что App Password верный |
| `PERMISSION_DENIED` | Проверьте правила Firestore (Шаг 2.5) и файл `google-services.json` |
| Пустой список курсов | Потяните экран вниз для обновления |
| Кнопка «Admin Panel» не появляется | Проверьте `ADMIN_EMAILS` в `HomeViewModel.kt` и пересоберите |
| Ошибка Gradle при синхронизации | **File → Sync Project with Gradle Files** |
| Render сервис засыпает после 15 минут (Free tier) | Первый запрос после паузы идёт ~30 сек — это нормально |

---

## Ежедневный запуск

### Если используете Render (Вариант A):
```
# Сервер работает в облаке постоянно — ничего запускать не нужно.
# Просто откройте Android Studio и нажмите Run ▶
```

### Если используете локальный сервер (Вариант B):
```bash
# 1. Запустить почтовый сервер
cd backend && npm start

# 2. Открыть Android Studio и нажать Run ▶
```
