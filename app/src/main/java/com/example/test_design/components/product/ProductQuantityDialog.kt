package com.example.test_design.components.product

import android.app.Dialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.test_design.R
import com.example.test_design.domain.models.CartItem
import com.example.test_design.domain.models.UiProduct

@Composable
fun ProductQuantityDialog(
    product: UiProduct,
    cart: SnapshotStateList<CartItem>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val imageModel = remember(product.imageRes) {
        val path = product.imageRes
        when {
            path.startsWith("content://") || path.startsWith("file://") -> {
                path.toUri()
            }

            path.contains("R.drawable.") -> {
                val resName = path.substringAfterLast(".")
                context.resources.getIdentifier(resName, "drawable", context.packageName)
                    .takeIf { it != 0 } ?: R.drawable.placeholder_image
            }

            else -> R.drawable.placeholder_image
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
            modifier = Modifier
                .width(500.dp)
                .wrapContentHeight()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFF5F5F5)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = product.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.cart_price_format, product.price),
                    fontSize = 20.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(64.dp)
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(32.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QuantityControl(
                        product = product,
                        cart = cart,
                        onInteraction = {}
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .width(280.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4700B3))
                ) {
                    Text("Klar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}