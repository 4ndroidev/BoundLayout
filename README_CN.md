# BoundLayout

## 介绍 [English](https://github.com/4ndroidev/BoundLayout/blob/master/README.md)

理论上，支持所有View的回弹效果，无论View是否能滚动

## 截图

![boundlayout.gif](https://github.com/4ndroidev/BoundLayout/blob/master/screenshot/boundlayout.gif)

## 使用

### 第一步

添加依赖 :

	compile 'com.androidev:boundlayout:1.0.0'

### 第二步

修改布局文件，如下 :

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.androidev.boundlayout.BoundLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:orientation="horizontal">

    <ImageView
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:scaleType="center"
        android:src="@drawable/ic_left_hint"
        app:displayMode="fixed" />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@android:color/white"
        android:gravity="center"
        android:text="@string/app_name" />

    <ImageView
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:scaleType="center"
        android:src="@drawable/ic_right_hint"
        app:displayMode="edge" />
</com.androidev.boundlayout.BoundLayout>
```

## 设置

> BoundLayout 最多包含三个子View，分别是头部，内容，尾部

回弹方向(orientation): 横向和纵向, 默认横向

头部尾部显示方式(displayMode): 固定, 滚动, 边缘

|显示方式|描述|
|---|---|
|固定|头部/尾部 会固定在父组件的边缘，没有任何滚动动作|
|滚动|头部/尾部 会跟随内容滚动|
|边缘|头部/尾部 滚动出现后固定在父组件的边缘|