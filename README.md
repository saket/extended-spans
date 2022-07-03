# Extended Spans

`ExtendedSpans` converts your boring `AnnotatedString` spans into rad spans.

| Before | After |
| --- | --- |
| ![Boring spans](sample/screenshots/before_light.jpg#gh-light-mode-only)![Boring spans](sample/screenshots/before_dark.jpg#gh-dark-mode-only) | ![Rad spans](sample/screenshots/after_light.gif#gh-light-mode-only)![Rad spans](sample/screenshots/after_dark.gif#gh-dark-mode-only) |


```groovy
implementation "me.saket.extendedspans:extendedspans:1.0.0"
```

```kotlin
val extendedSpans = remember {
  ExtendedSpans(
    RoundRectSpanPainter(…),
    SquigglyUnderlineSpanPainter(…)
  )
}

Text(
  modifier = Modifier.drawBehind(extendedSpans),
  text = remember(text) {
    extendedSpans.extend(text)
  },
  onTextLayout = { result ->
    extendedSpans.onTextLayout(result)
  }
)
```

You can also create your own custom spans by extending `ExtendedSpanPainter` and passing it to `ExtendedSpans`.

## License

```
Copyright 2022 Saket Narayan.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
