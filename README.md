# DragFooterView

[中文版README](https://github.com/uin3566/DragFooterView/blob/master/README-CN.md)  

![](https://jitpack.io/v/uin3566/DragFooterView.svg)  
A ViewGroup with a draggable footer

### Demo gif
![screenshot](/screenshot/demo.gif)

### Inspired by
![screenshot](/screenshot/inspiration.gif) 

### Customize your own footers
As a flexible library, it should not has only one footer effect, so you can custom your own footer effect,
below is my custom footer effect, you can get it in demo code. To custom footer view, you should only inherit
BaseFooterDrawer class, and draw the footer in footerRegion rectf.  
![screenshot](/screenshot/custom1.gif)    
![screenshot](/screenshot/custom2.gif) 

### Add to your project
* step1:Add it in your root build.gradle at the end of repositories:
```xml
    allprojects {
        repositories {
            ...
	        maven { url "https://jitpack.io" }
        }
    }
```
* step2:Add the dependency:
```
    dependencies {
        compile 'com.github.uin3566:DragFooterView:v1.0.1'
    }
```

### Usage
1、add in xml like this **(Attention:it should have only one child view)**
```xml
    <com.fangxu.library.DragContainer
        android:id="@+id/drag_recycler_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginLeft="10dp">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/recycler_view"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@android:color/white" />
    </com.fangxu.library.DragContainer>
```
2、in java code,add DragListener
```java
    DragContainer dragContainer = (DragContainer) findViewById(R.id.drag_image_view);
    
    //if you want to use your own custom footer, you should set your own footer to
    //the DragContainer like this
    dragContainer.setFooterDrawer(new ArrowPathFooterDrawer.Builder(this, 0xff444444).setPathColor(0xffffffff).build());
    
    //set listener
    dragContainer.setDragListener(new DragListener() {
        @Override
        public void onDragEvent() {
            //do whatever you want,for example skip to the load more Activity.
            Intent intent = new Intent(HomeActivity.this, ShowMoreActivity.class);
            startActivity(intent);
        }
    });
```

###Attributes
all of the attributes are listed below:  

|attribute|value type|defalut value| description|
| --- | --- | --- | --- |
|dc_footer_color|color|0xffcdcdcd|the color of footer background|
|dc_reset_animator_duration|integer|700|the reset animator duration in milliseconds|
|dc_drag_damp|float|0.5f|the drag damp,should be set in range (0,1],set it smaller will drag more difficultly|


###License
```
Copyright (c) 2016 uin3566 <xufang2@foxmail.com>

Licensed under the Apache License, Version 2.0 (the "License”);
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
   
   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
