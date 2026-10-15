# @JvmOverloads и @IntroducedAt: бинарная совместимость

[Доклад о контрактах V1 и V2](CONTRACTS_REPORT.md).

Styling собран против Component 1.0. App использует тот же Styling с Component 1.1.

## 1. Изменение API

Component 1.0 — обязательный required и необязательный optional:

```kotlin
@JvmOverloads
fun withJvmOverloads(required: String, optional: String = ":default"): String =
    required + optional

fun withIntroducedAt(required: String, optional: String = ":default"): String =
    required + optional
```

Component 1.1 добавляет необязательный added:

```kotlin
@JvmOverloads
fun withJvmOverloads(
    required: String,
    optional: String = ":default",
    added: String = "",
): String = required + optional + added

@OptIn(ExperimentalVersionOverloading::class)
fun withIntroducedAt(
    required: String,
    optional: String = ":default",
    @IntroducedAt("1.1") added: String = "",
): String = required + optional + added
```

## 2. Вызовы из старого Styling

App получает `FunctionBinaryProbe.INSTANCE` через `getstatic`, вызывает probe через
`invokevirtual`. Probe вызывает функцию Component через `invokestatic`:

```kotlin
object FunctionBinaryProbe {
    fun jvmOverloadsDefault(): String = withJvmOverloads("value")

    fun jvmOverloadsFull(): String = withJvmOverloads("value", ":explicit")

    fun introducedAtDefault(): String = withIntroducedAt("value")

    fun introducedAtFull(): String = withIntroducedAt("value", ":explicit")
}
```

`Full` передаёт все аргументы **1.0**, без added. JVM owner функций из `VersionedFunctions.kt`:

```text
com.alexcawl.contract.playground.component.VersionedFunctionsKt
```

Старый Styling хранит следующие вызовы. Аргументы показаны как содержимое JVM-стека;
synthetic helpers недоступны для прямого вызова из обычного Kotlin-кода.

| Probe | Метод Component и переданные аргументы | Descriptor |
| --- | --- | --- |
| `jvmOverloadsDefault()` | `withJvmOverloads$default("value", null, 2, null)` | `(Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;` |
| `jvmOverloadsFull()` | `withJvmOverloads("value", ":explicit")` | `(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;` |
| `introducedAtDefault()` | `withIntroducedAt$default("value", null, 2, null)` | `(Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;` |
| `introducedAtFull()` | `withIntroducedAt("value", ":explicit")` | `(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;` |

Оригинальный bytecode `jvmOverloadsDefault`:

```text
0: ldc           #15                 // String value
2: aconst_null
3: iconst_2
4: aconst_null
5: invokestatic  #21                 // Method com/alexcawl/contract/playground/component/VersionedFunctionsKt.withJvmOverloads$default:(Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;
8: areturn
```

Owner, имя и descriptor фиксируются при компиляции Styling. Обновление Maven-зависимости
меняет загруженный класс Component, сохраняя старую ссылку потребителя.

## 3. Как работает $default

`$default` принимает исходные аргументы, `int mask` и служебный `Object marker`.
Marker здесь равен `null` и не используется. Маска выбирает defaults:

| Параметр | Индекс | Бит маски | Значение по умолчанию |
| --- | --- | --- | --- |
| `required` | 0 | `1` / `0b001` | Нет; соответствующий default не обрабатывается |
| `optional` | 1 | `2` / `0b010` | `":default"` |
| `added` — только в 1.1 | 2 | `4` / `0b100` | `""` |

Java-эквивалент helper из Component 1.0:

```java
public static String withJvmOverloads$default(
        String required, String optional, int mask, Object marker) {
    if ((mask & 2) != 0) {
        optional = ":default";
    }
    return withJvmOverloads(required, optional);
}
```

`withJvmOverloads("value")` передаёт заглушку `optional = null` и маску `2`.
Helper заменяет null на `":default"` перед вызовом функции. Если оба аргумента 1.0
переданы явно, Styling вызывает функцию напрямую.

## 4. Почему @JvmOverloads ломается

Ссылка в старом Styling:

```text
withJvmOverloads$default(java.lang.String, java.lang.String, int, java.lang.Object)
(Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;
```

Helper в Component 1.1 получает третий String и меняет descriptor:

```text
withJvmOverloads$default(java.lang.String, java.lang.String, java.lang.String, int, java.lang.Object)
(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;
```

`@JvmOverloads` сохраняет обычные перегрузки:

```java
public static final String withJvmOverloads(String required, String optional, String added);
public static final String withJvmOverloads(String required, String optional);
public static final String withJvmOverloads(String required);
```

Старого `$default` с двумя String нет. JVM ищет точные owner, имя и descriptor
с параметрами и возвращаемым типом. Обычная перегрузка имеет другое имя, новый helper —
другой descriptor. JVM не выбирает перегрузку заново и не подставляет Kotlin defaults.
Вызов завершается **до выполнения тела функции**:

```text
java.lang.NoSuchMethodError: 'java.lang.String com.alexcawl.contract.playground.component.VersionedFunctionsKt.withJvmOverloads$default(java.lang.String, java.lang.String, int, java.lang.Object)'
```

`withJvmOverloads(String)` не помогает: в старом вызове записано имя `$default`.
Маска `2` корректна; отсутствует сам метод.

`jvmOverloadsFull` проходит: сохранённый `withJvmOverloads(String, String)` вызывает
новый helper с `("value", ":explicit", null, 4, null)`. Маска `4` подставляет added = `""`;
полная функция возвращает `"value:explicit"`.

## 5. Почему @IntroducedAt работает

`@IntroducedAt("1.1")` сохраняет старую функцию **вместе со старым helper**.
В Component 1.1 доступны четыре static-метода:

```text
withIntroducedAt(java.lang.String, java.lang.String, java.lang.String)
(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

withIntroducedAt$default(java.lang.String, java.lang.String, java.lang.String, int, java.lang.Object)
(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;

withIntroducedAt(java.lang.String, java.lang.String)
(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

withIntroducedAt$default(java.lang.String, java.lang.String, int, java.lang.Object)
(Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;
```

Первые два — API 1.1, последние два — скрытый API 1.0 с прежними defaults.
Цепочка `introducedAtDefault()` из старого Styling:

1. `withIntroducedAt$default("value", null, 2, null)` подставляет optional = `":default"`.
2. Он вызывает старую перегрузку `withIntroducedAt("value", ":default")`.
3. Она вызывает новый `withIntroducedAt$default("value", ":default", null, 4, null)`.
   Маска `4` подставляет added = `""`.
4. Новый helper вызывает `withIntroducedAt("value", ":default", "")`; результат — `"value:default"`.

`introducedAtFull()` начинает со второго шага с optional = `":explicit"`
и возвращает `"value:explicit"`. Пересборка Styling не нужна.
Сохранение старых helpers соответствует
[рекомендациям Kotlin](https://kotlinlang.org/docs/api-guidelines-backward-compatibility.html#use-overloads-to-preserve-binary-compatibility).

[`kotlin.IntroducedAt`](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-introduced-at/) —
experimental-аннотация параметра с opt-in `ExperimentalVersionOverloading`.
`com.alexcawl.contract.IntroducedAtContract` отмечает метод интерфейса нашего плагина.
Механизм сохранения старых точек входа аналогичен; контракт V2 не использует stdlib-аннотацию.

## 6. После пересборки Styling

После пересборки против Component 1.1 вызов `withJvmOverloads("value")` использует новый helper:

```text
VersionedFunctionsKt.withJvmOverloads$default("value", null, null, 6, null)
(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;
```

`6 = 2 | 4` подставляет optional и added. Вызов с явным optional использует
новый helper с `("value", ":explicit", null, 4, null)`. Пересобранные вызовы
`withIntroducedAt` используют те же аргументы и маски `6`/`4` для своего нового helper.

| Probe | Старый Styling 1.0 + Component 1.1 | Пересобранный Styling 1.1 + Component 1.1 |
| --- | --- | --- |
| `jvmOverloadsDefault` | `NoSuchMethodError`: старого helper нет | `"value:default"` |
| `jvmOverloadsFull` | `"value:explicit"` | `"value:explicit"` |
| `introducedAtDefault` | `"value:default"` | `"value:default"` |
| `introducedAtFull` | `"value:explicit"` | `"value:explicit"` |

Оригинальный disassembly:
[старый Styling](../artifacts/1.1-inline/bytecode/styling/instructions.txt),
[Component 1.1](../artifacts/1.1-inline/bytecode/component/instructions.txt),
[Component 1.0](../artifacts/1.0/bytecode/component/instructions.txt)
и [пересобранный Styling](../artifacts/probe-recompilation/rebuilt/bytecode.txt).
Тесты App: [BinaryCompatibilityTest.kt](../artifacts/presentation-sources/app/src/test/kotlin/com/alexcawl/contract/playground/app/BinaryCompatibilityTest.kt).
