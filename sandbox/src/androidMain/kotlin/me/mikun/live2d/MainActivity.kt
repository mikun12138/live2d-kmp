package me.mikun.live2d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Hello114514()
        }
    }
}

@Preview
@Composable
fun Live2d() {

}

@Preview(
    showBackground = true
)
@Composable
fun Hello() {
    BasicText(
        text = "Hello Live2D! ",
    )
}

@Preview(
    showBackground = true
)
@Composable
fun Hello114514() {
    Column(
        modifier = Modifier.padding(8.0f.dp)
    ) {
        Row {
            Hello()
        }
        Row {
            Hello()
        }
        Row {
            Hello()
            Hello()
            Hello()
            Hello()
        }
        Row {
            Hello()
            Hello()
            Hello()
            Hello()
            Hello()
        }
        Row {
            Hello()
        }
        Row {
            Hello()
            Hello()
            Hello()
            Hello()
        }
    }
}

