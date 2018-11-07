# SpialeLayout
自动竖向滚动的控件，可自定义Item样式
# 注意事项
* 由于OnItemClickListener点击事件是通过在item添加点击事件实现的，
所以如果在Adapter getView根节点添加点击事件会导致和OnItemClickListener冲突
* 现阶段Adapter只支持Item存在一种样式 所以getItemViewType方法无效
* OnItemClickListener第三个参数itemData 是从adapter.getItem()中获取的
# 效果图
![](https://github.com/ooftf/SpialeLayout/raw/master/ImageRepository/SpialeLayout.gif)
# Gradle配置
```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
dependencies {
    compile 'com.github.ooftf:SpialeLayout:1.1.0'
}
```
# 使用方式
```xml
 <com.ooftf.spiale.SpialeLayout
        app:scrollMillis="1000"
        app:showMillis="3000"
        android:layout_marginTop="36dp"
        android:id="@+id/spialeLayout"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:background="#FFFFFF">
    </com.ooftf.spiale.SpialeLayout>
```
```kotlin
    BaseAdapter adapter = SpialeAdapter(this)
    spialeLayout.adapter  = adapter
    spialeLayout.setOnItemClickListener{ position, _, itemData ->
                itemData as Bean
                Toast.makeText(this,"$position :: ${itemData.text}",Toast.LENGTH_SHORT).show()
    }
```
# XML属性
|属性名|描述|默认值|
|---|---|---|
|scrollMillis|滚动动画时间（毫秒）|1000|
|showMillis|停止展示时间（毫秒）|2000|
# SpialeLayout方法
|方法名|描述|
|---|---|
|setAdapter|设置适配器|
|setOnItemClickListener|设置Item点击时间|
