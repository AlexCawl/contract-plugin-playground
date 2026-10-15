# Контракты V1 и V2

Материал для доклада по Contract Plugin Playground. Документы и архивы находятся в `main`;
эксперимент воспроизводится в ветках `codex/contract-1.0` и `codex/contract-1.1`.
Ссылки на реализации ведут на сохранённые снимки исходников этих материалов.

Здесь **V1 и V2 — два механизма
реализации контракта**, а **1.0 и 1.1 — версии публикации Component**. В Component 1.0
есть `cornerSize()` и `shape()`; в Component 1.1 добавлен `borderStroke()`.

Source compatibility означает возможность пересобрать прежний исходный код без правок.
Бинарная совместимость позволяет использовать прежний скомпилированный потребитель.
Сохранение поведения проверяется отдельно. Эти определения соответствуют
[документации Kotlin](https://kotlinlang.org/docs/api-guidelines-backward-compatibility.html#compatibility-types).

## 1. Контракт V1

### 1.1. Объявление и назначение

Контракт описывает оформление через функции. Обязательный `cornerSize()` задаётся callback;
`shape()` и `borderStroke()` имеют реализации по умолчанию.

```kotlin
@Stable
interface Contract1 {
    // 1.0
    @Composable
    fun cornerSize(): Dp

    // 1.0
    @Composable
    fun shape(): Shape = RoundedCornerShape(cornerSize())

    // 1.1
    @Composable
    fun borderStroke(): BorderStroke? = null
}
```

Методы и callbacks — `@Composable`: вычисление может читать текущую Compose-тему.
Callback имеет receiver `Contract1` и может обращаться к другим методам контракта.
В интерфейсе хранятся правила вычисления, а не готовые значения оформления.

Исходник: [Contract1.kt](../artifacts/presentation-sources/component/src/main/kotlin/com/alexcawl/contract/playground/component/v1/Contract1.kt).

### 1.2. Какие сущности нужны

В Playground поддерживающий код V1 написан вручную, по образцу Benchmark. Contract Plugin
его не генерирует. Kotlin и Compose compiler превращают этот код в JVM-классы и методы.

| Сущность | Назначение |
| --- | --- |
| `Contract1` | Публичный интерфейс и default-реализации методов |
| Inline-фабрика `Contract1(...)` | Создаёт анонимную реализацию, вызывающую callbacks |
| `ReflectiveEqualsHashCode` | Сравнивает поля runtime-класса через reflection |
| `Contract1Wrapper` | Добавляет ссылку `wrapped` на исходный контракт |
| Inline-расширение `Contract1.copy(...)` | Создаёт wrapper и делегирует отсутствующие overrides исходному объекту |
| `@Composable inline rememberCopy(...)` | Удерживает созданную копию через `remember` |

Фабрика для Component 1.1:

```kotlin
inline fun Contract1(
    crossinline cornerSize: @Composable Contract1.() -> Dp,
    noinline shape: (@Composable Contract1.() -> Shape)? = null,
    noinline borderStroke: (@Composable Contract1.() -> BorderStroke?)? = null,
): Contract1 = object : ReflectiveEqualsHashCode(), Contract1 {
    @Composable
    override fun cornerSize(): Dp = cornerSize.invoke(this)

    @Composable
    override fun shape(): Shape = if (shape != null) shape.invoke(this) else super.shape()

    @Composable
    override fun borderStroke(): BorderStroke? =
        if (borderStroke != null) borderStroke.invoke(this) else super.borderStroke()
}
```

Отсутствующий callback фабрики означает вызов default-тела интерфейса. Callback
`borderStroke = { null }` при этом является явным override, возвращающим `null`.

Копирование использует другой receiver — wrapper:

```kotlin
interface Contract1Wrapper : Contract1 {
    val wrapped: Contract1
}

@RememberInComposition
inline fun Contract1.copy(
    crossinline cornerSize: @Composable Contract1Wrapper.() -> Dp = { wrapped.cornerSize() },
    crossinline shape: @Composable Contract1Wrapper.() -> Shape = { wrapped.shape() },
    crossinline borderStroke: @Composable Contract1Wrapper.() -> BorderStroke? = { wrapped.borderStroke() },
): Contract1 = object : ReflectiveEqualsHashCode(), Contract1Wrapper {
    override val wrapped: Contract1
        get() = this@copy

    @Composable
    override fun cornerSize(): Dp = cornerSize.invoke(this)

    @Composable
    override fun shape(): Shape = shape.invoke(this)

    @Composable
    override fun borderStroke(): BorderStroke? = borderStroke.invoke(this)
}
```

`rememberCopy` вызывает `copy` внутри `remember` без ключей. Поэтому смена исходного
контракта в той же позиции композиции сохраняет первую копию. Это намеренно оставленная
семантика Benchmark, отмеченная в коде комментарием `shortcut:`.

После компиляции появляются фасад `Contract1Kt`, анонимные классы реализации и wrappers,
классы callbacks и default-argument helpers. Compose добавляет `Composer` и маски:
callbacks этого примера имеют JVM-тип `Function3`. Полный список находится в
[сохранённом javap](../artifacts/1.1-inline/bytecode/component/signatures.txt).

### 1.3. Копирование делегирует исходному контракту

Пример внутри композиции:

```kotlin
val original = Contract1(cornerSize = { 10.dp })
val copy = original.copy(cornerSize = { 20.dp })
```

| Выражение | Результат | Причина |
| --- | --- | --- |
| `copy.cornerSize()` | `20.dp` | Явный override |
| `copy.shape()` | `RoundedCornerShape(10.dp)` | Default callback копии вызывает `wrapped.shape()` |

`copy.shape()` вызывает `wrapped.shape()`. Тело shape вызывает `cornerSize()` на этом же
исходном объекте и получает `10.dp`, хотя `copy.cornerSize()` уже возвращает `20.dp`.
Замена cornerSize в wrapper не меняет receiver исходного метода. Для другого результата нужно явно передать
`shape = { RoundedCornerShape(cornerSize()) }` в `copy`.

Это проверяют [Contract1Test](../artifacts/presentation-sources/component/src/test/kotlin/com/alexcawl/contract/playground/component/v1/Contract1Test.kt):
`isDelegated` проходит, `isRebinding` намеренно падает.

### 1.4. Проблемы ReflectiveEqualsHashCode

Helper сначала требует одинаковый `javaClass`, затем читает все `declaredFields`.
Массив полей кешируется; значения полей при каждом сравнении и хешировании читаются заново.

| Проблема | Что происходит |
| --- | --- |
| Reflection при `equals` и `hashCode` | Требуются поиск в кеше и reflective-доступ к полям. Кеш не устраняет сами чтения; измерений производительности здесь нет |
| Equality зависит от формы скомпилированного класса | Сравниваются захваты и служебные поля анонимного класса. Разные inline call sites могут создавать разные runtime-классы |
| Ошибка при двух `null` | Выражение `field.get(this)?.equals(field.get(other)) == true` возвращает `false`, если оба значения `null` |
| Несогласованность primitive floating-point equality и hashing | `+0.0f` и `-0.0f` сравниваются через primitive `==` как равные, но boxed `Float.hashCode()` различается; аналогичная проблема есть у `Double` |
| Неявный набор сравниваемых полей | `declaredFields` не фильтрует static/synthetic-поля и не включает поля superclass |

Ошибка `null` относится к сравнению **разных экземпляров**: сравнение объекта с самим собой
возвращает `true` раньше. Например, два объекта одного класса с единственным nullable-полем,
равным `null`, не считаются равными. Nullable callbacks фабрики попадают в ту же проблемную
ветку, если присутствуют в полях реализации.

Обе ошибки воспроизведены на JVM с неизменённым опубликованным helper. Для проверки использованы
два Java subclass: один с единственным полем `Object value = null`, другой с полем `float value`.
Полученный вывод:

```text
two null fields: equals=false
signed float zero: equals=true, hashCode=0 / -2147483648
```

Сохранены [исходник проверки](../artifacts/reflection-helper/ReflectiveCheck.java)
и [оригинальный вывод](../artifacts/reflection-helper/output.txt).

Второй случай нарушает требование одинакового hashCode у равных объектов. Это дефект общего
helper; он не означает, что каждый конкретный экземпляр Contract1 содержит primitive Float.

Reflection также не превращает callbacks в сравнение результатов: два callback, возвращающих
одинаковую форму, не обязаны считаться равными. Equality определяется захваченными полями
реализации. Аннотация `@Stable` сама по себе не исправляет эти свойства.

Исходник: [ReflectiveEqualsHashCode.kt](../artifacts/presentation-sources/component/src/main/kotlin/com/alexcawl/contract/playground/component/v1/ReflectiveEqualsHashCode.kt).

### 1.5. Совместимость V1 при переходе Component 1.0 к 1.1

**Source compatibility.** Новый метод имеет тело, а новый callback фабрики — default `null`.
Новый callback `copy` по умолчанию делегирует `wrapped.borderStroke()`.
Прежние именованные вызовы и позиционные вызовы внутри скобок сохраняются при пересборке.
Нельзя обещать сохранение арности callable references и привязки trailing lambda:
последний параметр фабрики теперь другой.

**Бинарные вызовы из Styling.** Фабрика, `copy` и `rememberCopy` — inline. Проверяемые
Kotlin-вызовы уже встроены в Styling 1.0 вместе с анонимными классами. Для них сохранение
старых перегрузок этих функций не требуется. В актуальной V1 таких перегрузок нет.
После их удаления все прежние проверки линковки контрактов продолжают проходить.

Старые реализации не знают `borderStroke()`, поэтому важен настоящий JVM default-метод
в новом интерфейсе. В обоих выпусках установлен `JvmDefaultMode.ENABLE`; старый класс
наследует `borderStroke() = null`. Режим также сохраняет `DefaultImpls` и compatibility
bridges, согласно [документации Kotlin](https://kotlinlang.org/docs/java-to-kotlin-interop.html#compatibility-modes-for-default-methods).

Это гарантия для проверяемых **встроенных Kotlin-вызовов**, а не сохранение всех методов
JVM-фасада. Старые двухаргументные `Contract1` и `Contract1$default` из `Contract1Kt` удалены.
Если отдельный JVM-потребитель вызывает их напрямую, такого пути данный эксперимент
не сохраняет. Само слово `inline` не отменяет существование JVM-методов.

Сохранённая линковка не обновляет встроенную реализацию `copy`.[^v1-inline-copy]

[^v1-inline-copy]: **Особенность под звёздочкой: старый inline-copy не переносит новый override.**

    App, собранный против Component 1.1, создаёт Contract1 с ненулевым `borderStroke`
    и передаёт его в `Contract1BinaryProbe.copyDefault` либо `copyFull` из старого Styling.
    Встроенный в Styling 1.0 wrapper умеет переопределять только cornerSize и shape.
    У него нет делегирования `wrapped.borderStroke()`; новый метод наследует default `null`.

    Поэтому `v1CopyDefault` и `v1CopyFull` получают `null` вместо ожидаемого
    `BorderStroke(10.dp, Color.Black)`. Оба падают с `AssertionError`, без
    `NoSuchMethodError` или `AbstractMethodError`. Это ограничение поведения уже встроенного
    кода при эволюции интерфейса, а не потеря точки входа вызова контракта.
    Пересборка Styling добавляет override нового метода; прежняя проверка с пересобранным
    Styling проходит.

    Оригиналы: [JUnit XML после удаления V1-перегрузок](../artifacts/1.1-inline/test-results/app/TEST-com.alexcawl.contract.playground.app.BinaryCompatibilityTest.xml)
    и [сравнение старого и пересобранного Styling](PROBE_RECOMPILATION.md).

## 2. Контракт V2

### 2.1. Объявление для Contract Plugin

Те же значения можно описать обычными методами, оставив генерацию поддерживающих сущностей
компиляторному плагину:

```kotlin
@GenerateContract
interface Contract2 {
    // 1.0: начальная версия контракта
    fun cornerSize(): Dp

    // 1.0
    fun shape(): Shape = RoundedCornerShape(cornerSize())

    // 1.1: первый этап расширения
    @IntroducedAtContract(1)
    fun borderStroke(): BorderStroke? = null
}
```

`@IntroducedAtContract(1)` — аннотация нашего плагина на **методе**. Число обозначает этап
расширения схемы, а не Maven-версию: здесь начальная группа `0` соответствует Component 1.0,
группа `1` — добавлению borderStroke в Component 1.1.

Это форма объявления **при подключённом Contract Plugin**. В Playground плагин не подключён:
[Contract2.kt](../artifacts/presentation-sources/component/src/main/kotlin/com/alexcawl/contract/playground/component/v2/Contract2.kt)
содержит ручную реализацию этого механизма, включая `contract()` и вложенный контейнер.
Результаты Android-эксперимента относятся к этой ручной реализации.

Методы и callbacks V2 обычные, без `@Composable`. Значения из темы нужно получать снаружи,
в composable getter presets, и передавать через callbacks; при изменении захваченных значений
контракт следует пересоздать. Плагин не добавляет `@Stable`: корректность этой аннотации
остаётся обязанностью автора интерфейса.

### 2.2. Какие сущности генерируются

| Сущность | Назначение |
| --- | --- |
| `Contract2.contract(): Contract2.Contract` | Возвращает новый контейнер исходных callbacks |
| `Contract2.Contract` | Публичный final-контейнер с immutable-свойствами callbacks |
| Приватный `Contract2Impl` | Хранит callbacks, реализует методы, `equals` и `hashCode` |
| Обычная фабрика `Contract2(...)` | Создаёт `Contract2Impl` |
| Обычное расширение `Contract2.copy(...)` | Объединяет callbacks исходного контракта с overrides |
| Скрытые старые конструкторы, фабрики и `copy` | Сохраняют опубликованные версии JVM-вызовов |

Плагин генерирует объявления внутри компилятора, без физических Kotlin-файлов.
Фабрика и реализация принадлежат фасаду `Contract2ConstructorKt`, copy — `Contract2CopyKt`.
В ручной модели Playground обе функции находятся в `Contract2Kt`.
Это разные JVM owners: переход с ручного кода на плагин нельзя объявлять бинарно совместимым
без отдельного сохранения прежних owners.

Контейнер из Playground:

```kotlin
class Contract(
    val cornerSize: Contract2.() -> Dp,
    val shape: (Contract2.() -> Shape)? = null,
    val borderStroke: (Contract2.() -> BorderStroke?)? = null,
) {
    @Deprecated("Binary compatibility with 1.0", level = DeprecationLevel.HIDDEN)
    constructor(
        cornerSize: Contract2.() -> Dp,
        shape: (Contract2.() -> Shape)? = null,
    ) : this(cornerSize, shape, null)
}
```

Это контейнер callbacks, не `data class`. Сам контейнер использует reference equality.
Метод `contract()` каждый раз создаёт новый контейнер. Value equality реализована
у `Contract2Impl`, а не у `Contract2.Contract`.

Состав сущностей и правила генерации подтверждены
[документацией GenerateContract в исходниках плагина](../artifacts/presentation-sources/contract-plugin/plugin-annotations/src/main/kotlin/com/alexcawl/contract/GenerateContract.kt).

### 2.3. Копирование сохраняет callbacks и меняет их receiver

Фабрика и copy из Playground:

```kotlin
fun Contract2(
    cornerSize: Contract2.() -> Dp,
    shape: (Contract2.() -> Shape)? = null,
    borderStroke: (Contract2.() -> BorderStroke?)? = null,
): Contract2 = Contract2Impl(cornerSize, shape, borderStroke)

fun Contract2.copy(
    cornerSize: (Contract2.() -> Dp)? = null,
    shape: (Contract2.() -> Shape)? = null,
    borderStroke: (Contract2.() -> BorderStroke?)? = null,
): Contract2 {
    val original = contract()
    return Contract2(
        cornerSize = cornerSize ?: original.cornerSize,
        shape = shape ?: original.shape,
        borderStroke = borderStroke ?: original.borderStroke,
    )
}
```

В реализации callback вызывается с `this` текущего `Contract2Impl`:
`cornerSize.invoke(this)`. Если callback shape отсутствует, реализация вызывает
`super<Contract2>.shape()`. Это default-тело выполняется с текущим экземпляром.

```kotlin
val original = Contract2(cornerSize = { 10.dp })
val copy = original.copy(cornerSize = { 20.dp })
```

| Выражение | Результат | Причина |
| --- | --- | --- |
| `copy.cornerSize()` | `20.dp` | Явный override |
| `copy.shape()` | `RoundedCornerShape(20.dp)` | Default-тело вызывает cornerSize новой копии |

В примере callback shape не задан. Копия сохраняет `shape = null` и выполняет default-тело
`shape()` с новой копией как receiver. Его вызов `cornerSize()` получает новый override
`20.dp`; делегирования `original.shape()` здесь нет.

Меняется receiver вызова, а не захваченные внешние ссылки. Callback вида
`{ original.cornerSize() }` продолжит обращаться к явно захваченному `original`.

У `copy` аргумент `null` означает «сохранить прежний callback». Вернуть nullable-значение
`null` можно через callback `{ null }`; сбросить override к default-телу интерфейса
через `copy` в текущем API нельзя.

Это проверяют [Contract2Test](../artifacts/presentation-sources/component/src/test/kotlin/com/alexcawl/contract/playground/component/v2/Contract2Test.kt):
`isRebinding` проходит, `isDelegated` намеренно падает.

### 2.4. Equality и hashCode без reflection

Реализация явно сравнивает три callbacks:

```kotlin
override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Contract2Impl) return false
    return cornerSize == other.cornerSize && shape == other.shape && borderStroke == other.borderStroke
}

override fun hashCode(): Int {
    var result = cornerSize.hashCode()
    result = 31 * result + (shape?.hashCode() ?: 0)
    result = 31 * result + (borderStroke?.hashCode() ?: 0)
    return result
}
```

Набор сравниваемых полей известен при компиляции; reflective-доступ не требуется.
Два отсутствующих callback сравниваются корректно. Код не смешивает primitive Float equality
с boxed Float hashing: поля содержат callbacks.

Callback сравнивается через собственные `equals` и `hashCode`; методы контракта для сравнения
не вычисляются. Разные lambda-объекты с одинаковым результатом не обязаны быть равны.
Неизменённая `copy()` создаёт другой экземпляр с теми же callbacks, равный исходному
и имеющий тот же hashCode.

Это также означает, что изменяемый захват callback не становится immutable автоматически.
Гарантии equality и Compose stability зависят от соблюдения соответствующих требований
самими callbacks и реализациями интерфейса.

### 2.5. Source и бинарная совместимость V2

**Source compatibility.** В фабрике и контейнере новый callback optional и равен `null`.
В copy `null` сохраняет callback источника. Прежние именованные и позиционные вызовы внутри
скобок продолжают компилироваться. Новый метод имеет default-тело.
Параметры плагин упорядочивает по этапу появления, затем по порядку методов внутри этапа:
новые callbacks добавляются после старых. Callable references и trailing lambda требуют
отдельной проверки; новые Java-вызовы могут потребовать явных `null`.

**Бинарная совместимость.** Фабрика и copy V2 не inline. Старый Styling содержит обращения
к их JVM-точкам входа. Поэтому старые сигнатуры нужно сохранить.

При добавлении borderStroke:

- У `Contract2.Contract` появляется новый primary constructor с тремя callbacks;
  старый двухаргументный остаётся скрытым secondary constructor.
- У фабрики и copy добавляются новые функции, а старые остаются скрытыми перегрузками.
- У старых объявлений сохраняются прежние default-аргументы. Это сохраняет также старые
  `$default` и synthetic constructor с маской и `DefaultConstructorMarker`.
- `Contract2Impl` получает третье поле и override borderStroke. Старый конструктор
  приватной реализации сохранять не требуется: публичная фабрика не встраивается в клиента.

Скрытые перегрузки фабрики и copy из Playground:

```kotlin
@Deprecated("Binary compatibility with 1.0", level = DeprecationLevel.HIDDEN)
fun Contract2(
    cornerSize: Contract2.() -> Dp,
    shape: (Contract2.() -> Shape)? = null,
): Contract2 = Contract2(cornerSize = cornerSize, shape = shape, borderStroke = null)

@Deprecated("Binary compatibility with 1.0", level = DeprecationLevel.HIDDEN)
fun Contract2.copy(
    cornerSize: (Contract2.() -> Dp)? = null,
    shape: (Contract2.() -> Shape)? = null,
): Contract2 = copy(cornerSize = cornerSize, shape = shape, borderStroke = null)
```

`DeprecationLevel.HIDDEN` скрывает прежние объявления от нового Kotlin-исходника,
оставляя точки входа для уже скомпилированного кода.

Старый copy передаёт новому `borderStroke = null`. Новый copy читает `contract()`
исходного объекта и сохраняет его callback borderStroke. Поэтому старый Styling
копирует даже override, появившийся только в Component 1.1. Сохранённый callback
выполняется с новой копией: тесты получают border шириной `20.dp`.
Эта логика находится в новом Component, поэтому пересборка Styling для неё не нужна.

У настоящего плагина старые перегрузки генерируются по `@IntroducedAtContract`.
Без отметки нового этапа появится только полная актуальная сигнатура.
Нужно сохранять опубликованные наборы методов, их типы, имена параметров, порядок внутри
этапа, defaults и JVM owners. Режим JVM default тоже должен оставаться совместимым:
плагин его не настраивает.

Подробный пример вызовов `@JvmOverloads` и `@IntroducedAt` вынесен в
[JVM_DEFAULT_ARGUMENTS.md](JVM_DEFAULT_ARGUMENTS.md).

### 2.6. Подтверждение и границы эксперимента

Styling 1.0 собран против Component 1.0 и опубликован в Maven Local. App использует
его прежний AAR вместе с Component 1.1. Зависимостей `project(...)` между этими модулями нет.
В новой записи V1 уже не содержит скрытых inline-перегрузок; V2 их сохраняет.
Styling во время записи не пересобирался, его SHA-256 проверен до и после запуска.

Все четыре текущих V2 probe-теста проходят: обе фабрики и оба copy. У V1 обе фабрики
работают, оба copy вызываются, но проверки нового borderStroke падают по причине,
отмеченной под звёздочкой. Единственная ошибка линковки — отдельный `@JvmOverloads`-пример.
Итого App: **9 passed, 3 failed**. Четыре component-теста отдельно показывают
delegation/rebinding: два проходят и два намеренно падают.

Оригиналы актуальной проверки: [manifest](../artifacts/1.1-inline/manifest.json),
[JUnit XML](../artifacts/1.1-inline/test-results/app/TEST-com.alexcawl.contract.playground.app.BinaryCompatibilityTest.xml),
[полные JVM-сигнатуры](../artifacts/1.1-inline/bytecode/component/signatures.txt),
[полный bytecode](../artifacts/1.1-inline/bytecode/component/instructions.txt)
и [публикация Component 1.1](../artifacts/1.1-inline/maven/component/1.1).
Архив содержит AAR, sources.jar, POM, module metadata, APK, отчёты, логи и SHA256SUMS.

Предыдущая матрица: Component 1.0 / Styling 1.0 — 12 passed; Component 1.1 /
пересобранный Styling 1.1 — 12 passed. Её оригиналы сохранены в
[probe-recompilation](../artifacts/probe-recompilation/manifest.json).
Пересобранный Styling после удаления V1-перегрузок повторно не проверялся.
Исторический [API_SIGNATURES.md](API_SIGNATURES.md) содержит полные Kotlin/JVM-сигнатуры
первоначальных публикаций, когда V1-перегрузки ещё присутствовали.

Проверено на Kotlin 2.4.20, JVM target 17, `JvmDefaultMode.ENABLE`; Gradle daemon использует
JDK 25 по сохранённой конфигурации проекта. Сборка APK и lint прошли.
Compose-вызовы проверялись на JVM в headless composition. На устройстве проверки
не выполнялись; Pixel не использовался.
