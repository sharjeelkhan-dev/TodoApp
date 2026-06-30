package com.todoapp.presentation.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.todoapp.R
import com.todoapp.presentation.theme.TodoAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginState,
    isDarkMode: Boolean,
    onEvent: (LoginEvent) -> Unit,
    onNavigateToHome: () -> Unit
) {
    val brandColor = Color(0xFF7B61FF)
    val bgColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFFBFBF9)
    val primaryText = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val secondaryText = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Logo Section ---
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF6C52EE), Color(0xFF8E75FF))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.notepad_icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                text = if (state.isSignUpMode) stringResource(R.string.create_account) else stringResource(R.string.welcome_back),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = primaryText,
                    fontSize = 28.sp
                )
            )
            Text(
                text = if (state.isSignUpMode) stringResource(R.string.join_us) else stringResource(R.string.login_desc),
                color = secondaryText,
                modifier = Modifier.offset(y = (-10).dp),
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            // --- Form Section ---
            AnimatedVisibility(visible = state.isSignUpMode) {
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = { onEvent(LoginEvent.DisplayNameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.full_name)) },
                    placeholder = { Text(stringResource(R.string.john_doe)) },
                    leadingIcon = { Icon(Icons.Default.Person,
                        contentDescription = null, tint = brandColor) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandColor,
                        unfocusedBorderColor = secondaryText.copy(alpha = 0.3f),
                        focusedLabelColor = brandColor,
                        cursorColor = brandColor
                    )
                )
            }
            OutlinedTextField(
                value = state.email,
                onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.email_address)) },
                placeholder = { Text(stringResource(R.string.example_mail)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = brandColor) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandColor,
                    unfocusedBorderColor = secondaryText.copy(alpha = 0.3f),
                    focusedLabelColor = brandColor,
                    cursorColor = brandColor
                )
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.password)) },
                leadingIcon = { Icon(Icons.Default.Lock,
                    contentDescription = null, tint = brandColor) },
                trailingIcon = {
                    IconButton(onClick = { onEvent(LoginEvent.TogglePasswordVisibility) }) {
                        Icon(
                            imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = secondaryText
                        )
                    }
                },
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandColor,
                    unfocusedBorderColor = secondaryText.copy(alpha = 0.3f),
                    focusedLabelColor = brandColor,
                    cursorColor = brandColor
                )
            )
            AnimatedVisibility(visible = state.isSignUpMode) {
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = { onEvent(LoginEvent.ConfirmPasswordChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.confirm_password)) },
                    leadingIcon = { Icon(Icons.Default.Lock,
                        contentDescription = null, tint = brandColor) },
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandColor,
                        unfocusedBorderColor = secondaryText.copy(alpha = 0.3f),
                        focusedLabelColor = brandColor,
                        cursorColor = brandColor
                    )
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            // --- Action Buttons ---
            Button(
                onClick = { if (state.isSignUpMode) onEvent(LoginEvent.SignUp)
                else onEvent(LoginEvent.SignIn) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (state.isSignUpMode) stringResource(R.string.sign_up) else stringResource(R.string.sign_in), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().offset(y = (-20).dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (state.isSignUpMode) stringResource(R.string.already_have_account)
                    else stringResource(R.string.dont_have_account),
                    color = secondaryText,
                    fontSize = 14.sp
                )
                TextButton(onClick = { onEvent(LoginEvent.ToggleMode) }) {
                    Text(
                        if (state.isSignUpMode) stringResource(R.string.sign_in)
                        else stringResource(R.string.sign_up),
                        color = brandColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TodoAppTheme(darkTheme = false) {
        LoginScreen(
            state = LoginState(),
            isDarkMode = false,
            onEvent = {},
            onNavigateToHome = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    TodoAppTheme(darkTheme = false) {
        LoginScreen(
            state = LoginState(isSignUpMode = true),
            isDarkMode = false,
            onEvent = {},
            onNavigateToHome = {}
        )
    }
}
