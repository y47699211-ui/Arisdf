# Arisdf — ArisDonate + RegionBlocks

Два связанных Paper-плагина для Minecraft 1.21.x:

- **ArisDonate** — донат-система, GUI, /home, /warp, /tpa, /kit, /msg, /sphere
  и ~150 команд.
- **RegionBlocks** — система регионов через специальные блоки + общий **/shop**
  (приваты, ТНТ, вагонетки, **сферы/шары**, **киты**).

## /shop

Команда `/shop` принадлежит **RegionBlocks**. Вкладки:

| Вкладка        | Источник         | Валюта |
|----------------|------------------|--------|
| Приваты        | RegionBlocks     | Арисы  |
| ТНТ            | RegionBlocks     | Арисы  |
| Вагонетки      | RegionBlocks     | Арисы  |
| Сферы / Шары   | ArisDonate       | Арисы  |
| Киты           | ArisDonate       | Арисы  |

Сферы и киты появляются, только если на сервере **загружен ArisDonate**
(`softdepend: [ArisDonate]`). Цены сфер берутся из `ArisDonate/config.yml`
(секция `spheres:`). Цены китов жёстко заданы в коде (см.
`ShopListener.KIT_PRICES`) и сделаны намеренно высокими.

Можно сразу открыть конкретную вкладку:

```
/shop privates
/shop tnt
/shop minecart
/shop spheres
/shop kits
```

`/donate` и `/sphere` в ArisDonate сохранены и продолжают работать как
независимые входы.

## Сборка

Требуется **JDK 21+** и **Maven 3.6+**.

```sh
./build.sh
```

Артефакты:
- `ArisDonate/target/ArisDonate-1.0.0.jar`
- `RegionBlocks/target/RegionBlocks-1.0.0.jar`

Оба .jar нужно положить на сервер в `plugins/`.
