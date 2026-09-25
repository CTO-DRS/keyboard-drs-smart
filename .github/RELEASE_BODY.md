<div align="center">

# DRS Smart Keyboard V 1.14.0

## الجولة الشاملة الرابعة عشرة — قائمة الإعدادات الشاملة في التطبيق: كل ما بنيناه الآن بين يديك

**Fourteenth Comprehensive Round — The Comprehensive In-App Settings List: Everything We Built, Now in Your Hands**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.14.0 — قائمة الإعدادات الشاملة

- ⚙️ **ست مجموعات كاملة في شاشة الحافظة** (طلب المستخدم الصريح: «الان قم
  بإضافة قائمة الاعدادات الشامله في التتطبيق لكل ماقمنا به اليوم وامس»):
  شاشة إعدادات الحافظة أُعيد تنظيمها بالكامل لتشمل كل ما بُني في الجولات
  الأخيرة: **نافذة التعديل المنبثقة، المحرر الذكي، البحث والنتائج، كشف
  الأسطر البرمجية، تنظيم السجل، والتصدير** — أحد عشر مفتاحًا جديدًا إلى
  جانب مفاتيح v1.12.0، كلها حقيقية وموصّلة بالسلوك لا زخارف.
- 🪟 **نافذة التعديل المنبثقة صارت خيارًا**: اختر مكان فتح «تعديل النص» —
  **نافذة عائمة فوق الشاشة** (سلوك v1.11.0 الافتراضي) **أو داخل لوحة
  الحافظة** — ثم اختر **حجم النافذة** (مدمجة / عادية / تملأ الشاشة
  تقريبًا) و**مقدار تعتيم الخلفية** (بلا / خفيف / متوسط / قوي). كل ما
  كان ثابتًا في v1.11.0 صار بين يديك.
- 🎯 **القفز المباشر إلى السطر صار سلوكًا قابلًا للتشكيل**: فعّل أو عطّل
  القفز عند الضغط على بطاقة النتيجة، واختر محاذاة القفز — **تمركز السطر
  في منتصف نافذة العرض** (سلوك v1.13.0 الافتراضي) أو **تثبيت رأسه تحت
  الحافة العليا** — وتحكم في **تتبع قائمة البطاقات للبطاقة النشطة**.
- 💻 **كشف الأسطر البرمجية بتفاصيله**: شارة الكود (اللغة وعدد الأسطر
  البرمجية) والإبدال التلقائي إلى الخط أحادي المسافة عند فتح كود — لكل
  تفصيلة مفتاحها الخاص، وكلاهما يتعطل تلقائيًا مع تعطيل الكشف.
- 🗂️ **تنظيم السجل بين يديك**: الأقسام التقويمية (مثبت/اليوم/أمس/هذا
  الأسبوع/هذا الشهر/أقدم) قابلة للطي إلى شبكة واحدة مسطحة، وشارات
  الفئات الذكية (رابط/بريد/هاتف/كود) قابلة للإخفاء — والترتيب المحفوظ
  (الأحدث/الأقدم/الأطول/الأقصر) كما هو.
- ⚠️ **تحذير النص الضخم صار إعدادًا**: تنبيه التباطؤ عند الأحجام الكبيرة
  يمكن إظهاره أو إخفاؤه في المحررين معًا.
- 🛡️ **عقود نقية مُختبرة**: توجيه سطح التعديل باحترام اختيار المستخدم
  (النافذة ترفض الوسائط والنص الفارغ دائمًا)، وهندسة النافذة (كسور
  داخل (0,1] فلا تخرج البطاقة من الشاشة أبدًا)، ومحاذاة القفز
  (تمركز/تثبيت بقيدَي المدى) — 341 اختبار وحدة ناجحًا (كانت 323).
- 🌍 **توازي كامل AR/EN**: 34 مفتاح سلاسل جديدًا — 2262 مفتاحًا لكل
  لغة، وكل الإعدادات الافتراضية تحافظ على سلوك الإصدارات السابقة حرفيًا.

## ماذا يعني هذا عمليًا؟

افتح التطبيق ← الإعدادات ← الحافظة: ستجد نافذة التعديل المنبثقة تنتظر
اختيارك — اجعلها مدمجة بتعتيم خفيف إن أردت رؤية تطبيقك خلفها، أو تملأ
الشاشة بتعتيم قوي لجلسة تحرير مركزة. عطّل القفز إن كنت تفضل أن يبقى
التمرير بيدك، أو اجعله يثبت رأس السطر بدل تمركزه. أخفِ شارات الفئات إن
أزعجتك، واطوِ الأقسام التقويمية إلى قائمة واحدة. كل قرار هندسي اتخذناه
نيابةًك في الجولات الماضية صار اليوم قرارًا تتفق عليه أنت مع لوحة المفاتيح.

<div dir="ltr">

## What's new in V 1.14.0 — The Comprehensive Settings List

- ⚙️ **Six full groups in the clipboard settings**: the settings screen
  is reorganized to cover everything the recent rounds built — the edit
  popup window, the smart editor, search & results, code-line detection,
  history organization, and export. Eleven new keys, all genuinely wired
  to behavior.
- 🪟 **The popup edit window becomes a choice**: pick where «Edit text»
  opens — the floating window above the screen (the v1.11.0 default) or
  inside the clipboard panel — then pick the window size (compact /
  normal / nearly full screen) and the background dimming (none / light
  / normal / strong). Everything hardcoded in v1.11.0 is now yours.
- 🎯 **The direct line jump becomes a configurable behavior**: toggle
  the jump on card tap, choose the alignment — center the row in the
  viewport (the v1.13.0 default) or pin its head under the top edge —
  and control the cards list following the active card.
- 💻 **Code detection in detail**: the code badge (language + code-line
  share) and the automatic monospace switch each get their own switch,
  both disabled automatically when detection is off.
- 🗂️ **History organization in your hands**: collapse the calendar
  sections (pinned/today/yesterday/this week/this month/older) into one
  flat grid, hide the smart category badges (link/email/phone/code) —
  the persisted sort order stays as-is.
- ⚠️ **The huge-text warning becomes a setting**: show or hide the
  slowdown warning in both editors.
- 🛡️ **Pure, tested contracts**: the edit-surface routing honors the
  user's choice (the window always refuses media and null text), the
  window geometry stays inside (0, 1] so the card never leaves the
  screen, and the jump alignment clamps to the real scroll range —
  341 unit tests passing (up from 323).
- 🌍 **Full AR/EN parity**: 34 new string keys — 2262 keys per language,
  and every default preserves the previous releases' behavior exactly.

</div>

## التحميل

| الأصل | الوصف |
|---|---|
| `DRS-Smart-Keyboard-v1.14.0.apk` | الحزمة الكاملة الموقّعة (versionCode 26) |
| `DRS-Smart-Keyboard-v1.14.0.aab` | حزمة أندرويد للمتاجر |
| `SHA256SUMS.txt` | بصمات SHA-256 لكل الأصول |

</div>
