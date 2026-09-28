# Лабораторная работа 1. Защищённое REST API

Федеральное государственное автономное образовательное учреждение высшего образования «Национальный исследовательский университет ИТМО»

Дисциплина: Информационная безопасность  
Работа 1: Разработка защищенного REST API с интеграцией в CI/CD

ФИО: Амельченко Дмитрий Сергеевич  
Группа: P3431 
Год: 2026

## Описание

Учебное REST API на Spring Boot. Пользователь входит по логину и паролю и получает JWT. Без действующего токена закрытые методы недоступны. Данные хранятся в H2 через Spring Data JPA: пароли только в виде bcrypt-хэша, SQL собирается параметрами, а строки в ответах экранируются.

Стек: Java 25, Spring Boot 4.1, Spring Security, Spring Data JPA, H2, OWASP Java Encoder, SpotBugs + FindSecBugs, OWASP Dependency-Check.

## Запуск

Нужна JDK 25. Maven скачивается через обёртку, отдельно ставить его не нужно.

```powershell
.\mvnw.cmd spring-boot:run
```

Сервис слушает `http://localhost:8080`.

Учётные записи, которые создаются при старте:

| Логин | Пароль | Имя |
| --- | --- | --- |
| `student` | `Student#Lab1` | Студент ИТМО |
| `admin` | `Admin#Lab1` | Администратор |

Секрет подписи JWT и пароли можно переопределить переменными `JWT_SECRET`, `DEMO_STUDENT_PASSWORD` и `DEMO_ADMIN_PASSWORD`. Для учебного стенда в `application.yml` заданы отдельные демо-значения. В реальном сервисе секрет берётся из окружения и не хранится в репозитории.

Проверка сборки, тестов и статического анализа:

```powershell
.\mvnw.cmd verify
```

## API

Все ответы — JSON. Закрытые методы ждут заголовок `Authorization: Bearer <token>`.

### 1. `POST /auth/login`

Аутентификация. Тело запроса:

```json
{"username":"student","password":"Student#Lab1"}
```

Ответ `200`:

```json
{
  "token": "<jwt>",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600,
  "username": "student"
}
```

Неверный логин или пароль — `401` и одно и то же сообщение `Неверные учетные данные`. Так нельзя узнать, существует ли имя пользователя.

### 2. `GET /api/data`

Список объявлений. Доступен только с JWT.

Необязательный параметр `q` ищет подстроку в заголовке и тексте. Значение передаётся в запрос связанным параметром, а не склеивается со строкой SQL.

```powershell
curl.exe -s http://localhost:8080/api/data -H "Authorization: Bearer ТОКЕН"
curl.exe -s "http://localhost:8080/api/data?q=парол" -H "Authorization: Bearer ТОКЕН"
```

Без токена сервер отвечает `401`.

### 3. `POST /api/notes` и `GET /api/notes`

Свой метод: личные заметки.

`POST /api/notes` создаёт заметку текущего пользователя:

```json
{"title":"Заголовок","content":"Текст"}
```

`GET /api/notes` возвращает только заметки автора токена. Чужие записи этот метод не отдаёт: имя автора берётся из JWT, а не из параметра запроса.

## Меры защиты

### SQL-инъекции

Запросы идут через Spring Data JPA и Hibernate. Поиск объявлений записан как JPQL с параметром `:query`:

```java
@Query("""
        select a from Announcement a
        where lower(a.title) like lower(concat('%', :query, '%'))
           or lower(a.body) like lower(concat('%', :query, '%'))
        order by a.id
        """)
List<Announcement> searchByQuery(@Param("query") String query);
```

Hibernate отправляет значение отдельным параметром prepared statement. Строка `' OR '1'='1` остаётся текстом для поиска и не меняет условие запроса. Интеграционный тест проверяет, что такой поиск возвращает пустой список, а обычное слово `парол` находит одну запись.

### XSS

Пользовательский текст в ответах проходит через OWASP Java Encoder, метод `Encode.forHtml`. Символы `<`, `>` и кавычки превращаются в HTML-сущности. Если клиент вставит строку в HTML, браузер покажет её как текст, а не выполнит скрипт.

Пример: заголовок `<script>alert(1)</script>` в ответе выглядит как `&lt;script&gt;alert(1)&lt;/script&gt;`.

Дополнительно длина полей ограничена аннотациями Bean Validation, а в ответах сервера выставлены заголовки `X-Content-Type-Options: nosniff` и `Content-Security-Policy: default-src 'none'; frame-ancestors 'none'`.

### Аутентификация

- Пароль проверяется через `BCryptPasswordEncoder`. В базе лежит хэш, исходный пароль не хранится и не возвращается API.
- После успешного входа `JwtService` выпускает JWT с алгоритмом HS256. В токене есть `sub` (логин), `iat`, `exp` (1 час), `iss` и роль `ROLE_USER`. Подпись считается общим секретом сервера.
- `JwtAuthenticationFilter` — фильтр на защищённых запросах. Он читает `Authorization: Bearer`, проверяет подпись и срок через `JwtDecoder` и кладёт аутентификацию в `SecurityContext`. Неверная подпись, просроченный или пустой токен дают `401`.
- Сессия не создаётся (`SessionCreationPolicy.STATELESS`). HTTP Basic и форма входа выключены.
- CSRF для этого API отключён осознанно: браузер сам не подставляет заголовок `Authorization`, в отличие от cookie. Классическая CSRF-атака на cookie-сессию здесь не выполняется. Это зафиксировано в исключении SpotBugs `spotbugs-exclude.xml`.

Ошибки клиенту отдаются коротким JSON. Стек вызовов в ответ не попадает.

## CI/CD

Файл `.github/workflows/ci.yml` запускается на каждый push и pull request. Две независимые проверки:

1. **SAST.** `./mvnw verify` собирает проект, прогоняет тесты и SpotBugs с плагином FindSecBugs. Порог — Medium. Отчёт: `target/reports/spotbugs.html`. В Actions он прикладывается артефактом `spotbugs-report`.
2. **SCA.** Профиль Maven `sca` запускает OWASP Dependency-Check 13. База CVE берётся из готового кэша OWASP, отдельный ключ NVD не нужен. Сборка падает, если найдена уязвимость с CVSS от 9. HTML-отчёт прикладывается артефактом `dependency-check-report`.

## Приложения

1. https://github.com/DimaAmelchenkoG/InfoBezLab1 - git репозиторий
2. https://github.com/DimaAmelchenkoG/InfoBezLab1/actions/runs/36426440830 - ссылка на последний успешный запуск pipeline.
<img width="1070" height="505" alt="image" src="https://github.com/user-attachments/assets/1dc8e6e4-f3ef-4e96-839a-26f07f8ace30" />


### Скриншоты CI

- SAST
<img width="347" height="410" alt="image" src="https://github.com/user-attachments/assets/30e96152-a145-4ed9-b70f-f955c20dd941" />

- SCA
<img width="374" height="489" alt="image" src="https://github.com/user-attachments/assets/bce6d554-84af-4541-bfb6-00c8c7a193a4" />

