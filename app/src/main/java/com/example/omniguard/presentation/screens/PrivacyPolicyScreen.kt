package com.example.omniguard.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "PRIVACY POLICY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Privacy Policy for OmniGuard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Last Updated: January 1, 2024",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            PrivacySectionCard(
                title = "1. Information We Collect",
                content = "OmniGuard collects the following information to provide its core functionality:\n\n" +
                        "• Storage information (total, used, free space)\n" +
                        "• RAM usage statistics\n" +
                        "• Battery health and status\n" +
                        "• App usage statistics (with your permission)\n" +
                        "• Device information (model, Android version)"
            )

            PrivacySectionCard(
                title = "2. How We Use Your Information",
                content = "The information collected is used solely for:\n\n" +
                        "• Displaying device performance metrics\n" +
                        "• Identifying unused apps for cleanup suggestions\n" +
                        "• Monitoring battery health\n" +
                        "• Providing storage analysis\n" +
                        "• Improving app functionality"
            )

            PrivacySectionCard(
                title = "3. Data Storage",
                content = "All data collected by OmniGuard is stored locally on your device. " +
                        "We do not upload, store, or process any of your personal information on external servers. " +
                        "Your privacy is our priority."
            )

            PrivacySectionCard(
                title = "4. Permissions",
                content = "OmniGuard requires the following permissions:\n\n" +
                        "• Usage Access Permission: Used to detect unused apps and analyze app usage patterns\n" +
                        "• Storage Permission: Used to analyze storage usage\n" +
                        "• Internet Permission: Used only for checking updates and reporting issues (optional)"
            )

            PrivacySectionCard(
                title = "5. Data Sharing",
                content = "We do not sell, trade, or otherwise transfer your personal information to outside parties. " +
                        "When you choose to report an issue, device information is shared only with your explicit consent " +
                        "via email or GitHub issue."
            )

            PrivacySectionCard(
                title = "6. Third-Party Services",
                content = "OmniGuard does not use any third-party analytics, advertising, or tracking services. " +
                        "The only external services used are:\n\n" +
                        "• GitHub (for update checking and issue reporting)\n" +
                        "• Email (for support requests)"
            )

            PrivacySectionCard(
                title = "7. Your Rights",
                content = "You have the right to:\n\n" +
                        "• Access all data stored by the app\n" +
                        "• Delete the app to remove all stored data\n" +
                        "• Deny permissions (though some features may not work)\n" +
                        "• Contact us with privacy concerns"
            )

            PrivacySectionCard(
                title = "8. Children's Privacy",
                content = "OmniGuard does not knowingly collect personal information from children under 13. " +
                        "If you are a parent and believe your child has provided us with personal information, please contact us."
            )

            PrivacySectionCard(
                title = "9. Changes to This Policy",
                content = "We may update this privacy policy from time to time. " +
                        "You will be notified of any changes by updating the 'Last Updated' date at the top of this policy."
            )

            PrivacySectionCard(
                title = "10. Contact Us",
                content = "If you have any questions about this privacy policy, please contact us at:\n\n" +
                        "Email: tmahmud547@gmail.com\n" +
                        "GitHub: https://github.com/TanimStu068/omniguard"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PrivacySectionCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}
