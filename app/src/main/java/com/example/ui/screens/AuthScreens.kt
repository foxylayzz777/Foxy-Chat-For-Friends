package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CuteBackground,
                        CuteLavenderContainer,
                        Color.White
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Cute Hero Illustration
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.White)
                    .border(2.dp, CuteBorder, RoundedCornerShape(36.dp))
                    .shadow(12.dp, RoundedCornerShape(36.dp), spotColor = CuteLavenderPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_welcome_cat),
                    contentDescription = "Foxy Chat Mascot",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Foxy Chat",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CuteTextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "💜",
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Make Real Friends\nChat Freely",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = CuteTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Action Buttons
            Button(
                onClick = onNavigateToLogin,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("welcome_login_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CuteLavenderPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Log In",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onNavigateToRegister,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("welcome_register_button"),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CuteLavenderPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CuteLavenderPrimary)
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AuthScreen(
    initialIsRegister: Boolean = false,
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    var isRegister by remember { mutableStateOf(initialIsRegister) }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CuteBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("auth_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = CuteTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cute small logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CuteLavenderContainer)
                    .border(2.dp, CuteBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_icon),
                    contentDescription = "Foxy Chat Icon",
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isRegister) "Create Account ✨" else "Welcome Back ✨",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = CuteTextPrimary
            )

            Text(
                text = if (isRegister) "Join Foxy Chat & chat freely" else "Login to continue",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = CuteTextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Error banner
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CutePinkHeartLight),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = CutePinkHeart,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Input Fields
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    errorMessage = null
                },
                label = { Text("Username") },
                placeholder = { Text("e.g. cute_cat") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = CuteLavenderPrimary)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input"),
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(imeAction = if (isRegister) ImeAction.Next else ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CuteTextPrimary,
                    unfocusedTextColor = CuteTextPrimary,
                    focusedLabelColor = CuteLavenderDark,
                    unfocusedLabelColor = CuteTextSecondary,
                    focusedBorderColor = CuteLavenderPrimary,
                    unfocusedBorderColor = CuteBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            if (isRegister) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        errorMessage = null
                    },
                    label = { Text("Display Name") },
                    placeholder = { Text("What friends should call you") },
                    leadingIcon = {
                        Icon(Icons.Default.Face, contentDescription = null, tint = CuteLavenderPrimary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("display_name_input"),
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CuteTextPrimary,
                        unfocusedTextColor = CuteTextPrimary,
                        focusedLabelColor = CuteLavenderDark,
                        unfocusedLabelColor = CuteTextSecondary,
                        focusedBorderColor = CuteLavenderPrimary,
                        unfocusedBorderColor = CuteBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password") },
                placeholder = { Text("At least 6 characters") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CuteLavenderPrimary)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = CuteTextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CuteTextPrimary,
                    unfocusedTextColor = CuteTextPrimary,
                    focusedLabelColor = CuteLavenderDark,
                    unfocusedLabelColor = CuteTextSecondary,
                    focusedBorderColor = CuteLavenderPrimary,
                    unfocusedBorderColor = CuteBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Submit Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (isRegister) {
                        viewModel.register(username, displayName, password) { success, err ->
                            if (!success) errorMessage = err
                        }
                    } else {
                        viewModel.login(username, password) { success, err ->
                            if (!success) errorMessage = err
                        }
                    }
                },
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("auth_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CuteLavenderPrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                } else {
                    Text(
                        text = if (isRegister) "Create Account 💜" else "Log In ✨",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch Mode Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isRegister) "Already have an account?" else "Don't have an account?",
                    color = CuteTextSecondary,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = {
                        isRegister = !isRegister
                        errorMessage = null
                    },
                    modifier = Modifier.testTag("switch_auth_mode_button")
                ) {
                    Text(
                        text = if (isRegister) "Log In" else "Sign Up",
                        color = CuteLavenderPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
