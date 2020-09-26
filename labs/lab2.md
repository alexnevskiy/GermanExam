# Цели работы

- Ознакомиться с жизненным циклом Activity
- Изучить основные возможности и свойства alternative resources

# 1. Activity

Продемонстрируйте жизненный цикл Activity на любом нетривиальном примере.

## Пример 1: Split Screen

![](https://raw.githubusercontent.com/alexnevskiy/GermanExam/master/labs/images/Split%20Screen.png)

*Рис. 1. Методы жизненного цикла приложения в режиме Split Screen*

При открытии окна вызываются методы в следующем порядке: `setContentView(), onCreate(), onStart()` и `onResume()`.  Данные 4 метода всегда вызываются при первом открытии окна, поэтому в следующих примерах они будут пропущены. Если перевести приложение в режим Split Screen, то в логах можно наблюдать следующие вызовы методов: `onPause(), onStop(), onSaveInstanceState(), onDestroy(), onCreate(), setContentView(), onStart(), onRestoreInstanceState(), onResume()` и `onPause()`. Если же окно растянуть снова на весь экран, то произойдёт вся таже процедура из этих же методов. Если же открыть второе окно, то появится вызов метода `onResume()`. При нажатии на экран в открытом втором приложении, никаких вызовов методов не происходит. При переведении моего приложения в режим Split Screen таймер, который ведёт обратный отсчёт времени для подготовки человека к ответу на задание, сбрасывается до начального времени, поэтому принято решение отключить возможность перевода приложения в режим разделённого экрана. Его можно отключить путём написания атрибута `android:resizeableActivity="false"` в файле манифеста.

## Пример 2: Звонок на устройство

![](https://raw.githubusercontent.com/alexnevskiy/GermanExam/master/labs/images/Call%20device.png)

*Рис. 2. Методы жизненного цикла приложения во время звонка*

При звонке на устройство появляется всплывающие уведомление, при этом никакие методы жизненного цикла Activity не вызывались. Если же пользователь решит ответить на звонок, то вызовутся методы `onPause(), onStop()` и `onSaveInstanceState()`. После завершения звонка при возвращении в окно приложения вызываются следующие методы: `onRestart(), onStart()` и `onResume()`.

## Пример 3: Вызов Google Assistant

![](https://raw.githubusercontent.com/alexnevskiy/GermanExam/master/labs/images/Google%20Assistant.png)

*Рис. 3. Методы жизненного цикла приложения при вызове Google Assistant*

При кратковременном вызове Google Assistant вызывается метод жизненного цикла `onPause()` и, соответственно, `onResume()` при возвращении в приложение. Если же вызвать ассистента и ждать, то через некоторое время откроется окно самого ассистента и будут вызваны следующие методы нашего приложения: `onStop(), onSaveInstanceState()`. При закрытии Google Assistant вызывается знакомый нам набор методов: `onRestart(), onStart()` и `onResume()`.

# 2. Alternative Resources

Продемонстрируйте работу альтернативного ресурса (тип ресурса согласно 4 варианту) на каком-либо примере.

![]()

*Рис. 4. Экран запущенного приложения с разрешением меньше 480dp*

![]()

*Рис. 5. Экран запущенного приложения с разрешением больше 480dp и конфигурацией Available height*

![]()

*Рис. 6. Экран запущенного приложения с разрешением больше 480dp и без конфигурации Available height*

Согласно 4 варианту нужно осуществить поддержку конфигурации Available height (Доступная высота). Для этого создана отдельная директория под названием layout-h480dp, в которой хранится task1.xml файл, переделанный специально для доступной высоты экрана устройства. При запуске приложения выбирается тот файл, который соответствует конфигурации устройства. Полезный эффект данного альтернативного ресурса заключается в том, что мы можем свободно переделывать вёрстку для разных доступных высот экрана. На примере моего приложения можно видеть, что если бы данное окно было запущено со стандартным task1.xml файлом, то интерфейс был бы слишком маленький, например, для современных планшетов с большой диагональю и разрешением, пользователю пришлось бы всматриваться в текст по центру экрана, как показано на рис. 6.
