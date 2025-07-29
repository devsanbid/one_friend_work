package com.example.meropasal.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import android.content.Context
import com.example.meropasal.Model.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.compose.ui.res.colorResource
import com.example.meropasal.R

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            LoginScreen(
                onBackClick = { finish() },
                onLoginSuccess = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onSignUpClick = {
                    startActivity(Intent(this, RegisterActivity::class.java))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val auth: FirebaseAuth = Firebase.auth


    // Color scheme
    val backgroundColor = Color(0xFFF5F5F5)
    val cardBackground = Color.White
    val inputBackground = Color(0xFFF0F0F0)
    val textPrimary = Color(0xFF2C2C2C)
    val hintColor = Color(0xFF9E9E9E)
    val blackPrimary = colorResource(R.color.darkBrown)

    fun validateInputs(): Boolean {
        usernameError = null
        passwordError = null

        if (username.trim().isEmpty()) {
            usernameError = "Username or Email is required"
            return false
        }

        if (password.isEmpty()) {
            passwordError = "Password is required"
            return false
        }

        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    fun performLogin() {
        if (!validateInputs()) return

        isLoading = true

        auth.signInWithEmailAndPassword(username.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        // Load user data from Firebase Realtime Database
                        val database = FirebaseDatabase.getInstance()
                        database.reference.child("Users").child(firebaseUser.uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    isLoading = false
                                    val userModel = snapshot.getValue(UserModel::class.java)
                                     if (userModel != null) {
                                         // Save current user ID to SharedPreferences
                                         val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                                         with(sharedPref.edit()) {
                                             putString("current_user_id", firebaseUser.uid)
                                             putBoolean("is_logged_in", true)
                                             apply()
                                         }
                                         
                                         Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                         onLoginSuccess()
                                     } else {
                                         Toast.makeText(context, "User data not found. Please contact support.", Toast.LENGTH_LONG).show()
                                     }
                                }
                                
                                override fun onCancelled(error: DatabaseError) {
                                    isLoading = false
                                    Toast.makeText(context, "Failed to load user data: ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            })
                    }
                } else {
                    isLoading = false
                    val errorMessage = when {
                        task.exception?.message?.contains("password") == true ->
                            "Invalid password"
                        task.exception?.message?.contains("user") == true ->
                            "User not found"
                        task.exception?.message?.contains("email") == true ->
                            "Invalid email format"
                        else -> "Login failed: ${task.exception?.message}"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textPrimary
                )
            }

            Text(
                text = "Meropasal",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            // Spacer for balance
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Main content card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Welcome Back Title
                Text(
                    text = "Welcome Back",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Username/Email Input
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        usernameError = null
                    },
                    label = { Text("Username or Email", color = hintColor) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = usernameError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = inputBackground,
                        unfocusedContainerColor = inputBackground,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                if (usernameError != null) {
                    Text(
                        text = usernameError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = { Text("Password", color = hintColor) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = passwordError != null,
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = inputBackground,
                        unfocusedContainerColor = inputBackground,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Login Button
                Button(
                    onClick = ::performLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = blackPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Login",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Sign Up Link
                Text(
                    text = "Don't have an account? Sign Up",
                    color = hintColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSignUpClick() }
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}