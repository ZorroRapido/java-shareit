# ShareIt
**ShareIt** - сервис для шеринга вещей (англ. share - "делиться"). В проекте реализовано REST API для выполнения различных операций с пользователями и вещами.

## Особенности
Использованный стэк:
- Java 11 (сборка Amazon Corretto)
- Spring Boot (v. 2.7.2)
- Lombok
- MapStruct (v. 1.5.3.Final)

## Описание приложения

Пользователи могут добавлять новые вещи на площадку, а также осуществлять поиск по каталогу вещей. Пользователь, который добавляет в приложение новую вещь, считается её владельцем. При добавлении вещи указывается её краткое название и краткое описание. К примеру, название может быть — «Дрель “Салют”», а описание — «Мощность 600 вт, работает ударный режим, так что бетон возьмёт». Также у каждой вещи есть статус — доступна ли она для аренды (available = True/False). Статус может проставлять только владельц.

На текущий момент в проекте реализованы две сущности и API для взаимодействия с ними:
- User (пользователь)
- Item (вещь)

## Описание endpoint'ов

### User (/users)

- **Создание нового пользователя**
```
POST /users
```
В теле запроса указываются имя (name) и электронная почта (email) пользователя.

*Пример:*
```
{
    "name": "user",
    "email": "user@user.com"
}
```

- **Обновление данных пользователя**
```
PATCH /users/:userId
```
*userId* - уникальный идентификатор пользователя


В теле запроса указываются поля, которые требуется обновить - name/email.

*Пример:*
```
{
    "email": "update@user.com"
}
```

- **Получение данных пользователя**
```
GET /users/:userId
```
*userId* - уникальный идентификатор пользователя

- **Удаление пользователя**
```
DELETE /users/:userId
```
*userId* - уникальный идентификатор пользователя

### Item (/items)

**Примечание**: во всех следующих запросах следует передавать заголовок **X-Sharer-User-Id** (id отправителя). Он нужен для определения пользователя, который отправляет запрос. Если заголовок не будет передан, будет выбрашено сообтветствующее исключение и пользователь получит ошибку.

- **Добавление вещи**
```
POST /items
```
В теле запроса указывается название вещи (name), краткое описание (description), возможно ли арендовать вещь (available).

*Пример:*
```
{
    "name": "Дрель",
    "description": "Простая дрель",
    "available": true
}
```

- **Редактирование параметров вещи**
```
PATCH /items/:itemId
```
*itemId* - уникальный идентификатор вещи, для которой требуется поменять параметры

В теле запроса указываются параметры вещи, которые требуется обновить - name/description/available.

*Пример:*
```
{
    "name": "Дрель",
    "description": "Простая дрель",
    "available": false
}
```
- **Получение вещи по id**
```
GET /items/:itemId
```
*itemId* - уникальный идентификатор вещи, информацию о которой требуется вернуть. Информацию о вещи по этому endpoint может получить только владелей веши (owner). Другие пользователи могут искать вещи с помощью следующего [endpoint](#search).

- **Получение всех своих вещей**
```
GET /items
```
Пользователь получает список вещей, для которых он является владельцем (id передаётся в заголовке). Отображаются в том числе вещи в статусе "неактивен" (available = false).

- **Поиск вещей** 
<a name="search"></a>
```
GET /items/search?text={text}
```
В {text} передаётся поисковый запрос пользователя. Поиск производится и по названию, и по описанию вещей, притом только среди тех, у которых статус является активным (available = true).

Поиск вещей могут осуществлять все пользователи. 