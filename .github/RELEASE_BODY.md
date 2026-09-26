<div align="center">

# DRS Smart Keyboard V 1.26.0

## الجولة الشاملة السادسة والعشرون — الوجه الصادق: الشارة تكذب أقل، والإذن المنكوف له باب خارج

**Twenty-Sixth Comprehensive Round — The Honest Face: the Badge Stops Lying and the Denied Permission Gets a Way Out**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.26.0 — جولة الوجه الصادق

فحص ثلاثي جديد تحقق من مرشحات الجولة الماضية سطرًا سطرًا بالكود الفعلي:
شريط الاستماع كان يظهر لأوضاع المتعرّف الثلاثة **بذات الوجه تمامًا** —
فلا فرق بصري بين جلسة محلية خالصة وجلسة سحابية — بينما الجلسة نفسها
تعرف أين يعمل متعرّفها فعلًا. ورفض إذن الميكروفون نهائيًا («لا تسأل
مجدًا») كان ينتهي بـ**توست ميت لا طريق بعده**. وشريط التقني المتقدم كان
بلا ميكروفون رغم كامل جاهزية المسار. وأقدم TODO في طبقة الإضافات كان
يمحو ويعيد فك ضغط كاش كل إضافة في **كل تحميل** حتى لو لم يتغير مصدرها.
وTODO تطبيع رقم الهاتف في مقترحات الحافظة كان عالقًا منذ وراثة الكود.
كل بند أدناه عمل حقيقي مُختبر، لا ترقيم أسطر.

- 🛰️ **شارة «بدون شبكة» الحية — الجلسة تصرّح بمكان متعرّفها** —
  `VoiceUiState.Listening` يحمل الآن حقل `onDevice` الذي يُحسم **مرة
  واحدة في كل جلسة** في `start()` بجانب إنشاء المتعرّف نفسه — عبر
  الدالة النقية `usesOnDeviceRecognizer(mode, onDevicePossible)` — ثم
  يُحمل على كل حالة استماع تنشرها الجلسة (البداية، والنص الجزئي). فتظهر
  في شريط الاستماع رقاقة صغيرة «بدون شبكة» بحدود من لون مقدمة الشريط
  نفسه فتلتزمها كل الثيمات. **والصدق في التفاصيل**: جلسة AUTO التي نالت
  محرك الجهاز تُظهر الشارة تمامًا كجلسة «على الجهاز فقط» — لأن الحقيقة
  واحدة — بينما جلسة «الخدمة القياسية» لا تظهرها أبدًا، والجلسة الصارمة
  على نظام لا يستطيع honoring الطلب لا تصل للاستماع أصلًا (مسار التوست
  الصادق من v1.25) فلا وعد كاذب قبل الجلسة ولا بعدها.

- 🚪 **باب خارج الرفض النهائي للإذن — لا حبس في توست ميت** — الدالة
  النقية `nextPermissionAction(canShowRationale)` تقسم رفض إذن
  الميكروفون إلى عالمين: **رفض عادي** (النظام ما زال قادرًا على عرض
  حواره) يبقى بتوست الشرح الخفيف كما هو، و**رفض نهائي**
  (`shouldShowRequestPermissionRationale == false` — «لا تسأل مجددًا»،
  وعلى أندرويد 11+ يرد النظام على كل طلب لاحق فورًا دون أي حوار) يفتح
  صفحة إعدادات التطبيق في النظام مباشرة (`ACTION_APPLICATION_DETAILS_SETTINGS`)
  مع توست طويل يشرح الخطوة المطلوبة. المستخدم الذي نكفى يده مرة يملك
  طريقًا حقيقيًا للعودة بدل حلقة توست-انتهى.

- 🎙️ **الميكروفون يركب شريط التقني** — مفتاح `mic` جديد في كتالوج
  `DrsTechToolbarKeys` (بالنمط اللاتيني الموحد: Tab/Esc/Ctrl/Alt/Fn ثم
  Mic) يرسل `KeyCode.VOICE_INPUT` عبر مسار الإرسال نفسه، فتصل ضغطة
  واحدة — من المستوى المتقدم أو المزدوج — إلى **نفس مسار الإملاء
  المبوَّب بالخصوصية** الذي يمر به مفتاح الميكروفون في لوحة الحروف
  (الحساس ثم الإعداد ثم الوضع ثم الإذن). والمفتاح لحظي بلا نقطة حالة —
  لا يكذب بأنه مفتاح تبديل.

- 💾 **الكاش الصادق للإضافات — أقدم TODO في الطبقة يموت بصمت وقوة** —
  `Extension.load` كان يمحو مجلد الكاش ويعيد فك ضغط المصدر في **كل
  تحميل** (كل تبديل ثيم، كل تفعيل حزمة لغة، كل استخدام) حتى لو كان
  المصدر هو نفسه بايتًا بايتًا. الآن يُحسب **بصمة محتوى المصدر**: SHA-256
  لبايتات أرشيف الـflex، أو digest مفروز «مسار:حجم» لشجرة أصول الـAPK —
  وتُكتب في ملف شقيق `<ext-id>.drs-fp` (خارج مجلد الكاش كي لا تراه
  مكوّنات الإضافة أبدًا). مطابقة البصمة = إعادة ربط الكاش فورًا بلا
  محو ولا فك ضغط، مع استدعاء `onAfterLoad` (القاعدة الوحيدة المتجاوزة —
  فتح قاعدة بيانات الهان للقراءة — يعيد ربط المقبض بأمان وهو قابل
  لإعادة الاستدعاء بطبيعته). والإجباري (`force`) والبصمة الغامضة
  (كاش قديم بلا وسم أو مصدر غير قابل للقياس) يبقيان السلوك المحافظ
  القديم — الكاش لا يوافق على الخدمة إلا وهو واثق.

- ☎️ **تطبيع رقم الهاتف يصير عقدًا مختبرًا** — تعليق TODO في
  `ClipboardSuggestionProvider` («عدّل الرجيكس كي لا ننزع الأقواس يدويًا»)
  كان مستحيل الإنجاز كما هو: `java.util.regex` بلا `\K`، والقوس الذي
  يلتف حول الرقم كله هو نفسه قد يكون زوج منطقة داخليًا. الحل الصادق:
  النزع يصير `NetworkUtils.normalizePhoneNumberMatch` النقية — الزوج
  الكامل يُنزع `(0541234567)` ← `0541234567`، والقوس الافتتاحي المبتور
  (بلا إغلاق في النص كله) يُفك، والمتوازن يبقى **كما كتبه المستخدم**
  `(054) 123 4567` — مع `isParenBalanced` النقية وفحص عمق بسيط.

- 🧪 **19 اختبار وحدة جديدة (598 ناجحة، كانت 579)** — حقيقة الشارة بكل
  أوضاع المتعرّف الثلاثة وتحمّل الحقل الافتراضي الصادق؛ الانقسام
  الرفضي بعالميه وعقد الأفعال الاثنين؛ وجود مفتاح mic بكوده ونوعه وفرادة
  المعرفات؛ قرار إعادة استخدام الكاش بحالاته الخمس (مطابقة/اختلاف/كاش
  بلا وسم/مصدر غامض/إجبار)؛ ثبات البصمة عبر ترتيب الإدراج وحساسيتها
  لأي تغيير مسار أو حجم؛ بادئة `tree:` وثبات الشجرة الفارغة؛ عقد
  الوسم الشقيق `.drs-fp`؛ نزع الزوج الكامل والقوس المبتور وبقاء
  المتوازن ومرور المدخلات الصغيرة؛ وتوازي المفتاحين الجديدين في
  اللغتين.

- 🌍 **التوازي 2,417 مفتاحًا لكل لغة (AR/EN)** — مفتاحان جديدان
  (`voice__on_device_badge` و`voice__permission_permanent`) عبر
  `add_v1260_strings.py` بالنمط المرجعي نفسه: idempotent ببوابة فرق
  المجموعات التي تفشل بصوت عالٍ عند أي انفراد.

</div>

<div dir="ltr">

## New in V 1.26.0 — The Honest Face

- 🛰️ **The live «On device» badge** — the listening bar now shows the
  session's privacy truth: a small chip (`On device` / «بدون شبكة»)
  driven by `VoiceUiState.Listening.onDevice`, decided once per session
  by the pure `usesOnDeviceRecognizer` and carried on every listening
  state. AUTO sessions that got the local engine show it exactly like
  strict ON_DEVICE_ONLY ones; STANDARD never does; an unhonorable
  strict demand never reaches listening at all.

- 🚪 **A way out of the permanently denied mic permission** — the pure
  `nextPermissionAction(canShowRationale)` splits the denial: a plain
  denial keeps the light explanation toast, while a permanent one opens
  the app's system settings page (with a toast explaining the step) —
  no more dead-end toast loops.

- 🎙️ **A mic key on the technical strip** — `mic` joins the tech
  toolbar catalogue (Tab/Esc/Ctrl/Alt/Fn → Mic), sending
  `KeyCode.VOICE_INPUT` through the same fully-gated dictation route as
  the letters-board mic key. Momentary key, no toggle dot — it does not
  lie about being a switch.

- 💾 **The honest extension cache** — the oldest TODO in the extensions
  layer dies: `Extension.load` now computes a source-content
  fingerprint (SHA-256 for flex archives, sorted path:size digest for
  APK asset trees) stored in a sibling `<ext-id>.drs-fp` marker and
  reuses a matching cache instead of the unconditional
  delete+re-extract on every load. Forced reloads and unknown
  fingerprints keep the conservative rebuild; `onAfterLoad` re-attaches
  runtime handles (the Han database) on reuse.

- ☎️ **Phone-number normalization as a tested contract** — the ancient
  inline paren-strip TODO becomes the pure
  `NetworkUtils.normalizePhoneNumberMatch`: a complete outer pair is
  removed, a truncated leading paren is unwrapped, and balanced parens
  stay exactly as typed.

- 🧪 **19 new unit tests (598 passing, was 579)** covering every pure
  contract above. 🌍 **Parity is now 2,417 keys per language (AR/EN)**
  via the reference-pattern `add_v1260_strings.py` (idempotent, with
  the hard parity gate).

## Install

1. Download `DRS-Smart-Keyboard-v1.26.0.apk` below.
2. Install it (allow unknown sources when prompted).
3. Open Settings → System → Languages & input → enable the keyboard.
4. Pick it as your default keyboard and start typing.

Voice dictation: tap the microphone; on the first press Android will ask
for the microphone permission — grant it and speak. If the permission
was permanently denied before, the denied press now opens the app's
system settings so you can re-enable it. While listening, the bar shows
the live transcript, your actual voice wave, and the «On device» chip
whenever the session runs on the local recognizer.

Verify integrity with the SHA-256 checksums in the attached `SHA256SUMS.txt`.

## Privacy

Everything runs **locally and offline**: no accounts, no tracking, no ads,
no network except the app's own update check. Voice dictation is the one
explicit exception you trigger per press and fully control: it delegates
to your platform's recognition service only when you tap the mic, and the
bar now shows you — session by session — when your speech stays on the
device. Read the full `PRIVACY.md`.

</div>
