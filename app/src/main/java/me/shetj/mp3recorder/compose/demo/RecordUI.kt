package me.shetj.mp3recorder.compose.demo

import android.Manifest.permission
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.HomeMini
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import me.shetj.mp3recorder.compose.demo.ui.theme.font_noto_sans
import me.shetj.recorder.compose.RecorderState
import me.shetj.recorder.compose.RecorderState.RecordError
import me.shetj.recorder.compose.RecorderState.RecordIng
import me.shetj.recorder.compose.RecorderState.RecordPause
import me.shetj.recorder.compose.RecorderState.RecordPermission
import me.shetj.recorder.compose.RecorderState.RecordStop
import me.shetj.recorder.compose.rememberRecorderState


@ExperimentalPermissionsApi
@ExperimentalPagerApi
@ExperimentalMaterial3Api
@Composable
fun RecordUI(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val recordStateInfo = rememberRecorderState() { isAutoComplete, file ->
        Log.i("rememberRecorderState", "isAutoComplete = $isAutoComplete || file = $file")
        Toast.makeText(context,"录制完成：$file",Toast.LENGTH_LONG).show()
    }

    val isShowDialog = remember {
        mutableStateOf(false)
    }

    val isRecordError = recordStateInfo.state is RecordError


    val needPermis = recordStateInfo.state == RecordPermission
    if (isRecordError || needPermis) {
        isShowDialog.value = true
    }

    ShowRecordPermissionDialog(isShowDialog)

    Scaffold(modifier = modifier,
        topBar = {
            DemoTopBar("Mp3Recorder")
        }) {
        val file = context.filesDir.absolutePath + "/test.mp3"


        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 120.dp)
                    .wrapContentSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formatSeconds4(recordStateInfo.recTime),
                    fontFamily = font_noto_sans,
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Row(
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .offset(y = (-20).dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RecordImageView(
                    recordState = recordStateInfo.state, modifier = Modifier
                        .size(64.dp)

                ) {
                    recordStateInfo.startOrPause(file)
                }

                if (recordStateInfo.isActive){
                    CompleteImage(modifier = Modifier
                        .size(64.dp)
                    ){
                        recordStateInfo.complete()
                    }
                }
            }
        }
    }
}

@Composable
fun CompleteImage(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Image(painter = painterResource(R.mipmap.icon_record_save),
        contentDescription = "record state",
        modifier = modifier
            .background(Color(0xFFf5f5f5.toInt()), shape = CircleShape)
            .cir()
            .clickable {
                onClick.invoke()
            },
    contentScale = ContentScale.Inside)

}

@Composable
fun RecordImageView(recordState:  RecorderState, modifier: Modifier = Modifier, onClick: () -> Unit) {

    val painterResource = painterResource(
        when (recordState) {
            is RecordError -> R.mipmap.icon_start_record
            RecordIng -> R.mipmap.icon_record_pause_2
            RecordPause -> R.mipmap.icon_start_record
            RecordPermission -> R.mipmap.icon_start_record
            RecordStop -> R.mipmap.icon_start_record
        }
    )

    Image(painter = painterResource,
        contentDescription = "record state",
        modifier = modifier
            .background(Color.Red, shape = CircleShape)
            .cir()
            .clickable {
                onClick.invoke()
            })

}


fun formatSeconds4(seconds: Long): String {
    val secondsx = seconds % 1000
    val seconds = seconds / 1000
    return (getTwoDecimalsValue(seconds.toInt() / 60) + ":"
            + getTwoDecimalsValue(seconds.toInt() % 60)) + "." + getTwoDecimalsValue(secondsx.toInt() / 10)
}

private fun getTwoDecimalsValue(value: Int): String {
    return if (value in 0..9) {
        "0$value"
    } else {
        value.toString() + ""
    }
}

@ExperimentalPermissionsApi
@Composable
fun ShowRecordPermissionDialog(openDialog: MutableState<Boolean>) {

    val cameraPermissionState = rememberMultiplePermissionsState(
        listOf(permission.RECORD_AUDIO)
    )

    if (!openDialog.value) {
        return
    }

    AlertDialog(
        onDismissRequest = {
            openDialog.value = false
        },
//        icon = { Icon(Icons.Filled.Camera, contentDescription = null) },
        title = {
            Text(text = "获取权限")
        },
        text = {
            val textToShow = if (cameraPermissionState.allPermissionsGranted) {
                "The Record audio permission is granted"
            } else if (cameraPermissionState.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "The Record audio is important for this app. Please grant the permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                buildString {
                    cameraPermissionState.revokedPermissions.forEach {
                        append(it.permission)
                        append(" ")
                    }
                    append(
                        " required for this feature to be available. " +
                                "Please grant the permission"
                    )
                }
            }
            Text(textToShow)
        },
        confirmButton = {
            if (!cameraPermissionState.allPermissionsGranted) {
                TextButton(
                    onClick = {
                        cameraPermissionState.launchMultiplePermissionRequest()
                        openDialog.value = false
                    }
                ) {
                    Text("Request permission")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    openDialog.value = false
                }
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * [ShapeTokens] 圆角
 */
fun Modifier.cir(): Modifier {
    return this.clip(shape = CircleShape)
}

@ExperimentalMaterial3Api
@Composable
fun DemoTopBar(title: String) {

    val bgColor = MaterialTheme.colorScheme.background
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }
    SmallTopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {
            Row {
                Icon(
                    imageVector = Filled.HomeMini,
                    contentDescription = "change Theme",
                )
                Text(title)
            }
        },
        navigationIcon = {

        },
        actions = {
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = bgColor,
            titleContentColor = contentColorFor(backgroundColor = bgColor),
            actionIconContentColor = contentColorFor(backgroundColor = bgColor),
            navigationIconContentColor = contentColorFor(bgColor),
            scrolledContainerColor = bgColor
        ),
        scrollBehavior = scrollBehavior
    )
}
