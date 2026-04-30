package com.example.test_design.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.test_design.components.menu.MenuButton
import com.example.test_design.data.utils.generateArticleNumber
import com.example.test_design.data.utils.generateEAN
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.test_design.data.dao.ProductDao
import com.example.test_design.data.entity.ProductEntity
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import android.widget.Toast

@Composable
fun AddProductScreen(
    navController: NavController,
    dao: ProductDao,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    val tempUri = remember {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempUri
        }
    }

    rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempUri)
        } else {
            Toast.makeText(context, "Kameratillåtelse krävs för att ta foto", Toast.LENGTH_SHORT).show()
        }
    }


    val scope = rememberCoroutineScope()

    var productName by remember { mutableStateOf("") }
    var articleNumber by remember { mutableStateOf(generateArticleNumber()) }
    var ean by remember { mutableStateOf(generateEAN()) }
    var unitPrice by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Dryck") }
    var vatRate by remember { mutableStateOf(25.0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ny produkt", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            MenuButton(isOpen = true, onClick = onClose)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            placeholder = { Text("Namn på produkten") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = unitPrice,
            onValueChange = { unitPrice = it },
            label = { Text("Pris (kr)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Denna information genereras automatiskt", fontSize = 12.sp, color = Color.Gray)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = articleNumber,
                onValueChange = { articleNumber = it },
                label = { Text("Art.nr") },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 14.sp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = ean,
                onValueChange = { ean = it },
                label = { Text("EAN") },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 14.sp),
                shape = RoundedCornerShape(12.dp)
            )
        }

        //Produktbild
        Text("Produktbild", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🖼️", fontSize = 32.sp)
                    Text("Tryck för att välja bild", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val newProduct = ProductEntity(
                    productName = productName,
                    articleNumber = articleNumber,
                    ean = ean,
                    baseProductCode = articleNumber, // basprodukt pekar på sig själv
                    isVariant = false,               // false = basprodukt
                    category = listOf(category),
                    unitPrice = unitPrice.toIntOrNull() ?: 0,
                    vatRate = vatRate,
                    imageResName = imageUri?.toString() ?: "placeholder_image",
                    variantType = null,
                    variantValue = null,
                    priceModifier = 0
                )
                scope.launch {
                    dao.insertProduct(newProduct)
                    onClose()
                }
            },
            enabled = productName.isNotBlank() && unitPrice.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Lägg till i lager", color = Color.White)
        }
    }
}
