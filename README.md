# FandoMatch
Дипломная работа студентки НИУ ВШЭ ФКН ПИ 2026 года Судаковой Дарьи Евгеньевны.

## Описание
Серверная часть проекта FandoMatch

## Как запустить DEV / LOCAL среду
0. _**Предусловия:**_  Наличие скачанных PGadmin, docker desktop, опциональьно bruno для запросов к серверу
1. Склонировать репозиторий
2. Сгенерировть API клиенты. Для этого из корня проекта запускаем команду  ``.\gradlew.bat openApiGenerate``
3. Создать базы данных. Для этого в PG Admin создаем БД с названиями и паролями ровно как указано в application.yaml каждого из сервисов в папке ``services``. Создаем таблицы с помощью SQL скриптов из папок `services/{service_name}/resources/db.migration`
4. Запускаем кафку с помощью команды из корня проекта ``docker compose up --build kafka``
5. Стартуем приложения в таком порядке: users, core, gateway. Для того чтобы нужный профиль подтянулся, делаем следующее: в конфигурации в Intelij Idea нажимаем `edit` -> `VM Options` -> вставляем строчку `-Dspring.profiles.active=local` -> `apply`  ![img.png](img.png) ![img_1.png](img_1.png)
   Так делаем для каждой конфигурации и потом запускаем. 
6. Локальный профиль готов! Можно делать запросы, вероятно только через postman/bruno

## Как запустить UAT среду
**ВАЖНО!** Обязательно нужно выключить все прокси/ВПН перед запуском, иначе не получится запустить докер.
0. _**Предусловия:**_ Запускаем/скачиваем [Ехидного кита для десктопа](https://www.docker.com/products/docker-desktop/).
1. Далее в корень проекта нужно положить файл под названием ``.env`` с секретами. Секреты я дам.
2. _**(Опционально, в случае ошибок в следующих пунктах - иначе можно смело скипать)**_ Убеждаемся что в папках ``clients/..`` лежит папка ``build``. Ее я не дам, ее нужно сгенерить командой ``.\gradlew.bat openApiGenerate``
2. Запускаем из корня проекта команду:
   - ``docker compose up --build`` - если билдим в первый раз
   - ``docker compose up`` - если уже сегодня сбилдили и не хотим ждать снова/если нет изменений в репозитории и образ актуален
   - ``docker compose up -d`` - если не хотим видеть логи добавляем к любой из команд флаг -d
Когда проект поднимется - можно делать запросы через bruno (его я тоже могу дать), например, или с вашего мобильного клиента.
Тут сохраняются тестовые пользователи

## PROD-среда
По URL: https://xsqs-1dmk-iemo.gw-1a.dockhost.net/{your_path}

## Администрирование

Продовая Grafana: https://xsqs-1dmk-iemo.gw-1a.dockhost.net/grafana/login
Уатная Grafana: http://localhost:3000/d/famz8b4/fdmatch-uat?orgId=1&from=now-2d&to=now&timezone=browser&var-application=core&refresh=5s


### Как генерить open api спеки?
Командой ``.\gradlew.bat openApiGenerate``
Иногда нужно две попытки для успешной генерации


### Как запустить тесты
./gradlew clean test jacocoTestReport (--continue)  
./gradlew :services:users:clean :services:users:test :services:users:jacocoTestReport


### Как запустить python скрипт для ваншота заполнения фандомов

Этот скрипт парсит фандомы со ссылок в Википедии

```
cd ~/FandoMatch/scripts          
python3 -m venv venv             
source venv/bin/activate       
pip install requests beautifulsoup4   
python fandom-oneshot.py        
```

### Полезные команды

- Если нужно выполнить SQL напрямую в контейенре БД:
```
psql -h core-db -U postgres -d fdmatch_core
ваш SQL-скрипт
```

- Скрипт для дропа всех таблиц
```
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO public;
DELETE FROM public.match_action WHERE user_id = '408fea1c-6991-4148-9a13-14ce708a5496';
```

- Вставка фандомов вручную (не надо так, для этого есть ручка fandom_oneshot)
```
INSERT INTO fandom (name, category_id) VALUES
    ('One Piece',           (SELECT id FROM fandom_category WHERE name = 'ANIME_MANGA')),
    ('Но-Энор',             (SELECT id FROM fandom_category WHERE name = 'OTHER')),
    ('My Chemical Romance', (SELECT id FROM fandom_category WHERE name = 'MUSIC')),
    ('Ведьмак',             (SELECT id FROM fandom_category WHERE name = 'BOOKS')),
    ('Утиные истории',      (SELECT id FROM fandom_category WHERE name = 'CARTOONS')),
    ('Бэтмен',              (SELECT id FROM fandom_category WHERE name = 'COMICS')),
    ('Star Wars',           (SELECT id FROM fandom_category WHERE name = 'TV_SERIES')),
    ('Dungeons and Dragons',(SELECT id FROM fandom_category WHERE name = 'TABLETOP_GAMES')),
    ('The Beatles',         (SELECT id FROM fandom_category WHERE name = 'MUSIC')),
    ('Стража! Стража!',     (SELECT id FROM fandom_category WHERE name = 'BOOKS')),
    ('Легенды Олимпа',      (SELECT id FROM fandom_category WHERE name = 'MYTHOLOGY')),
    ('Волкодав',            (SELECT id FROM fandom_category WHERE name = 'BOOKS'));
```
