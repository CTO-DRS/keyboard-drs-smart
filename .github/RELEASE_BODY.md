<div align="center">

# DRS Smart Keyboard V 1.6.0

## الجولة الشاملة السادسة — قناتان ميتتان تُحييَتان وخمس أدوات كتالوج وأربع عمليات نصية ومقياس معاينة وعدّاد اقتراحات

**Sixth Comprehensive Round — Two Dead Tiles Revived, Five Catalog Tools, Four Text Ops, Preview Scale, Suggestion Counter**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.6.0 — كل ميزة مربوطة بالمحرك فعليًا

- 🐛 **آخر بلاطتَين بعنوان فاشل أصبحتا حقيقيتين**: بلاطتا «مسح سجل
  الحافظة» و«اللغة التالية» اللتان أُضيفتا في V 1.5.0 كانتا تظهران بعنوان
  العنصر الفاشل العام `invalid_fatal` حين تصعدا إلى قسم «الأكثر استخدامًا»
  في الشريط الذكي — لأن دالتي العنوان والتلميح لم تكونا تعرفان رمزيهما.
  صارتا تعرضان اسميهما ووصفهما الحقيقيين، ومعهما خمسة رموز أدوات جديدة كلها
  بأسماء وتلميحات كاملة منذ اليوم الأول
- 🛠️ **خمس أدوات كتالوج جديدة** (تصبح **41 أداة**) بأفعال محرك قائمة
  فعلًا، ملحقة بذيل الكتالوج حفاظًا على ترتيبك وتثبيتك المحفوظَين:
  - **مسح الحافظة كاملة**: `CLIPBOARD_CLEAR_FULL_HISTORY` يمسح السجل كله
    بما فيه العناصر المثبتة — الشقيق المدمّر لأداة «مسح سجل الحافظة» التي
    تحفظ المثبتات
  - **اللغة السابقة**: `IME_PREV_SUBTYPE` ينتقل إلى اللغة السابقة المكوّنة
    عبر مدير اللغات مباشرة — النظير التام لأداة «اللغة التالية»
  - **اليد الواحدة: يسار / يمين**: `COMPACT_LAYOUT_TO_LEFT/RIGHT` ينقلان
    نافذة اليد الواحدة إلى الحافة المطلوبة حتمًا بدل الاكتفاء بالتبديل —
    عبر مسار وحدة التحكم بالنافذة نفسه
  - **اللوحة التالية**: `SYSTEM_NEXT_INPUT_METHOD` ينتقل إلى لوحة المفاتيح
    التالية في قائمة النظام — نفس المسار الذي يستخدمه إجراء «تبديل
    التطبيق» للمفتاح المساعد
- ✍️ **أربع عمليات نصية جديدة** (تصبح **38 عملية**)، كلها تحويل نقي:
  - **مسافات إلى تبويبات**: كل أربع مسافات متتالية تصبح محرف جدولة واحدًا
    (8 مسافات ← تبوبان، 5 ← تبوّب ومسافة) — النقيض الحقيقي لعملية
    «تبويبات إلى مسافات»، ومرتدتها الكاملة مُختبرة
  - **الترقيم الغربي**: ، و؛ و؟ العربي إلى نظيراتها اللاتينية دون مساس
    بأي محرف آخر — النقيض التام لعملية «الترقيم العربي»
  - **تنظيف الحواف الفارغة**: يحذف الأسطر الفارغة من بداية النص ونهايته
    **فقط**، ويبقي الفواصل الفارغة في الوسط كما هي تمامًا — ركن الثالوث
    الناقص بين «تكثيف الأسطر» و«إزالة الأسطر الفارغة»
  - **عكس ترتيب الكلمات**: يقلب ترتيب الكلمات في كل سطر (الأخيرة أولًا)
    — الشقيق الكلموي لعملية «عكس ترتيب الأسطر»، والأسطر الفارغة تبقى كما
    هي والسطر الختامي محفوظ
- 🎨 **مقياس حجم معاينة المفاتيح** (70–200%): حجم صندوق المعاينة الذي
  يظهر عند الضغط المطول كان مضروبًا بثوابت مضمّنة في كود الرسم — صار
  إعدادًا حقيقيًا `keyboard__preview_scale_percent` يُقرأ لحظة إظهار كل
  معاينة ويتبعه الصندوق أفقيًا وعموديًا في الوضعين الرأسي والأفقي، مع
  شريط تمرير في المجموعة الأساسية وثوابت تحقق مشتركة بين الشريط والمحرك
  والاختبارات
- 📊 **عدّاد قبول الاقتراحات من طرف إلى طرف**: كل صف اقتراح يُقبل (نقرًا
  أو إكمالًا تلقائيًا) يمر عبر `commitCandidate` وحدها — صار يُعدّ الآن
  في المحرك (`recordSuggestionAccept`) ويتدفق إلى الدلاء اليومية وبطاقة
  الإجماليات وصف «الاقتراحات المقبولة» وعمود جديد في CSV (11 عمودًا) —
  عدّ مجهول للحدث لا للكلمة المقبولة نفسها
- 📈 **ثلاثة تجميعات إحصاء جديدة** في بطاقة الإجماليات، دوال نقيّة من
  الدلاء اليومية نفسها: **أيام بلا نشاط داخل المدى** (المدى ناقص الأيام
  النشطة)، و**نسبة الأيام النشطة** (0–100% بأرقام مستقلة عن اللغة)، و**أقل
  يوم كتابة في الأسبوع** (النظير الهادئ لـ«أكثر يوم»، بالاسم الحقيقي عبر
  تقويم الجهاز)
- 🩺 **فحصا تشخيص جديدان** (تصبح **31 فحصًا**):
  - **سلامة ملف حالة DRS** (خطأ حقيقي لا تحذير): إعادة فك ترميز ملف
    الحالة المحلي — فقدان الترميز يعني صمت المسح الكامل للأنظمة
    والاختصارات والإحصاءات عند أول كتابة، وكان يمر بلا أي إشارة إطلاقًا
  - **سجل انهيار سابق** (تحذير): وجود سجل انهيار من تشغيل سابق كان يُقرأ
    ويُعرض على الشاشة دون أن يصل إلى الفحص الشامل قط — صار يظهر في
    الملخص ليستعرضه المستخدم ويمسحه
- 🌐 **42 مفتاح سلاسل جديدًا** بتوافق عربي/إنجليزي تام (فحص فرق آلي:
  PARITY OK — 2055 مفتاحًا لكل لغة).
- ✅ **179 اختبار وحدة ناجح** (كانت 168): العمليات الأربع بأطرافها
  (التقسيم الحتمي للمسافات، الارتداد الكامل مع النقيضين، حفظ السطر
  الختامي، الأسطر الفارغة في الوسط)، أدوات الكتالوج الخمس برموزها
  الحقيقية وعقد الذيل الممتد، تسجيل الأدوات الجديدة في إحصاء «الأكثر
  استخدامًا»، عدّاد الاقتراحات عبر الدمج والجمع والعقلنة والدلالة،
  التجميعات الثلاث بالتعادل الحتمي، وحدود مقياس المعاينة.

## التنزيل

- **APK**: `DRS-Smart-Keyboard-v1.6.0.apk` — ثبّته مباشرة (الترقية موضعية
  آمنة فوق أي إصدار سابق، نفس مفتاح التوقيع)
- **AAB**: للحاجة المتقدمة
- **SHA256SUMS.txt**: تحقق تشفيري كامل من كل الملفات
- **حزم السمات** (8): سمات إضافية اختيارية بتثبيت لاحق

## التحقق من التكامل

بعد التثبيت: شاشة التشخيصات ← «الفحص التقني الشامل» يتحقق الآن من 31
بندًا حقيقيًا داخل العملية نفسها (بينها سلامة ملف الحالة، وفحص الانهيار
السابق)، وشاشة الإحصاءات تعرض «الاقتراحات المقبولة» مع الأيام النشطة
ونسبتها وأقل يوم كتابة — كلها من بياناتك المحلية وحدها.

## الخصوصية

كل شيء يعمل **دون اتصال بالإنترنت إطلاقًا** في مسار الكتابة،
والإحصاءات عدّادات مجهولة على جهازك (لا نصوص ولا طوابع زمنية للضغطات
ولا الكلمات المقبولة نفسها)، والنسخ الاحتياطي ملف JSON محلي تختار وجهته
بنفسك عبر منتقي ملفات النظام.

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

## What's new in V 1.6.0 — every feature wired to the real engine

- 🐛 **The last two placeholder tiles are real now**: the V 1.5.0
  clipboard-history-clear and next-language tiles rendered the
  invalid-fatal label when they surfaced as most-used smartbar tiles —
  the display-name/tooltip functions didn't know their codes. They show
  their real names now, together with five new tool codes that ship fully
  named from day one.
- 🛠️ **Five new catalogue tools (41 total)** on real engine actions,
  tail-appended to preserve saved ordering/pins: full-history wipe
  (pinned items included), previous language (the exact counterpart of
  next language), one-handed left/right edge placement (deterministic,
  same window-controller path), and next keyboard app (the system
  input-method list).
- ✍️ **Four new pure text operations (38 total)**: spaces→tabs (runs of
  four split deterministically; full roundtrip tested), Arabic→Latin
  punctuation (the exact inverse of the v1.5.0 mapping), edge-only blank
  trimming (interior blank separators stay untouched), and per-line
  word-order reversal (the word-level sibling of reverse-lines).
- 🎨 **Key-preview size scale (70–200%)**: the long-press preview box was
  sized by hardcoded multipliers — it is now a real setting
  (`keyboard__preview_scale_percent`) read at popup time and honored in
  both orientations, with a slider in the basic group and shared
  sanitize constants.
- 📊 **End-to-end suggestion-accept counter**: every committed
  suggestion-row entry (tapped or auto-committed) flows through
  commitCandidate alone — it is now counted in the engine, persisted
  into the daily buckets, shown in the totals card and exported as a new
  CSV column (11 columns). The count is anonymous — never WHICH word.
- 📈 **Three new pure stats aggregations**: missed days inside the
  recorded span, the active-day share (0–100%), and the quietest typing
  weekday (the calm counterpart of busiest weekday, localized).
- 🩺 **Two new diagnostics checks (31 total)**: DRS state-file decode
  integrity (a genuine ERROR — decode loss meant a silent full reset of
  systems/shortcuts/stats on the next write, with zero signal), and
  previous crash-log presence as a warning (the log was displayed but
  never fed the full test).
- 🌐 **42 new AR/EN string keys**, parity-checked automatically
  (PARITY OK — 2055 keys per language).
- ✅ **179 unit tests passing** (was 168), covering every new
  operation's edge cases and roundtrips, the five new tools' real codes
  and the extended tail contract, SmartToolCodes registration for the
  most-used stats, the suggestion counter through merge/sum/sanity/
  activity, the deterministic-tie aggregations, and the preview-scale
  bounds.

## Download

- **APK**: `DRS-Smart-Keyboard-v1.6.0.apk` — install directly (safe
  in-place upgrade over any previous release, same signing key)
- **AAB**: for advanced needs
- **SHA256SUMS.txt**: full cryptographic verification of every asset
- **Theme packs** (8): optional add-on themes, installable later

## Privacy

Everything runs **fully offline**: no internet on the typing path at all.
Statistics are anonymous counters on your device (never text, never
per-keystroke timestamps, never the accepted words themselves), and
backups are local JSON files whose destination you pick through the
system file picker.

</div>
