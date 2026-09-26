<div align="center">

# DRS Smart Keyboard V 1.24.0

## الجولة الشاملة الرابعة والعشرون — الإذن والامتداد: الميكروفون في قبضة المستخدم وصف Fn الأخير

**Twenty-Fourth Comprehensive Round — Permission and Extension: the Microphone in the User's Hands and the Final Fn Row**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.24.0 — جولة الإذن والامتداد

فحص ثلاثي جديد تحقق من كل مرشح تركته الجولة الماضية سطرًا سطرًا بالكود
الفعلي: الإملاء الصوتي استيقظ الجولة الماضية لكن **بلا أي مفتاح إعداد
يصمت به**، وصف Fn على الشاشة توقف عند F10 بينما '-' و'=' يجلسان بعد '0'
تمامًا كما يجلس F11/F12 بعد F10 على لوحات الأجهزة، وكاشف التنظيف كان
يستهلك اللمبة عند قفزات الصفحات فيجعل الصف الممتد مستحيل الوصول، وأقدم
ملاحظات FIXME في مزوّد الهان كانت تنتظر حلاً صادقًا. كل بند أدناه عمل
حقيقي مُختبر، لا ترقيم أسطر.

- ⚙️ **بوابة الإملاء الصوتي — «الميكروفون في قبضة المستخدم»** — v1.23.0
  أيقظ الميكروفون بلا أي مفتاح إعداد صريح يواجهه المستخدم. الآن
  `voice__enabled` (افتراضي: مفعّل) هو **البوابة الحقيقية** التي
  يفحصها مفتاح الميكروفون في كل ضغطة عبر قرار
  `decideVoiceInputRoute(userEnabled)`: الإيقاف من إعدادات الكتابة
  يجعل الضغطة التالية تجيب بتوست صادق «الإملاء الصوتي معطل من إعدادات
  الكتابة» بدل الاستماع بصمت أو التراجع بصمت للوحة الصوت الخارجية.
  والخصوصية تحتفظ بأسبقيتها: حقول كلمات المرور ووضع التخفي يُرفضان
  **قبل** وصول القرار إلى الإعداد أصلًا. والمفتاح ظاهر كمجموعة
  «الإدخال الصوتي» في شاشة إعدادات الكتابة مع ملخص خصوصية كامل.

- ⌨️ **صف Fn يبلغ نهايته الطبيعية: F11 وF12** — الخريطة الرقمية
  ('1'→F1 … '0'→F10) التي بناها الإحياء تكتمل الآن بمفاتيح '-'(45)→F11
  و'='(61)→F12 — انعكاس صادق للوحة الأجهزة حيث يجلس '-' و'=' بعد '0'
  على الصف الأعلى تمامًا كما يجلس F11/F12 بعد F10. وكلاهما **مضمون
  الوصول بعقود أصول**: '-' على الصفحة الرقمية وصفحة الرموز، و'=' على
  صفحة الرموز الثانية.

- 🔁 **لمبة FN تنجو من قفزات الصفحات** — كاشف التنظيف في v1.23 كان
  يستهلك اللمبة المسلحة عند **كل** مفتاح غير موديفاير، ومنهم مفاتيح
  تبديل الصفحات (VIEW_*/IME_UI_MODE_*) — ما كان سيجعل F12 خلف '=' في
  صفحة الرموز الثانية **مستحيل الوصول عمليًا**: سلّح Fn → قفز للصفحة →
  اللمبة ميتة. الدالة النقية `fnSurvivesKey` تملك مجموعة المفاتيح
  الحافظة، فيصبح «سلّح ثم قفز ثم اضغط» طريقًا حقيقيًا مُختبرًا، بينما
  كل مفتاح استهلاك حقيقي (حرف، رقم، '-'/'=' نفسها، مسافة) يُطلق
  اللمبة الواحدة كما هو.

- 🧹 **أقدم FIXME في مزوّد الهان يُحسم صدقًا** — الملاحظة «observeForever
  لا تُستدعى إلا على الخيط الرئيسي» كانت معلّقة ميتًا منذ الإرث: تبيّن
  أن فهرس الحزم هو **StateFlow** أصلًا (ExtensionIndex : StateFlow)،
  فجامع بسيط في نطاق المزوّد نفسه يمنح تحديثًا حيًا لتثبيت/إزالة حزمة
  هان بلا أي قيد خيوط — والمراقب يُربط مرة واحدة بأمان. وملاحظتا
  «تخطَّ فحص نوع حزمة اللغة» استُبدلتا بملاحظات تدقيق صادقة: لا واجهة
  فرعية لحزم الهان موجودة أصلًا يُمكن فحصها، وعقد الأصول هو نظام الأنواع
  هنا — والحارس الكسول في suggest يبقى حزام أمان مجانيًا.

- 🎙️ **شريط الاستماع يتنفس** — رمز الميكروفون في شريط الإملاء ينبض
  نبضة هادئة (دورة مقياس 620 مللي ثانية بمنحنى التوكيد القياسي) أثناء
  الاستماع الفعلي فقط، ويسكن بحجمه الطبيعي في الخمول والخطأ — إشارة
  بصرية صادقة أن التعرّف يسمعك الآن، بلا أي لمس لحالة التدفق نفسها.

- 🧪 **9 اختبارات وحدة جديدة (565 ناجحة، كانت 556)** — خريطة F11/F12
  عبر '-' و'=' مع ثبات خريطة الأرقام ورفض ما خارج الصف، عقد بقاء اللمبة
  عبر القفزات الأربعة عشر (صفحات وأنماط) مع استهلاك مفاتيح الاستهلاك
  الحقيقية، بوابة الإعداد بالترتيب الصادق (الخصوصية أولًا ثم الإعداد
  ثم التوفر ثم الإذن) مع سلوك v1.23 سليمًا عند التفعيل وقيمة افتراضية
  تحفظ كل مواضع الاستدعاء القديمة، وعقدا أصول يثبتان أن '-' فعلًا على
  الصفحة الرقمية (مع سلامة صف الأرقام كاملًا) و'=' على الرموز الثانية.

- 🌍 **التوازي الآن 2,410 مفاتيح لكل لغة (AR/EN)** — أربعة مفاتيح جديدة
  (عنوان مجموعة الإدخال الصوتي، مفتاح التبديل باسمه وملخصه الخصوصي،
  توست الإيقاف من الإعدادات) عبر `add_v1240_strings.py` بالنمط المرجعي
  نفسه: idempotent مع بوابة توازٍ تسقط عند أي انفراد.

</div>

<div dir="ltr">

## About the project

**DRS Smart Keyboard** is a free, open-source Android keyboard
(Apache-2.0) built with Kotlin, Jetpack Compose and Material 3 —
**Arabic-first**: a fully Arabic interface with native RTL support,
thoughtful Arabic layouts and Arabic suggestions/correction, with English
as a complete second option. Everything stays on your device: no accounts,
no tracking, no ads.

## What's new in V 1.24.0 — The Permission & Extension Round

A fresh tri-front audit verified every filter the last round left,
line by line, against the actual code: voice dictation woke up last
round but shipped with **no explicit settings switch to silence it**,
the on-screen Fn row stopped at F10 while '-'/'=' sit right after '0'
exactly like F11/F12 sit after F10 on physical keyboards, the latch
cleanup consumed the armed FN on page hops — making the extended row
unreachable in practice — and the oldest FIXMEs in the Han provider
were still waiting for an honest resolution. Every item below is real,
tested work.

- ⚙️ **Voice dictation settings gate — «the microphone in the user's
  hands»** — v1.23.0 woke the microphone with no explicit user-facing
  switch. Now `voice__enabled` (default: on) is **the real gate** the
  mic key consults on every press through
  `decideVoiceInputRoute(userEnabled)`: switching it off in the typing
  settings makes the very next press answer with an honest toast
  instead of silently listening or silently falling back to the
  external voice IME. Privacy keeps its lead: password fields and
  incognito are refused **before** the decision even reaches the
  setting. Surfaced as a «Voice input» group in the typing settings
  with a full privacy summary.

- ⌨️ **The Fn row reaches its natural end: F11 and F12** — the
  digit→F1–F10 map completes with '-'(45) → F11 and '='(61) → F12, an
  honest mirror of physical keyboards where '-'/'=' sit right after
  '0' on the top row just like F11/F12 sit right after F10. Both are
  **pinned reachable by asset contracts**: '-' on the numeric and
  symbols pages, '=' on the symbols2 page.

- 🔁 **The FN latch survives page hops** — the v1.23 cleanup consumed
  the armed latch on EVERY non-modifier key, including the VIEW_*/
  IME_UI_MODE_* page switches, which would have made F12 (behind '='
  on symbols2) **practically unreachable**: arm Fn → hop → dead latch.
  The pure `fnSurvivesKey` owns the preserving set, so arm → hop →
  press is now a real, tested path, while every true consuming key
  (letter, digit, '-'/'=' themselves, space) still releases the
  one-shot latch.

- 🧹 **The Han provider's oldest FIXMEs, resolved honestly** — the
  commented-out «observeForever only callable on the main thread» init
  turned out to be waiting on the wrong API: the pack index is a
  **StateFlow** (ExtensionIndex), so a plain collector in the
  provider's own scope delivers live install/remove refresh with no
  thread constraint, wired exactly once. The two «skip checking
  language pack type» FIXMEs became honest audit notes: no
  Han-specific extension subtype exists to check against — the asset
  contract is the type system here — and the lazy guard in suggest()
  stays as a free belt-and-braces.

- 🎙️ **The dictation bar breathes** — the mic glyph in the dictation
  bar pulses calmly (a 620 ms scale loop on the standard emphasis
  curve) while the recognizer is actually listening, and rests at its
  natural size when idle or errored — an honest visual signal that the
  recognizer hears you now, touching nothing outside its own
  graphicsLayer.

- 🧪 **9 new unit tests (565 passing, was 556)** — the '-'/'=' → F11/F12
  map with the digit map pinned and the row refusing everything else;
  the latch-survival contract across all fourteen page/mode switches
  with the true consumers still consuming; the settings gate in its
  honest order (privacy first, then the setting, then availability,
  then permission) with the v1.23 behavior intact when enabled and a
  default argument keeping every old call site honest; and two asset
  contracts proving '-' really ships on the numeric page (digit row
  intact) and '=' on symbols2.

- 🌍 **Parity is now 2,410 keys per language (AR/EN)** — four new keys
  (the voice-input group title, the switch label + privacy summary,
  and the disabled-by-setting toast) via the same reference-pattern
  `add_v1240_strings.py`: idempotent with a parity gate that fails on
  any orphan.

## Install

1. Download `DRS-Smart-Keyboard-v1.24.0.apk` below.
2. Install it (allow unknown sources when prompted).
3. Open Settings → System → Languages & input → enable the keyboard.
4. Pick it as your default keyboard and start typing.

Voice dictation: tap the microphone; on the first press Android will ask
for the microphone permission — grant it and speak. In password fields
and incognito mode the mic stays off by design, and you can switch the
whole feature off in Typing settings → Voice input.

Verify integrity with the SHA-256 checksums in the attached `SHA256SUMS.txt`.

## Privacy

Everything runs **locally and offline**: no accounts, no tracking, no ads,
no network except the app's own update check. Voice dictation is the one
explicit exception you trigger per press and can now switch off entirely:
it delegates to your platform's recognition service and only when you tap
the mic — the keyboard itself still sends nothing anywhere. Read the full
`PRIVACY.md`.

</div>
