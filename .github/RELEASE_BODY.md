<div align="center">

# DRS Smart Keyboard V 1.5.0

## الجولة الشاملة الخامسة — إصلاحات جذعية وثماني عمليات نصية وثلاث أدوات كتالوج وأسماء بلاطات حقيقية

**Fifth Comprehensive Round — Root Fixes, Eight Text Ops, Three Catalog Tools, Real Tile Names**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.5.0 — كل ميزة مربوطة بالمحرك فعليًا

- 🐛 **إصلاحان جذعيان لقنوات كانت صامتة**:
  - **أداة «اللغة» في الكتالوج** كانت منذ V 1.0.8 تشير إلى رمز
    `IME_SUBTYPE_PICKER` الذي **لا معالج له** — الضغط عليه يُسجل «مفتاح
    مجهول» ولا يفعل شيئًا. صارت تشير إلى `SHOW_SUBTYPE_PICKER` فتفتح
    منتقي اللغات داخل لوحة المفاتيح فعلًا
  - **مفتاحا Tab وEsc في شريط الأدوات التقني** كانا يسقطان في فرع المفاتيح
    المجهولة (سلوك صامت بلا أثر). Tab يُدخل الآن محرف جدولة حقيقيًا في
    الحقل، وEsc يخفي لوحة المفاتيح عبر مسار `IME_HIDE_UI` نفسه
- 🏷️ **أسماء حقيقية لبلاطات «الأكثر استخدامًا»**: أي مستخدم كثيف لأدوات
  النصوص كان يرى بلاطات بعنوان العنصر الفاشل العام `invalid_fatal`؛ صارت
  كل أدوات النصوص الـ34 تعرض عنوانها ووصفها الحقيقيين من لوحة الأدوات
  نفسها، وأُضيفت الأسماء والتلميحات لستة رموز كانت مهملة كذلك (بداية/نهاية
  النص والسطر، تحديد النص، إظهار الشريط الذكي)
- ✍️ **ثماني عمليات نصية جديدة** (تصبح **34 عملية**)، كلها تحويل نقي:
  - **عكس حالة الأحرف**: الصغير يصبح كبيرًا والعكس — والعربية بلا حالة
    تبقى كما هي
  - **ترقيم عربي**: `,` و`;` و`?` اللاتينية إلى نظيراتها ، ؛ ؟ — تحويل
    محرفي نقي لا مساس لغيره
  - **إزالة الأحرف الصفرية**: يحذف ZWSP وZWJ ومحارف الاتجاه وBOM التي
    تفسد البحث والنسخ — ويحافظ **عمدًا** على نصف المسافة (ZWNJ) فهو حرف
    عربي حقيقي
  - **تبويبات إلى مسافات**: كل محرف جدولة يصبح أربع مسافات حقيقية
  - **ترتيب الأسطر بالطول**: من الأقصر إلى الأطول ثباتًا، والمتساوي يحفظ
    ترتيبه، والسطر الختامي محفوظ
  - **إزالة الكلمات المكررة**: أول ظهور يبقى، والمسافة السابقة للكلمة
    المحذوفة تُحذف معها فلا بقاء لمسافات معلّقة، والمقارنة حرفية فلا
    تُمس الاختصارات والأعلام
  - **إحاطة بأقواس**: (…) حول التحديد أو الحقل كله
  - **جملة في كل سطر**: فصل بعد نهايات الجمل (. ! ? ؟ …) متى تبعها محرف
    غير فراغ — لا أسطر مزدوجة ولا مساس بالفواصل الموجودة
  - وتوسعة **تحويل الأرقام إلى العربية** لتشمل الأرقام الفارسية/الأردية
    (۰-۹) فضلاً عن الغربية (0-9)
- 🛠️ **ثلاث أدوات كتالوج جديدة** (تصبح **36 أداة**) بأفعال محرك قائمة
  فعلًا، ملحقة بذيل الكتالوج حفاظًا على ترتيبك وتثبيتك المحفوظَين:
  - **مسح سجل الحافظة**: `CLIPBOARD_CLEAR_HISTORY` يمسح غير المثبت دفعة
    واحدة — المثبتات تبقى محفوظة
  - **اللغة التالية**: `IME_NEXT_SUBTYPE` ينتقل إلى اللغة التالية المكوّنة
    عبر مدير اللغات مباشرة
  - **وضع تغيير الحجم**: `TOGGLE_RESIZE_MODE` يدخل تحجيم اللوحة أو يخرج
    منه — نفس مسار الإجراء السريع الموجود
- 📊 **ثلاثة تجميعات إحصاء جديدة** في بطاقة الإجماليات، دوال نقيّة من
  الدلاء اليومية نفسها: **أطول سلسلة نشاط** (السجل الكلي لا السلسلة
  الحالية فقط)، و**أكثر يوم كتابة في الأسبوع** (باسم اليوم الحقيقي عبر
  تقويم الجهاز)، و**المدى المسجل** (من أقدم يوم حتى اليوم)
- 📤 **تصدير CSV بلا فجوات**: كان التصدير يشمل الأيام النشطة فقط فتتخلل
  الملف أيام مفقودة رغم أن العقد الموثق «نافذة صفرية الامتلاء»؛ صار
  الملف نافذة متصلة من أقدم يوم محفوظ حتى اليوم (حتى 60 يومًا) — والتنبيه
  عن فراغ البيانات ما زال على النشاط الفعلي
- 🩺 **ثلاثة فحوص تشخيص جديدة** (تصبح **29 فحصًا**) بقياسات حقيقية وبلا
  إنذارات كاذبة (المجهول = نجاح):
  - **ربط خدمة التدقيق الإملائي**: مفتاحا `Settings.Secure` نفسان اللذان
    تقرؤهما شاشة الإعدادات — تحذير فقط عندما يكون المدقق مفعّلًا نظاميًا
    لكنه يشير إلى تطبيق آخر
  - **إذن إشعارات التحديث**: على أندرويد 13+ يصمت إشعار التحديث تمامًا
    عند رفض `POST_NOTIFICATIONS` — الفحص يحذر فقط ممن طلب الإشعارات
  - **تكوين الاهتزاز اللمسي**: طلب الاهتزاز بنمط التحكم المباشر على جهاز
    بلا فيبراتور يعني تخطي كل اهتزاز صامتة — تحذير بالتكوين الحقيقي
- 💾 **سقف إصدار على استيراد النسخة الاحتياطية**: ملف حالة مكتوب بإصدار
  مخطط أحدث من التطبيق كان يُستورد صامتةً مع إسقاط الحقول المجهولة
  (فقدان بيانات عند الاستعادة إلى إصدار أقدم) — يُرفض الآن رفضًا صريحًا
  برسالة واضحة، عبر دالة تحقق نقية قابلة للاختبار
- 🎨 **زر «تبديل الثيم الآن» في شاشة الثيم**: `cycleTheme()` كان لا يصل
  إليه إلا الشريط الذكي؛ صار بإمكانك التبديل من الإعدادات نفسها مع toast
  باسم الثيم الجديد الحقيقي
- ♻️ **إصلاح تسريب حقيقي في محرك الثيمات**: كل تبديل ثيم كان ينشئ مجلد
  فك ضغط جديدًا تحت `cacheDir/loaded` بلا تنظيف قطعًا (TODO قائم) — صار
  الكاش محدودًا بثلاثة مدخلات والمجلدات المُخلّى عنها تُحذف فعليًا، مع
  حماية الثيم النشط من الإخلاء
- 🌐 **47 مفتاح سلاسل جديدًا** بتوافق عربي/إنجليزي تام (فحص فرق آلي:
  PARITY OK — 2013 مفتاحًا لكل لغة).
- ✅ **168 اختبار وحدة ناجح** (كانت 150): العمليات الثماني بأطرافها
  (العربية بلا حالة، ZWNJ محفوظ، ثبات الترتيب بالطول، المسافات المعلّقة،
  القابلية للتكرار)، الأرقام الفارسية، الدوال الإحصائية الثلاث بالتعادل
  الحتمي، أدوات الكتالوج الثلاث برموزها الحقيقية وعقد الذيل، إصلاح أداة
  اللغة، وسقف إصدار النسخ الاحتياطي.

## التنزيل

- **APK**: `DRS-Smart-Keyboard-v1.5.0.apk` — ثبّته مباشرة (الترقية موضعية
  آمنة فوق أي إصدار سابق، نفس مفتاح التوقيع)
- **AAB**: للحاجة المتقدمة
- **SHA256SUMS.txt**: تحقق تشفيري كامل من كل الملفات
- **حزم السمات** (8): سمات إضافية اختيارية بتثبيت لاحق

## التحقق من التكامل

بعد التثبيت: شاشة التشخيصات ← «الفحص التقني الشامل» يتحقق الآن من 29
بندًا حقيقيًا داخل العملية نفسها، وشاشة الإحصاءات تعرض الأيام النشطة وأطول
سلسلة وأكثر يوم كتابة — كلها من بياناتك المحلية وحدها.

## الخصوصية

كل شيء يعمل **دون اتصال بالإنترنت إطلاقًا** في مسار الكتابة،
والإحصاءات عدّادات مجهولة على جهازك (لا نصوص ولا طوابع زمنية للضغطات)،
والنسخ الاحتياطي ملف JSON محلي تختار وجهته بنفسك عبر منتقي ملفات النظام.

</div>

---

<div align="center">

## About

**DRS Smart Keyboard** is a free, open-source (Apache-2.0) Android keyboard
built with Kotlin, Jetpack Compose and Material 3 — **Arabic-first**: a
fully localized Arabic UI with native RTL, carefully designed Arabic
layouts, and Arabic suggestions and correction, with English as a complete
second option. Everything you type stays on your device: no accounts, no
tracking, no ads.

## What's new in V 1.5.0 — every feature wired to the real engine

- 🐛 **Two root fixes for silent dead paths**: the LANGUAGE catalogue tool
  (a no-op since V 1.0.8 — its code had no handler) now opens the real
  in-keyboard subtype picker; the tech toolbar's Tab/Esc keys are handled
  for the first time (a real tab commit / hide-UI via the existing path).
- 🏷️ **Real names for most-used tiles**: text-tool tiles no longer render
  the invalid-fatal placeholder — all 34 tools show their actual panel
  titles/descriptions, plus six previously unnamed codes (page/line moves,
  clipboard select, smartbar visibility).
- ✍️ **Eight new pure text operations (34 total)**: invert case (Arabic
  untouched), Latin→Arabic punctuation mapping, zero-width cleanup that
  deliberately keeps ZWNJ, tabs→4 spaces, stable sort-by-line-length,
  word-level dedup with no dangling separators, parenthesis wrapping, and
  one-sentence-per-line. TO_ARABIC_DIGITS now also maps Persian/Urdu
  digits (۰-۹).
- 🛠️ **Three new catalogue tools (36 total)** on real engine actions,
  tail-appended to preserve saved ordering/pins: clear unpinned clipboard
  history, next language, resize mode.
- 📊 **Three new pure stats aggregations**: longest activity streak
  (all-time), busiest typing weekday (localized real name), and recorded
  span (oldest day → today).
- 📤 **Gap-free CSV export**: the file is now a continuous zero-filled
  window across the full retention span (matching the documented
  contract) instead of activity-only days.
- 🩺 **Three new diagnostics checks (29 total)**, measured and
  false-alarm-proof: spell-checker wiring (the same Settings.Secure keys
  the settings screen reads), Android-13+ notification permission for
  update notices, and haptics configuration sanity (direct-vibrator mode
  with no vibrator).
- 💾 **Backup schema version ceiling**: files from a newer schema are
  rejected with a clear message instead of silently dropping unknown
  fields on downgrade-restore (pure, unit-tested validation).
- 🎨 **"Cycle theme now" button on the theme screen** — the cycleTheme()
  engine action is no longer keyboard-strip-only, with a real toast of
  the new theme's label.
- ♻️ **Theme engine leak fixed**: the loaded-dir cache is bounded (3
  entries) and evicted unzipped folders are physically deleted — the
  upstream TODO leak is gone, with the active theme protected from
  eviction.
- 🌐 **47 new AR/EN string keys**, parity-checked automatically
  (PARITY OK — 2013 keys per language).
- ✅ **168 unit tests passing** (was 150), covering every new operation's
  edge cases, the Persian digits, all three stats aggregations including
  deterministic ties, the new catalogue tools' real codes and the
  tail-append contract, the language-tool fix, and the backup ceiling.

## Download

- **APK**: `DRS-Smart-Keyboard-v1.5.0.apk` — install directly (safe
  in-place upgrade over any previous release, same signing key)
- **AAB**: for advanced needs
- **SHA256SUMS.txt**: full cryptographic verification of every asset
- **Theme packs** (8): optional add-on themes, installable later

## Privacy

Everything runs **fully offline**: no internet on the typing path at all.
Statistics are anonymous counters on your device (never text, never
per-keystroke timestamps), and backups are local JSON files whose
destination you pick through the system file picker.

</div>
