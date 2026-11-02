# Habr & Theatre Parser

CLI-приложение на Java для сбора данных со статей [habr.com](https://habr.com)
и афиш двух театров Кирова: «Театра на Спасской» и «Драмтеатра им. С.М. Кирова».

## Возможности

- **Хабр**: заголовок, автор, аватар, дата публикации, время чтения,
  просмотры, сложность, хабы, метки, превью, обложка
- **Театры**: название спектакля, даты сеансов, возрастное ограничение,
  продолжительность, постер
- Асинхронная загрузка карточек через `CompletableFuture` + `Semaphore`
- Retry-политика при сетевых ошибках (3 попытки)
- Скачивание обложек
- Экспорт в JSON

## Стек

Java 17 · Maven · Jsoup · Jackson · SLF4J

## Запуск

```bash
git clone https://github.com/rayoraq/habr-theatre-parser.git
cd habr-theatre-parser
mvn clean package

# Хабр, 3 страницы по умолчанию
java -jar target/habr-theatre-parser-1.0.0.jar 1 3

# Театры Кирова
java -jar target/habr-theatre-parser-1.0.0.jar 2
```
Результат: output/publications.json, output/theatres.json, обложки — в images/

ekvus-kirov.ru блокирует анонимные запросы и возвращает HTTP connect timed out. 
Чтобы это обойти, создайте в корне проекта файл application-local.properties:
```properties
cookies.ekvus-kirov.ru = PHPSESSID=...; _ym_uid=...; _ym_d=...
cookies.kirovdramteatr.ru = PHPSESSID=...; _ym_uid=...; _ym_d=...
```
Куки берутся из браузера: DevTools → Network → любой запрос → Request Headers → Cookie
