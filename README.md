# BoundLayout

## Introduction [中文](https://github.com/4ndroidev/BoundLayout/blob/master/README_CN.md)

In the abstract, the library supports bound-effect of all kinds of view which can scroll or can't scroll

## Screenshot

![boundlayout.gif](https://github.com/4ndroidev/BoundLayout/blob/master/screenshot/boundlayout.gif)

## Usage

### step 1

add dependency :

	compile 'com.androidev:boundlayout:1.0.0'

### step 2

modify your layout file, such as :

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

## Settings

> BoundLayout can host at most 3 children, header, content and footer respectively

orientation of BoundLayout: horizontal or vertical, default horizontal

displayMode of header and footer: fixed, scroll, edge

|displayMode|description|
|---|---|
|fixed|header/footer fixed at the edge of parent without any scrolling|
|scroll|header/footer will scroll following the content|
|edge|header/footer will scroll to appear then fix at the edge|