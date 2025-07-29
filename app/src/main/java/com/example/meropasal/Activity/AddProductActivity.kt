package com.example.meropasal.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.meropasal.Model.ItemsModel
import com.example.meropasal.R
import com.example.meropasal.Helper.CloudinaryHelper
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AddProductActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AddProductScreen(
                onBackClick = { finish() },
                onProductAdded = {
                    Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onBackClick: () -> Unit,
    onProductAdded: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Add Product",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Product Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Image",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = "Tap to add image (optional)",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Product Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price ($)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = categoryId,
                onValueChange = { categoryId = it },
                label = { Text("Category ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank() && 
                        price.isNotBlank() && categoryId.isNotBlank()) {
                        isLoading = true
                        uploadProductToFirebase(
                            title = title,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            categoryId = categoryId.toIntOrNull() ?: 0,
                            imageUri = imageUri ?: Uri.EMPTY,
                            context = context,
                            onSuccess = {
                                isLoading = false
                                onProductAdded()
                            },
                            onError = { error ->
                                isLoading = false
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Add Product",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun uploadProductToFirebase(
    title: String,
    description: String,
    price: Double,
    categoryId: Int,
    imageUri: Uri,
    context: android.content.Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val database = FirebaseDatabase.getInstance()
    
    val productId = database.reference.child("Items").push().key
    if (productId != null) {
        // Use coroutine to handle Cloudinary upload
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageUrl = if (imageUri != Uri.EMPTY) {
                    // Upload image to Cloudinary
                    CloudinaryHelper.uploadImage(context, imageUri)
                } else {
                    // Use placeholder if no image selected
                    "https://via.placeholder.com/300x300.png?text=Product+Image"
                }
                
                val product = ItemsModel(
                    title = title,
                    description = description,
                    picUrl = arrayListOf(imageUrl),
                    price = price,
                    categoryId = categoryId.toString(),
                    showRecommended = true,
                    numberInCart = 0
                )
                
                // Switch back to main thread for Firebase operations
                withContext(Dispatchers.Main) {
                    database.reference.child("Items").child(productId)
                        .setValue(product)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            onError(exception.message ?: "Failed to save product")
                        }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Failed to upload image: ${e.message}")
                }
            }
        }
    } else {
        onError("Failed to generate product ID")
    }
}