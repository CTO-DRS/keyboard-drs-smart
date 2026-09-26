<div align="center">

# DRS Smart Keyboard V 1.23.0

## الجولة الشاملة الثالثة والعشرون — الصوت والوجه الكامل: إملاء صوتي مدمج، إحياء FN، وجه CTRL/ALT/FN، لوحة الكاوموجي، وتنظيف الميت

**Twenty-Third Comprehensive Round — Voice and the Complete Face: Built-in Voice Dictation, the FN Revival, a Face for CTRL/ALT/FN, the Kaomoji Palette, and Dead-Code Cleanup**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.23.0 — جولة الصوت والوجه الكامل

فحص ثلاثي جديد مسح المحرك والأصول والسكربتات سطرًا سطرًا: الميكروفون كان
نهاية عمياء، وعائلة الموديفايرات كانت ناقصة وعمياء معًا، وأصل كاوموجي كامل
كان دفينًا في الـAPK مع محمّل ميت. كل بند أدناه عمل حقيقي مُختبر، لا ترقيم
أسطر.

- 🎙️ **الإملاء الصوتي المدمج — «الميكروفون يستيقظ»** — منذ اليوم الأول كان
  ضغط مفتاح الميكروفون نهاية عمياء: التبديل إلى لوحة صوت خارجية إن وُجدت
  في النظام، أو توست «لم يتم العثور على لوحة صوت». الآن **المُتعرِّف
  (SpeechRecognizer) يعمل مباشرة**: شريط استماع حي يحل محل شريط الاقتراحات
  (النص الجزئي يظهر أثناء الكلام + زر إيقاف فوري)، والنص النهائي يُكتب عند
  المؤشر بلغة المخطط النشط (ar-SA وغيرها عبر الوسم الكامل)، مع محرك التعرّف
  على الجهاز أولًا في أندرويد 12+ عندما يكون متاحًا. العقد الخصوصي صارم:
  **حقول كلمات المرور ووضع التخفي لا يرون الميكروفون أبدًا** (توست صادق)،
  إذن الميكروفون يُطلب عبر نشاط شفاف مخصص لأن خدمة الإدخال لا تستضيف
  حوارات الأذونات، وعند غياب خدمة التعرّف في النظام تبقى آلية التبديل
  الخارجي كاحتياط صادق، وجلسة الاستماع تُقتل مع إخفاء لوحة المفاتيح أو
  موت الخدمة — لا ميكروفون ساخن في الخلفية إطلاقًا.

- ⚡ **إحياء FN/FN_LOCK — آخر موديفايرين ميتين** — بعد أن حييت CTRL/ALT في
  الجولة الماضية، كشف الفحص أن FN/FN_LOCK كانا في **نفس شكل الموت تمامًا**:
  معلنان ومُعرَّفان منذ اليوم الأول بلا أي فرع معالجة — أي تخطيط يعلنهما
  يرسمهما ثم يكتب «مفتاح مجهول» عند الضغط. الإحياء بعقد v1.22.0 حرفيًا:
  حالة لمبة في بتّي 30-31 الأحرار من سجل الحالة (لا تصادم مع CTRL في 18-19
  ولا ALT في 28-29 ولا أي راية)، نابض بنفس الدورة (لمسة = لمبة واحدة،
  لمستان = قفل، ثالثة = إطلاق؛ و`FN_LOCK` يقفل مباشرة). **والعمل الحقيقي**:
  أثناء التفعيل تصبح مفاتيح الأرقام **مفاتيح F1–F10 حقيقية** تُرسل
  كأحداث أجهزة للمضيف ('1'→F1 … '9'→F9 و'0'→F10) — نفس ما يقدمه صف Fn على
  لوحات الأجهزة، وقيمته في الطرفيات (Termux) وسطوح المكتب البعيد ومحاكيات
  الكونسول. اللمبة الواحدة تُستهلك بعد الاستخدام والقفل يبقى، وأي مفتاح
  آخر يستهلكها فلا مفاجآت. وبلاطة **Fn** في كتالوج شريط التقني مع نقطة
  الحالة الحية نفسها.

- 😶 **وجه CTRL/ALT/FN أخيرًا** — مفاتيح الموديفايرات المعلنة في التخطيطات
  المخصصة كانت **صناديق فارغة**: لا تسمية مرئية ولا أيقونة ولا أي مؤشر
  حالة. الآن: تسميات مرئية مترجمة (Ctrl/Alt/Fn عبر مفاتيح `key__ctrl`
  و`key__alt` و`key__fn`)، و**سمات ثيم جديدة** (`ctrlstate`/`altstate`/
  `fnstate` بقيم off/latched/locked) تصل إلى كل مفتاح عبر خط إعادة الحساب
  نفسه الذي يحرك shift، وقواعد الثيم الأساسي تُضيء المفتاح **بالأزرق
  الفاتح أثناء اللمبة الواحدة** و**بالبرتقالي أثناء القفل** (نفس لون قفل
  الحروف الكبيرة) — وأي سمة مستخدم يمكنها استهداف الحالات الثلاث بنفس
  السمات.

- 😊 **لوحة الكاوموجي — أصل دفين يخرج للنور** — ملف `emoticons.json`
  (21 كاوموجي في ثلاثة صفوف) كان يُشحن داخل الـAPK منذ اليوم الأول بينما
  دالة تحميله كانت **`return null` مُعمّاة** — صفر مستدعين، صفر ظهور. محمّل
  حقيقي عبر نفس مسار DrsRef الذي تقرأ به كل التخطيطات، وزر «:-)» في الصف
  السفلي للوحة الوسائط يبدّل بين الإيموجي والكاوموجي (ويختفي كليًا إن فشل
  تحميل الأصل — الزر لا يكذب)، والشبكة تُرسم بنفس بلاطات الإيموجي المؤثرة،
  والضغط يُدخل الكاوموجي عند المؤشر عبر مسار MEDIA الطبيعي، وألوان النص من
  ثيم لوحة المفاتيح نفسه (لا أبيض مثبّت على خلفية فاتحة).

- 🧹 **KeyboardMode تتخلص من الميت الثلاث** — القيم الثلاث المعلنة
  `@Deprecated("TODO: remove")` منذ الإرث (EDITING=1،
  SMARTBAR_CLIPBOARD_CURSOR_ROW=8، SMARTBAR_NUMBER_ROW=9) تحقق منها الفحص
  بندًا بندًا: **لا مُنتِج** يكتبها في سجل الحالة أبدًا، السجل نفسه حي
  داخل الذاكرة فقط ولا يُستعاد من تفضيلات أو حزم، لا منتقي واجهة يعرضها،
  ولا اختبار يدافع عنها — مستهلكها الوحيد ثلاثة فروع ميتة في LayoutManager
  تعيد لوحة فارغة أو صفًا ذا آلية استُبدلت بـQuickActions منذ زمن. حُذفت
  من التعداد مع فروعها الثلاثة، والأعداد القديمة اليتيمة تسقط بأمان إلى
  CHARACTERS عبر `fromInt` الموجود أصلًا. ومعها الثابت الميت `KANA_SMALL`
  (صفر مراجع في الكود والأصول — تبديل الكانا الصغيرة راية حالة لا كود مفتاح).

- 🌐 **بوابات التوازي تصل للسكربتات القديمة السبع** — سبعة من سكربتات
  السلاسل العشرة (v170 وv190 وv1100 وv1150 وv1160 وv1190 وv1200) انتهت بلا
  بوابة التوازي المرجعية، واثنان منها (v1150/v1160) **لم تكن idempotent**
  أصلًا: إعادة تشغيلها كانت تكرر كل كتلتها في الملفين. الآن كلها تنتهي
  بنفس البوابة الصادقة (فرق مجموعات المفاتيح AR/EN، انفراد واحد = فشل
  بصوت عالٍ)، وv1150/v1160 صارت تتجاوز المفاتيح الموجودة بدل تكرارها.
  كل السكربتات العشرة تعمل الآن نظيفة على 2,406 مفاتيح لكل لغة.

- 🧪 **12 اختبار وحدة جديدًا (556 ناجحة، كانت 544)** — خرائط الأرقام إلى
  F1–F10 بالكامل (والرفض لغير الأرقام)، دورة لمبة FN بعقدها الثلاثة
  والقفل المباشر، **عقد عدم تصادم بتّي ثلاثي** (FN مع CTRL مع ALT مع
  KeyboardMode مع التخفي في سجل واحد)، قرار الإملاء الصوتي النقي بحالاته
  (التخفي أولًا ثم توفر الخدمة ثم الإذن)، زوال القيم المهجورة وسقوط
  الأعداد اليتيمة بأمان، كتالوج التقني بعائلة الموديفايرات الكاملة ونقطة
  fn الحية، وعقدا أصول الكاوموجي (بنية 21 مدخلًا وفرادة الأيقونات وقيود
  مسار الإدخال).

- 🌍 **التوازي الآن 2,406 مفاتيح لكل لغة (AR/EN)** — ثلاثة عشر مفتاحًا
  جديدًا (الاستماع والإيقاف والتوستات الخمسة للصوت، تسميات الموديفايرات
  الثلاث، بلاطة Fn باسمها ووصفها، وزر الكاوموجي) عبر
  `add_v1230_strings.py` بالنمط المرجعي نفسه: idempotent مع بوابة توازٍ
  تسقط عند أي انفراد.

</div>

<div dir="ltr">

## About the project

**DRS Smart Keyboard** is a free, open-source Android keyboard
(Apache-2.0) built with Kotlin, Jetpack Compose and Material 3 —
**Arabic-first**: a fully Arabic interface with native RTL support,
thoughtful Arabic layouts and Arabic suggestions/correction, with English
as a complete second option. Everything stays on your device: no accounts,
no tracking, no ads.

## What's new in V 1.23.0 — The Voice & Complete Face Round

A fresh tri-front audit swept the engine, the assets and the scripts,
line by line: the mic key was a dead end, the modifier family was both
incomplete and invisible, and a full kaomoji asset sat dead inside the
APK with a stubbed loader. Every item below is real, tested work.

- 🎙️ **Built-in voice dictation — «the microphone wakes up»** — since
  day one the mic key was a dead end: switch to an external voice IME if
  the ROM ships one, or an honest "voice IME not found" toast. The
  platform **SpeechRecognizer now runs directly**: a live dictation bar
  replaces the Smartbar (partial transcript while you speak + an
  immediate cancel button), the final transcript is committed at the
  cursor in the active subtype's language (full BCP-47 tag, ar-SA and
  friends), preferring the on-device recognizer on Android 12+ when
  available. The privacy contract is strict: **password fields and
  incognito mode never see the microphone** (an honest toast instead),
  the RECORD_AUDIO permission is requested through a dedicated
  translucent trampoline activity (an IME service cannot host permission
  dialogs), the external voice-IME switch remains as the honest fallback
  when no recognition service exists, and a live session dies with the
  keyboard window or the service — no hot microphone in the background,
  ever.

- ⚡ **FN/FN_LOCK revive — the last two dead modifiers** — after CTRL/ALT
  woke up last round, the audit found FN/FN_LOCK in **the exact death
  shape**: declared and defined since day one with no handling branch —
  any layout declaring them rendered the keys and then logged "unknown
  key" on press. The revival follows the v1.22.0 contract literally: a
  latch state in the last free 2-bit region of the state register
  (bits 30-31 — no collision with CTRL at 18-19, ALT at 28-29, or any
  flag), the same cycle (tap = one-shot, tap again = lock, third tap =
  release; `FN_LOCK` jumps straight to lock). **And the real work**:
  while armed, the digit keys send **real F1–F10 hardware events** to
  the host ('1'→F1 … '9'→F9, '0'→F10) — exactly what a physical Fn row
  delivers, and genuinely useful in terminals (Termux), remote-desktop
  clients and console emulators. A one-shot latch releases after use,
  locks persist, and any other key consumes it — no surprises. Plus an
  **Fn tile** in the tech-toolbar catalogue with the same live status
  dot.

- 😶 **CTRL/ALT/FN finally get a face** — modifier keys declared in
  custom layouts rendered as **blank boxes**: no visible label, no icon,
  no state feedback. Now: localized visible labels (Ctrl/Alt/Fn via the
  `key__ctrl`/`key__alt`/`key__fn` keys), and **new theme attributes**
  (`ctrlstate`/`altstate`/`fnstate`, values off/latched/locked) reaching
  every key through the same recompute pipeline that drives shift. The
  base stylesheet lights an armed latch **light blue** and a locked
  latch **orange** (the same color as caps lock), and any user theme can
  target all three states with the same attributes.

- 😊 **The kaomoji palette — a buried asset surfaces** —
  `emoticons.json` (21 kaomoji in three rows) shipped inside the APK
  from day one while its loader function was a **hardcoded
  `return null`** — zero callers, zero visibility. A real loader through
  the same DrsRef pipeline every layout uses, plus a «:-)» toggle in the
  media palette's bottom row that swaps emoji for kaomoji (and hides
  itself entirely if the asset ever fails to parse — the button never
  lies). The grid renders through the same interactive emoji tiles, a
  tap commits the kaomoji at the cursor through the normal MEDIA path,
  and the text color comes from the keyboard theme itself (no hardcoded
  white-on-white).

- 🧹 **KeyboardMode sheds its three corpses** — the three values marked
  `@Deprecated("TODO: remove")` since the legacy era (EDITING=1,
  SMARTBAR_CLIPBOARD_CURSOR_ROW=8, SMARTBAR_NUMBER_ROW=9) were verified
  item by item: **no producer** ever wrote them into the mode register,
  the register is runtime-only and never restored from prefs or bundles,
  no UI selector lists them, no test defends them — and their only
  consumers were three dead LayoutManager branches returning an empty
  keyboard or a smartbar row whose mechanism was replaced by
  QuickActions long ago. Deleted from the enum along with their branches;
  stale orphan ints fall back to CHARACTERS through the existing
  `fromInt`. Along with them, the zero-reference `KANA_SMALL` constant
  (the kana-small toggle is a state flag, not a key code).

- 🌐 **Parity gates reach the seven old string scripts** — seven of the
  ten string scripts (v170, v190, v1100, v1150, v1160, v1190, v1200)
  ended without the reference parity gate, and two of them (v1150/v1160)
  were **not even idempotent**: re-running them duplicated their whole
  block into both files. All seven now end with the same honest gate
  (AR/EN key-set difference; a single orphan fails loudly), and
  v1150/v1160 skip existing keys instead of duplicating them. All ten
  scripts now run clean on 2,406 keys per language.

- 🧪 **12 new unit tests (556 passing, was 544)** — the full digit→F-key
  map (plus refusing non-digits), the FN latch cycle with its three
  directions and direct lock, a **triple no-bit-collision contract**
  (FN + CTRL + ALT + KeyboardMode + incognito coexisting in one
  register), the pure voice route decision (sensitivity first, then
  service availability, then permission), the deprecated values' absence
  and the safe fallback of stale ints, the tech-toolbar catalogue with
  the complete modifier family and the live fn dot, and two kaomoji
  asset contracts (the 21-entry structure, unique icons, and the
  commit-path constraints).

- 🌍 **Parity is now 2,406 keys per language (AR/EN)** — thirteen new
  keys (listening hint, cancel label, the five voice toasts/errors, the
  three modifier labels, the Fn tile name + description, and the kaomoji
  toggle) via the same reference-pattern `add_v1230_strings.py`:
  idempotent with a parity gate that fails on any orphan.

## Install

1. Download `DRS-Smart-Keyboard-v1.23.0.apk` below.
2. Install it (allow unknown sources when prompted).
3. Open Settings → System → Languages & input → enable the keyboard.
4. Pick it as your default keyboard and start typing.

Voice dictation: tap the microphone; on the first press Android will ask
for the microphone permission — grant it and speak. In password fields
and incognito mode the mic stays off by design.

Verify integrity with the SHA-256 checksums in the attached `SHA256SUMS.txt`.

## Privacy

Everything runs **locally and offline**: no accounts, no tracking, no ads,
no network except the app's own update check. Voice dictation is the one
explicit exception you trigger per press: it delegates to your platform's
recognition service and only when you tap the mic — the keyboard itself
still sends nothing anywhere. Read the full `PRIVACY.md`.

</div>
