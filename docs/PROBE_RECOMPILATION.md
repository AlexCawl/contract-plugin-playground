# Те же probes: старый и пересобранный Styling

`BinaryProbes.kt` одинаков в ветках `codex/contract-1.0` и `codex/contract-1.1`.
В нём по четыре метода у Contract1BinaryProbe, Contract2BinaryProbe и FunctionBinaryProbe.
В App ровно 12 тестов: по одному на каждый probe. Удалены тесты методов, которых больше нет
в probes. Сами probes сохранены в том виде, который задал пользователь.

## Результат

| Component | Styling | Против какого Component собран Styling | Тесты App |
| --- | --- | --- | --- |
| 1.0 | 1.0 | 1.0 | 12 passed |
| 1.1 | 1.0 | 1.0 | 9 passed, 3 failed |
| 1.1 | 1.1 | 1.1 | 12 passed |

На ветке 1.1 использовались **одни и те же тесты и assertions**. Менялась только Maven-зависимость
Styling через `-PstylingVersion=1.1`. Результат не подменён проверкой ожидаемого исключения:
со старым Styling Gradle действительно завершается с ошибкой, а HTML-отчёт показывает падения.

## Что именно падает без пересборки

- `v1CopyDefault`: новый исходный Contract1 содержит borderStroke. Старый inline-copy из
  Styling 1.0 не содержит override нового метода и возвращает default null. Тест ожидает
  делегированный исходный BorderStroke с шириной 10 dp и получает null.
- `v1CopyFull`: даже при явных cornerSize/shape старый inline-copy не переносит borderStroke.
  Тест также получает null вместо исходного BorderStroke с шириной 10 dp.
- `jvmOverloadsDefault`: старый Kotlin-вызов с пропущенным optional ссылается на отсутствующий
  старый withJvmOverloads$default и получает NoSuchMethodError.

Первые два падения — **изменение поведения встроенного inline-кода**, а не ошибка линковки
контракта. Скрытые overloads Component 1.1 сохраняют ABI, но не заменяют тело, уже встроенное
в Styling 1.0. Пересборка Styling добавляет делегирование `wrapped.borderStroke()`.

V2 сохраняет новый callback даже со старым Styling: выполнение происходит через сохранённые
обычные функции Component 1.1. Его copy перебинживает callback на новый экземпляр, поэтому
проверяемый BorderStroke имеет ширину 20 dp. Оба V2 copy-теста проходят в обоих режимах.

`withIntroducedAt` сохраняет старые full/default entry points и также проходит в обоих режимах.

## Запуск

На ветке 1.0 probes компилируются против Component 1.0 и публикуются как Styling 1.0:

```sh
git switch codex/contract-1.0
./gradlew :styling:publishReleasePublicationToMavenLocal
./gradlew :app:testDebugUnitTest --tests '*BinaryCompatibilityTest'
```

На ветке 1.1 App по умолчанию получает этот старый Styling:

```sh
git switch codex/contract-1.1
./gradlew :app:testDebugUnitTest --tests '*BinaryCompatibilityTest'
# 9 passed, 3 failed
```

Отдельная публикация Styling 1.1 компилирует те же probes против Component 1.1:

```sh
./gradlew :styling:publishReleasePublicationToMavenLocal -PstylingVersion=1.1
./gradlew :app:testDebugUnitTest --tests '*BinaryCompatibilityTest' -PstylingVersion=1.1
# 12 passed
```

В этом sandbox свойство `stylingVersion` выбирает версию публикации Styling и версию Component,
против которой она компилируется. В App оно выбирает соответствующую Maven-зависимость.
Без свойства остаётся 1.0. Пересборка 1.1 не перезаписывает публикацию 1.0.
Component 1.0 и 1.1 уже опубликованы в Maven Local предыдущим этапом; на новой машине сначала
опубликуйте соответствующий `:component:publishReleasePublicationToMavenLocal` в каждой ветке.

## Оригинальные результаты

[Артефакты этого запуска](../artifacts/probe-recompilation/manifest.json) содержат:

- baseline/: JUnit XML и HTML для Component 1.0 / Styling 1.0;
- frozen/: JUnit XML и HTML с тремя реальными падениями, Styling 1.0 AAR/sources/POM/module и bytecode;
- rebuilt/: JUnit XML и HTML с 12 успешными тестами, Styling 1.1 AAR/sources/POM/module и bytecode;
- полные логи публикаций/тестов, точный BinaryProbes.kt, diff байткода и SHA256SUMS.

В обоих опубликованных sources.jar BinaryProbes.kt совпадает побайтово. Новая публикация 1.0
учитывает удалённые пользователем probes; оригинальные исторические записи artifacts/1.0
и artifacts/1.1 сохранены без изменений. Старый отчёт на 18 тестов описывает исходный эксперимент,
а эта запись — текущие 12 probes и проверку необходимости пересборки.

Последний запуск App выполнен с настройками по умолчанию и оставлен красным: три указанных
падения видны в app/build/reports/tests/testDebugUnitTest/index.html. Pixel не использовался.
