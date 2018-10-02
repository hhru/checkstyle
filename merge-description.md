## Как работает merge
Тул мержа рекурсивно объединяет идентичные элементы, обновляя(заменяя) значения атрибутов и обновляя и дополняя дочерние элементы
Идентичными считаются элементы, у которых совпадают: tagName, значение аттрибута 'name' и порядковый номер в родителе
### Пример
parent.xml:
```xml
<module name="RootModule">
    <module name="OuterModule">
        <module name="Check">
            <property name="property-name" value="value-unchanged"/>
        </module>
        <module name="Check">
            <property name="property-name" value="value-parent"/>
        </module>
    </module>
</module>
```
child.xml:
```xml
<module name="RootModule">
    <property name="hh-parent-config" value="../shared-checkstyle.xml"/>
    <module name="OuterModule">
        <module name="Check"/>
        <module name="Check">
            <property name="property-name" value="value-child"/>
        </module>
    </module>
</module>
```
result.xml:
```xml
<module name="RootModule">
    <module name="OuterModule">
        <module name="Check">
            <property name="property-name" value="value-unchanged"/>
        </module>
        <module name="Check">
            <property name="property-name" value="value-child"/>
        </module>
    </module>
</module>
```
з.ы. Тул мержа убирает все пустые текстовые ноды(отступы)
з.з.ы. Тул мержа можно написать и получше, но мне было некогда, так что ограничился mvp
