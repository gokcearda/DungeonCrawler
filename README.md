# DungeonCrawler - Basit Java GUI Oyunu
Bu proje, basit bir Java GUI ile yazılmış, grafik ekranda açılabilen bir Dungeon oyunudur.

## Nasıl Çalıştırılır?
Bu projeyi çalıştırmak için aşağıdaki adımları izleyin:

## 1. Gereksinimler
- **Java Development Kit (JDK)** kurulu olmalı. Eger kurulu değilse, [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) veya [OpenJDK](https://openjdk.org/) indirip kurun.
- **Resim Dosyaları**: Proje klasöründe `images` klasörü bulunmalı ve aşağıdaki resimleri içermeli:

```
images/
├── background.png
├── enemy_1.png
├── enemy_2.png
├── enemy_3.png
├── enemy_4.png
├── exit.png
├── gold.png
├── player.png
├── super_gold.png
└── wall.png
```

## 2. Projeyi İndir
Bu depoyu bilgisayarınıza klonlayın:

```bash
git clone https://github.com/gokcearda/DungeonCrawler.git
```

Proje klasörüne gidin:

```bash
cd DungeonCrawler
```

## 3. Oyunu Derle
Terminalde aşağıdaki komutları çalıştırarak oyunu derleyin:

```bash
javac src/DungeonCrawler.java
```

## 4. Oyunu Çalıştır
Derlenmiş dosyayı aşağıdaki komutla çalıştırın:

```bash
java src/DungeonCrawler
```

## 5. JAR Dosyası ile Çalıştırma (Opsiyonel)
Eğer JAR dosyası oluşturmak isterseniz:

JAR dosyasını oluşturun:

```bash
jar cfm DungeonCrawler.jar manifest.txt -C src . -C images .
```

JAR dosyasını çalıştırın:

```bash
java -jar DungeonCrawler.jar
```

## Oynanış
### Kontroller:
- **WASD veya Yön Tuşları**: Hareket etmek için.
- **P**: Oyunu duraklatmak/devam etmek için.
- **R**: Oyunu yeniden başlatmak için.
- **K**: Süper güç almak için (10. seviyede ve 10+ altına sahipseniz).

### Amaç:
- Altın toplayın (`$` veya `S`) ve çıkış kapısına (`>`) ulaşın.
- Her seviyede belirli miktarda altın toplamanız gerekir.
- Düşmanlardan (`E`) kaçının veya süper güç aktifken onları yok edin.

