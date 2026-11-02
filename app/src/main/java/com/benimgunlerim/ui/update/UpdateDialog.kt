package com.benimgunlerim.ui.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.benimgunlerim.R
import com.benimgunlerim.domain.update.UpdatePrompt
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.molecules.SheetSaveButton
import com.benimgunlerim.ui.theme.AppTokens

/**
 * "Yeni sürüm hazır" pop-up'ı. İsteğe bağlıda "Sonra" ile kapanır; zorunluda geri tuşu ve dışarı
 * dokunma da kapatmaz — mağazadan dönünce hâlâ eski sürümdeyse yine karşısına çıkar.
 */
@Composable
fun UpdateDialog(prompt: UpdatePrompt, onLater: () -> Unit) {
    val (required, message, storeUrl) = when (prompt) {
        UpdatePrompt.None -> return
        is UpdatePrompt.Optional -> Triple(false, prompt.message, prompt.storeUrl)
        is UpdatePrompt.Required -> Triple(true, prompt.message, prompt.storeUrl)
    }
    val context = LocalContext.current
    Dialog(
        onDismissRequest = { if (!required) onLater() },
        properties = DialogProperties(dismissOnBackPress = !required, dismissOnClickOutside = !required),
    ) {
        AppSurface(
            modifier = Modifier.testTag(TestTags.UpdateDialog),
            radius = AppTokens.Radius.xxl,
            padding = AppTokens.Spacing.xl,
        ) {
            Image(
                painter = painterResource(R.drawable.mascot_mino),
                contentDescription = stringResource(R.string.mascot_content_description),
                modifier = Modifier.size(AppTokens.IconSize.mascot).align(Alignment.CenterHorizontally),
            )
            Text(
                text = stringResource(if (required) R.string.update_required_title else R.string.update_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = AppTokens.Spacing.md),
            )
            Text(
                // Console'da mesaj yazılmadıysa uygulamadaki varsayılan metin kullanılır.
                text = message.ifBlank {
                    stringResource(if (required) R.string.update_required_body else R.string.update_body)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = AppTokens.Spacing.xs),
            )
            SheetSaveButton(
                text = stringResource(R.string.update_action),
                onClick = { context.openStore(storeUrl) },
                modifier = Modifier.padding(top = AppTokens.Spacing.xl),
            )
            if (!required) {
                TextButton(
                    onClick = onLater,
                    modifier = Modifier.fillMaxWidth().padding(top = AppTokens.Spacing.xxs),
                ) { Text(stringResource(R.string.update_later)) }
            }
        }
    }
}

/**
 * Doğrudan Play Store uygulamasını açar (Samsung'da Galaxy Store da market:// adresini tanır ve
 * seçici çıkar). Play Store yoksa (ör. emülatör) tarayıcıda açar.
 */
private fun Context.openStore(storeUrl: String) {
    val packageId = Uri.parse(storeUrl).getQueryParameter("id")
    val market = packageId?.let { Uri.parse("market://details?id=$it") }
    val intent = Intent(Intent.ACTION_VIEW, market ?: Uri.parse(storeUrl))
        .setPackage(PLAY_STORE_PACKAGE)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}

private const val PLAY_STORE_PACKAGE = "com.android.vending"
