<div align="center">

# DRS Smart Keyboard V 1.25.0

## الجولة الشاملة الخامسة والعشرون — التفضيل والنبض الحي: المستخدم يختار أين يُسمع كلامه والشريط يتنفس بصوته

**Twenty-Fifth Comprehensive Round — Preference and Live Pulse: the User Picks Where Speech Is Heard and the Bar Breathes with the Actual Voice**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.25.0 — جولة التفضيل والنبض الحي

فحص ثلاثي جديد تحقق من كل مرشحات الجولة الماضية سطرًا سطرًا بالكود الفعلي:
الإملاء الصوتي كانت تختار متعرّفه **ضمنيًا بلا رأي للمستخدم**، ورمز
الميكروفون كان ينبض نبضة زمنية ثابتة بينما محرّك التعرّف **يرسل قياس سعة
صوتك في كل إطار ولا أحد يقرؤه** (`onRmsChanged = Unit` منذ v1.23)،
وآخر عنصر وهمي تراثي في تعداد أوضاع النافذة (THUMBS) كان بلا أي منتج
أو كاتب حفظ، وآخر سكربتَي سلاسل كانا بلا بوابة التوازي الموحدة. كل بند
أدناه عمل حقيقي مُختبر، لا ترقيم أسطر.

- 🎛️ **بوابة المتعرّف الثانية — «أين يُسمع كلامك؟»** — حتى هذا الإصدار
  كان اختيار المتعرّف ضمنيًا: على الجهاز إن وُجد، وإلا فالقياسي. الآن
  `voice__recognizer_mode` بثلاثة أوضاع قرارًا حقيقيًا يُفحص في **كل
  ضغطة ميكروفون** عبر `decideVoiceInputRoute(recognizerMode,
  onDeviceAvailable)`: **تلقائي** (السلوك الأصلي — على الجهاز إن توفر)،
  **على الجهاز فقط** (لا خدمة شبكية أبدًا — فإن لم يستطع النظام
  honoring الطلب صاحَ بالتوست الصادق الجديد بدل سقوط صامت إلى سحابة
  رفضها المستخدم)، و**الخدمة القياسية** (لمن خابت جودة محرك جهازه).
  والترتيب الصادق باقٍ: الخصوصية أولًا، ثم مفتاح الإيقاف، ثم بوابة
  الوضع، ثم التوفر، ثم الإذن. والمتحكم يعيد قراءة التفضيل عند كل بدء
  جلسة فالتغيير من الإعدادات يُحترم فورًا، والمفتاظ قائمة اختيار في
  مجموعة «الإدخال الصوتي» بشاشة إعدادات الكتابة.

- 🎙️ **الشريط يتنفس بصوتك الحقيقي** — `onRmsChanged` المُهمَل منذ
  وُلد الإملاء المدمج يُغذّي أخيرًا قناة سعة حية على `DrsVoiceInputBus`:
  الدالة النقية `rmsToAmplitude` تطبيع تقرير الديسيبل على نافذة
  ‎-2..12dB وتقنّنها بخطوات 2% كي لا يعيد الشريط الرسم على كل ارتجاف
  عشري، فيرسم الشريط **موجة خماسية حية** ترتفع بأصواتك وتخفت بصمتك،
  ملونة بلون مقدمة عنصر الشريط نفسه فتلتزمها كل الثيمات. وصدقًا مع
  الواقع: كثير من الخدمات لا ترسل RMS أصلًا، فنبضة v1.24 تبقى
  الاحتياط الظاهر حتى تتدفق عينات فعلية — وكل جلسة تبدأ من الصمت لا من
  آخر سعة سابقة.

- 🧹 **مسح TODO/FIXME الصادق** — `ImeWindowMode.Fixed.THUMBS` آخر
  placeholder تراثي في تعداد النافذة حُذف من ثلاثة مواضع (التعداد،
  فرع المصنع، فئة القيود التي كانت خصائصها مطابقة بايتًا لـNORMAL)
  بلا أي هجرة حفظ — لا كاتب يُنتج قيمة THUMBS أصلًا والفك المقروء
  ينهار بأمان إلى الافتراضي. و`DictionaryManager` نال أخيرًا KDoc
  حقيقيًا يوثّق ولايته ومسؤولياته، وملاحظات DrsEmojiCompat التحقيقية
  الثلاث وسؤال «هل أحتاج قفلًا للقاعدة؟» في LanguagePackExtension
  صارت تدقيقًا صادقًا ببدائل الـTODO، وDebugSummarizeUtils يوثّق لماذا
  تبقى قيم الـBundle غير مطبوعة عمدًا (فخ ClassCastException القديم).

- 🛠️ **مسح utils/ والسكربتات** — `update_codes.py` مساعد التأليف كان
  ينهار بـTypeError على أي مفتاح متعدد الأحرف؛ الآن يتخطاه بتحذير.
  و`convert_fcitx5_sqlite.py` — السكربت الذي أنتج فعلًا حزمة الهان
  المشحونة في APK — وثّق أصلها الدقيق (الأمر الفعلي والجداول الثلاث
  الحقيقية: cangjielarge/boshiamy/zhengma) وصُححت قائمته النهائية
  العتيقة. وآخر سكربتَي سلاسل بلا بوابة (v1120/v1140) ختما ببوابة
  فرق المجموعات الموحدة نفسها فاكتملت البوابة على **كل** سكربتات
  السلاسل الأربعة عشر.

- 🧪 **14 اختبار وحدة جديدة (579 ناجحة، كانت 565)** — نافذة rmsToAmplitude
  بأطرافها ووسطها وتقنينها ورتابتها، الرفض الصادق للطلب الصارم غير
  القابل للتحقيق وتحديده، مرور الوضع الصارم القابل للتحقيق بالعقد
  الطبيعي (بدء وإذن)، تجاهل STANDARD لبوابة الجهاز مع بقاء حارس
  التوفر، بقاء AUTO للعقد القديم بالوسائط الافتراضية، أسبقية الخصوصية
  والإعداد على بوابة الوضع، شكل تعداد المسارات الستة وتعداد الأوضاع
  الثلاثة ورفض الأسماء الفاسدة، تصفير قناة السعة مع الجلسة، اختزال
  Fixed إلى NORMAL/COMPACT بلا شبح THUMBS، وعقد مفاتيح السلاسل في
  اللغتين.

- 🌍 **التوازي الآن 2,415 مفاتيح لكل لغة (AR/EN)** — خمسة مفاتيح جديدة
  (اسم قائمة الوضع، أسماء الأوضاع الثلاثة، توست عدم توفر الجهاز) عبر
  `add_v1250_strings.py` بالنمط المرجعي نفسه: idempotent مع بوابة
  توازٍ تسقط عند أي انفراد — والدروس السابقة محفوظة (لا مفاتيح
  summary يتيمة، فقوائم jetpref تختصر من الخيار المحدد نفسه).

</div>

<div dir="ltr">

## About the project

**DRS Smart Keyboard** is a free, open-source Android keyboard
(Apache-2.0) built with Kotlin, Jetpack Compose and Material 3 —
**Arabic-first**: a fully Arabic interface with native RTL support,
thoughtful Arabic layouts and Arabic suggestions/correction, with English
as a complete second option. Everything stays on your device: no accounts,
no tracking, no ads.

## What's new in V 1.25.0 — The Preference & Live-Pulse Round

A fresh tri-front audit verified every filter the last round left, line
by line, against the actual code: voice dictation picked its recognizer
**implicitly with no say from the user**, the mic glyph pulsed on a fixed
timer while the recognition engine **reports your voice amplitude every
frame and nobody reads it** (`onRmsChanged = Unit` since v1.23), the last
legacy placeholder in the window-mode enum (THUMBS) had no producer and
no persistence writer, and the last two string scripts still lacked the
unified parity gate. Every item below is real, tested work.

- 🎛️ **The second voice gate — «where is your speech heard?»** — until
  this release the recognizer choice was implicit: on-device when
  available, standard otherwise. Now `voice__recognizer_mode` with
  three values is a real decision consulted on **every mic press**
  through `decideVoiceInputRoute(recognizerMode, onDeviceAvailable)`:
  **Auto** (the original behavior — on-device when the ROM offers it),
  **On-device only** (never a network service — and when the ROM cannot
  honor the demand, the new honest toast answers instead of a silent
  fall to a cloud the user explicitly refused), and **Standard service**
  (for ROMs whose on-device engine disappoints). The honest order holds:
  privacy first, then the on/off switch, then the mode gate, then
  availability, then permission. The controller re-reads the pref at
  every session start, so a settings change is honored immediately;
  surfaced as a list preference in the typing settings «Voice input»
  group.

- 🎙️ **The bar breathes with your actual voice** — `onRmsChanged`,
  ignored since the built-in dictation was born, finally feeds a live
  amplitude channel on `DrsVoiceInputBus`: the pure `rmsToAmplitude`
  normalizes the dB report onto the -2..12 dB window and quantizes it
  in 2% steps so the bar does not redraw on every decimal jitter. The
  bar renders a **live five-bar wave** that rises with your voice and
  rests with your silence, painted in the smartbar element's own
  foreground color so every theme stays consistent. And honest about
  reality: many services never deliver RMS at all, so the v1.24 mic
  pulse remains the visible fallback until real samples flow — and
  every session starts from silence, never the previous session's last
  amplitude.

- 🧹 **The honest TODO/FIXME sweep** — `ImeWindowMode.Fixed.THUMBS`,
  the last legacy placeholder in the window-mode enum, is deleted from
  three places (the enum, the factory branch, the constraints class
  whose props were byte-identical to NORMAL) with no migration needed —
  no writer ever produces a THUMBS value, and a stale decoded one
  already falls back gracefully. `DictionaryManager` finally gets its
  real KDoc (lifetime, ownership, authority); the three
  DrsEmojiCompat investigation notes and LanguagePackExtension's
  «need a database lock?» question become honest audit comments; and
  DebugSummarizeUtils documents why bundle values are deliberately
  unprinted (the old ClassCastException trap).

- 🛠️ **The utils/ and scripts/ sweep** — `update_codes.py`, the layout
  authoring helper, crashed with a TypeError on any multi-character
  label; it now skips with a warning. `convert_fcitx5_sqlite.py` —
  the script that demonstrably produced the Han pack shipped inside
  the APK — documents its exact provenance (the real invocation and
  the three real tables: cangjielarge/boshiamy/zhengma) and its stale
  final display list is corrected. And the last two gateless string
  scripts (v1120/v1140) end with the same unified AR/EN set-difference
  gate, completing the gate across **all fourteen** string scripts.

- 🧪 **14 new unit tests (579 passing, was 565)** — the rmsToAmplitude
  window (floor/middle/ceiling, clamping, quantization, monotonicity);
  the honest refusal of an unhonorable strict demand and its
  determinism; an honorable strict demand flowing through the normal
  contract (start and permission); STANDARD skipping the on-device gate
  with the availability guard intact; AUTO keeping the old contract via
  default arguments; privacy and the setting leading the mode gate; the
  exact six-value route enum and three-value mode enum with junk names
  refused; the amplitude channel resetting with the session; Fixed
  shrunk to NORMAL/COMPACT with no THUMBS ghost; and the string-key
  contract in both languages.

- 🌍 **Parity is now 2,415 keys per language (AR/EN)** — five new keys
  (the mode preference label, the three mode names, and the
  on-device-unavailable toast) via the same reference-pattern
  `add_v1250_strings.py`: idempotent with a parity gate that fails on
  any orphan — and past lessons preserved (no orphan summary keys;
  jetpref list preferences derive their summary from the selected
  entry itself).

## Install

1. Download `DRS-Smart-Keyboard-v1.25.0.apk` below.
2. Install it (allow unknown sources when prompted).
3. Open Settings → System → Languages & input → enable the keyboard.
4. Pick it as your default keyboard and start typing.

Voice dictation: tap the microphone; on the first press Android will ask
for the microphone permission — grant it and speak. In password fields
and incognito mode the mic stays off by design. In Typing settings →
Voice input you can switch the whole feature off, or pick where your
speech is recognized: Auto, On-device only (no network), or the Standard
service.

Verify integrity with the SHA-256 checksums in the attached `SHA256SUMS.txt`.

## Privacy

Everything runs **locally and offline**: no accounts, no tracking, no ads,
no network except the app's own update check. Voice dictation is the one
explicit exception you trigger per press and fully control now: it
delegates to your platform's recognition service only when you tap the
mic, honors your strict on-device demand with an honest refusal instead
of a silent cloud fallback, and the keyboard itself still sends nothing
anywhere. Read the full `PRIVACY.md`.

</div>
