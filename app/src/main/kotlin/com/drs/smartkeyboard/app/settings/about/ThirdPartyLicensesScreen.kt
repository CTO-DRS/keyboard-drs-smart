/*
 * Copyright (C) 2025 The DRS Smart Keyboard Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drs.smartkeyboard.app.settings.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.lib.compose.DrsScreen
import org.drs.lib.compose.stringRes

/**
 * Static, fully self-contained licenses screen of DRS Smart Keyboard.
 *
 * This screen intentionally does NOT rely on any external metadata generator
 * (which previously crashed with IllegalStateException when the generated
 * library data was missing). All content below is bundled directly in the
 * app binary, so this screen can never fail at runtime again.
 */
@Composable
fun ThirdPartyLicensesScreen() = DrsScreen {
    title = stringRes(R.string.about__third_party_licenses__title)
    scrollable = false
    iconSpaceReserved = false

    val context = LocalContext.current
    val licenseUrl = stringRes(R.string.drs__license_url)

    content {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LicenseCard {
                Text(
                    text = stringRes(R.string.about__licenses__intro),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            LicenseCard {
                Text(
                    text = stringRes(R.string.about__licenses__apache_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringRes(R.string.about__licenses__apache_summary),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            LicenseCard {
                Text(
                    text = stringRes(R.string.about__licenses__terms_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringRes(R.string.about__licenses__terms_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            LicenseCard {
                Text(
                    text = stringRes(R.string.about__licenses__full_text_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringRes(R.string.about__licenses__full_text_body),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(licenseUrl)),
                            )
                        }
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                text = stringRes(R.string.about__licenses__view_on_github),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun LicenseCard(
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        content()
    }
}
