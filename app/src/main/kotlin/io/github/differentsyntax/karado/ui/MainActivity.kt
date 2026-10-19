package io.github.differentsyntax.karado.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.differentsyntax.karado.engine.Mode
import io.github.differentsyntax.karado.guide.GuideController
import io.github.differentsyntax.karado.guide.GuideService
import io.github.differentsyntax.karado.guide.RecipeRepository
import io.github.differentsyntax.karado.recipes.Lang
import io.github.differentsyntax.karado.recipes.Localized
import io.github.differentsyntax.karado.recipes.Recipe

class MainActivity : ComponentActivity() {
    private val serviceOn = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("karado", MODE_PRIVATE)
        GuideController.lang = if (prefs.getString("lang", "hi") == "en") Lang.EN else Lang.HI
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    App(
                        prefs = prefs,
                        serviceOn = serviceOn.value,
                        recipes = RecipeRepository.all(this),
                        openAccessibilitySettings = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        start = { recipe, mode -> GuideController.start(this, recipe, mode) },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        serviceOn.value = isServiceEnabled(this)
    }
}

private fun isServiceEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        ?: return false
    val me = ComponentName(context, GuideService::class.java)
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
}

/** Short bilingual strings for this screen. Hinglish style: tech words stay in English letters. */
private object T {
    val helperQuestion = Localized(
        "Is someone you know and trust helping you set this up right now?",
        "क्या कोई भरोसेमंद, जान-पहचान वाला व्यक्ति अभी setup में आपकी मदद कर रहा है?",
    )
    val yes = Localized("Yes", "हाँ")
    val no = Localized("No", "नहीं")
    val neverCalls = Localized(
        "Kara Do will never call you, message you, or ask you to install anything. If someone does, it is a scam.",
        "Kara Do कभी आपको call या message नहीं करेगा, और कुछ install करने को नहीं कहेगा। अगर कोई ऐसा करे, तो वो धोखा है।",
    )
    val waitForHelper = Localized(
        "Please set this up together with a family member or friend who knows phones. Never set up any app because a stranger asked you to.",
        "इसे अपने घर के किसी सदस्य या जान-पहचान वाले के साथ बैठकर setup करिए। किसी अनजान के कहने पर कभी कोई app setup मत करिए।",
    )
    val continueAnyway = Localized("I understand, continue", "समझ गए, आगे बढ़िए")
    val disclosureTitle = Localized("What Kara Do can see", "Kara Do क्या देख सकता है")
    val disclosure = Localized(
        "• Only while a guide is running, and only in the app being guided, Kara Do reads the buttons on the screen to show you where to tap.\n" +
            "• It never taps, types or does anything for you.\n" +
            "• It never reads password fields and does not work in payment apps.\n" +
            "• It has no internet access. Nothing on your screen can leave your phone.\n" +
            "• Nothing is saved.",
        "• सिर्फ़ guide चलते समय, और सिर्फ़ उसी app में, Kara Do screen के buttons पढ़ता है ताकि बता सके कहाँ tap करना है।\n" +
            "• यह खुद कभी tap, type या कोई काम नहीं करता।\n" +
            "• Password कभी नहीं पढ़ता, और payment वाले apps में काम नहीं करता।\n" +
            "• इसमें internet नहीं है। आपकी screen की कोई बात phone से बाहर नहीं जा सकती।\n" +
            "• कुछ भी save नहीं होता।",
    )
    val agree = Localized("I agree", "मैं सहमत हूँ")
    val turnOn = Localized(
        "Now turn on Kara Do in Accessibility settings. Do this together with your helper.",
        "अब Accessibility settings में Kara Do को on करिए। यह अपने helper के साथ करिए।",
    )
    val openSettings = Localized("Open Accessibility settings", "Accessibility settings खोलिए")
    val whatToDo = Localized("What would you like to do?", "आप क्या करना चाहते हैं?")
    val teachRoute = Localized("Teach me the way", "रास्ता सिखाइए")
    val takeMe = Localized("Take me straight there", "सीधे ले चलिए")
}

@Composable
private fun App(
    prefs: android.content.SharedPreferences,
    serviceOn: Boolean,
    recipes: List<Recipe>,
    openAccessibilitySettings: () -> Unit,
    start: (Recipe, Mode) -> Boolean,
) {
    var lang by remember { mutableStateOf(GuideController.lang) }
    var helperAnswered by remember { mutableStateOf(prefs.getBoolean("helper_answered", false)) }
    var helperMissing by remember { mutableStateOf(false) }
    var consented by remember { mutableStateOf(prefs.getBoolean("disclosure_accepted", false)) }
    fun t(l: Localized) = l.get(lang)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (l in Lang.entries) {
                val label = if (l == Lang.HI) "हिंदी" else "English"
                val onClick = {
                    lang = l
                    GuideController.lang = l
                    prefs.edit().putString("lang", if (l == Lang.EN) "en" else "hi").apply()
                }
                if (l == lang) Button(onClick) { Text(label, fontSize = 18.sp) }
                else OutlinedButton(onClick) { Text(label, fontSize = 18.sp) }
            }
        }

        when {
            !helperAnswered && !helperMissing -> {
                Big(t(T.helperQuestion))
                Body(t(T.neverCalls))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button({ helperAnswered = true; prefs.edit().putBoolean("helper_answered", true).apply() }) { Big(t(T.yes)) }
                    OutlinedButton({ helperMissing = true }) { Big(t(T.no)) }
                }
            }
            helperMissing && !helperAnswered -> {
                Big(t(T.waitForHelper))
                Body(t(T.neverCalls))
                OutlinedButton({ helperAnswered = true; prefs.edit().putBoolean("helper_answered", true).apply() }) {
                    Text(t(T.continueAnyway), fontSize = 18.sp)
                }
            }
            !consented -> {
                Big(t(T.disclosureTitle))
                Body(t(T.disclosure))
                Button({ consented = true; prefs.edit().putBoolean("disclosure_accepted", true).apply() }) { Big(t(T.agree)) }
            }
            !serviceOn -> {
                Big(t(T.turnOn))
                Button(openAccessibilitySettings) { Big(t(T.openSettings)) }
            }
            else -> {
                Big(t(T.whatToDo))
                for (recipe in recipes) RecipeCard(recipe, lang, start)
            }
        }
    }
}

@Composable
private fun RecipeCard(recipe: Recipe, lang: Lang, start: (Recipe, Mode) -> Boolean) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Big(recipe.title.get(lang))
            Button({ start(recipe, Mode.ROUTE) }, Modifier.fillMaxWidth()) { Text(T.teachRoute.get(lang), fontSize = 20.sp) }
            if (recipe.shortcut != null) {
                OutlinedButton({ start(recipe, Mode.SHORTCUT) }, Modifier.fillMaxWidth()) { Text(T.takeMe.get(lang), fontSize = 20.sp) }
            }
        }
    }
}

@Composable
private fun Big(text: String) = Text(text, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, lineHeight = 34.sp)

@Composable
private fun Body(text: String) = Text(text, fontSize = 20.sp, lineHeight = 30.sp)
