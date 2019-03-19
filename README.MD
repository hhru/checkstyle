# Checkstyle
> Официально checkstyle не умеет наследовать конфиги: [issue](https://github.com/checkstyle/checkstyle/issues/2873)  
> Но мы запилили свой тулсет, который позволяет наследовать конфиги один от другого  
> [Как работает merge](merge-description.md)
Пример стандартного использования: 
![example](./example)
## Как собрать
1. `merge --no-ff`
2. `mvn release:prepare mvn release:perform` 
> Сервис использует практику dogfood, поэтому после релиза полезно поднять версию плагина в помнике
## Тулы для работы с checkstyle
- ![hh-maven-checkstyle-plugin](./hh-maven-checkstyle-plugin) - maven-plugin, обертка над [maven-checkstyle-plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/), который включает в себя наши кастомные чеки, конфиги и механизм наследования.  
**При сборке своих конфигов можно использовать `<property name="hh-parent-config" value="..."/>`**,  
(см. ![hhru-checkstyle.xml](./checkstyle-config-plugin/src/main/resources/hhru/hhru-checkstyle.xml))
Если нет никаких причин, то лучше просто использовать этот плагин
### Еще
- ![checks](./checks) - наши кастомные checkstyle проверки.  
Можно использовать отдельно (см. ![shared-checkstyle.xml](./checkstyle-config-plugin/src/main/resources/shared-checkstyle.xml))
- ![checkstyle-config-plugin](./checkstyle-config-plugin) - артефакт, который поставляет конфиги  
его можно использовать с оригинальным checkstyle, но потребуются зависимости на `com.puppycrawl.tools`, наши чеки и не будет работать наследование конфигов:
```xml
<build>
    <plugins>
    ...
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>3.0.0</version>
            <dependencies>
                <dependency>
                    <groupId>ru.hh.checkstyle</groupId>
                    <artifactId>checkstyle-config-plugin</artifactId>
                    <version>${project.version}</version>
                </dependency>
            </dependencies>
            <executions>
                <execution>
                    <id>validate</id>
                    <phase>validate</phase>
                    <configuration>
                        <configLocation>shared-checkstyle.xml</configLocation>
                    </configuration>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ...
    </plugins>
</build>
```
- ![checkstyle-config-compilation-maven-plugin](./checkstyle-config-compilation-maven-plugin) - maven-plugin, который разрешает использование наследования при сборке ![checkstyle-config-plugin](./checkstyle-config-plugin)
