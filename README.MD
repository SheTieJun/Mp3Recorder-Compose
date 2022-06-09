## MP3Recorder 提供Compose 支持（实验性）

### Compose Version
```
compose_version = '1.2.0-alpha07'
```

### 创建Mp3Recorder

```
   val mp3Recorder = rememberRecorderState() { isAutoComplete, file ->
        Log.i("rememberRecorderState", "isAutoComplete = $isAutoComplete || file = $file")
   }
```

## 相关参数
```
    val recorderState: MutableState<RecorderState>, //录音状态
    val recordTime: MutableState<Long>, //录音时间的长度
    val currentVolume: MutableState<Int>, //当前声音大小
    val maxTime: MutableState<Int>,//最大录制时间
    val isPlayIngBgm: MutableState<BGMState>, //背景音乐的状态
    val bgmDuration: MutableState<Int>, //背景音乐总时长
    val bgmTime: MutableState<Int>,//背景音乐播放的时长
```


## DEMO  TODO
- [ ] 背景音乐
- [ ] 历史记录