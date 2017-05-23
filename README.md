# DownloadView

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-DownloadView-green.svg?style=true)](https://android-arsenal.com/details/1/2908)
[![Travis](https://img.shields.io/travis/rust-lang/rust.svg)]()
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)

**DownloadView** - An Android library that provides a realistic downloading view effect like 360-mobile-helper.

## Sample

<img src="http://7xivx9.com1.z0.glb.clouddn.com/downloadview.gif" alt="sample" title="sample" />

## Usage

**For a working implementation of this project see the `app/` folder.**

### Step 1

Include the library as a local library project or add the dependency in your build.gradle.

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

```groovy
dependencies {
    compile 'com.github.icodeu:DownloadView:v1.0'
}
```

Or

Import the library, then add it to your /settings.gradle and /app/build.gradle.


### Step 2

Include the DownloadView widget in your layout. And you can customize it like this.

```xml
<com.icodeyou.library.DownloadView
        android:id="@+id/downloadView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:shrinkDuration="1000"
        app:prepareRotateAnimDuration="1000"
        app:backgroundColor="#FF00CC99"
        app:progressColor="#4400CC99" />
```

## Customization

Please feel free to :)

|name|format|description|
|:---:|:---:|:---:|
| textSize | integer | The text writed size, default is 40px
| textColor | color | The text writed size, default is 0xFFFFFFFF, White
| backgroundColor | color | The background color of the view, default is 0xFF00CC99, which like dark green
| progressColor | color | The color of the progress, default is 0x4400CC99, which like light green
| shrinkDuration | integer | The duration of the shrink animation in first stage, unit is millisecond, default is 1000
| prepareRotateAnimDuration | integer | The duration of the prepare-rotate animation in second stage, unit is millisecond, default is 1000
| prepareRotateAnimSpeed | integer | The speed of the prepare-rotate animation in second stage, unit is angle, default is 10
| expandAnimDuration | integer | The duration of the expand animation in thrid stage, unit is millisecond, default is 1000
| loadRotateAnimSpeed | integer | The speed of the load-rotate animation in fourth stage, unit is angle, default is 5
| movePointDuration | integer | The duration of the move-point animation in fourth stage, unit is millisecond, default is 3000

**All attributes have their respective getters and setters to change them at runtime.**

## Contact Me

Born in 1992, graduated from BJTU University, now a developer of Android in Baidu. Loving technology, programming, reading books.

## License

    Copyright 2017 icodeyou

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.