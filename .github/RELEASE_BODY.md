<div align="center">

# DRS Smart Keyboard V 1.9.0

## الجولة الشاملة التاسعة — نظام الحافظة الذكي المتكامل: تعديل النص، الحفظ كملف باسمك، سقف 50,000 حرف، وتصدير السجل

**Ninth Comprehensive Round — The Integrated Smart Clipboard System: Edit Text, Save-As-File With Your Name, the 50,000-Character Cap, and History Export**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.9.0 — نظام متكامل وذكي واحترافي للحافظة

- ✏️ **تعديل أي نص في السجل من داخل الكيبورد**: قائمة العنصر (ضغطة
  مطوّلة) صارت تمنح **«تعديل النص»** — محرر حقيقي داخل لوحة الحافظة
  نفسها يفتح النص الحالي، ويحفظ عبر مسار محركي واحد: تحديث السجل مع
  رفع الطابع الزمني فيطفو العنصر إلى المقدمة، وبقاء التثبيت
  والهوية كما هما، ومزامنة المقصوصة الأساسية إذا كانت هي العنصر
  نفسه عبر مسار المزامنة المعتمد ذاته
- 📊 **إحصاءات حية أثناء التعديل**: عدّاد **الأحرف والكلمات والأسطر**
  يتحدث لحظة بلحظة أثناء الكتابة، محسوبًا من دوال نقية مختبرة
  (الكلمات مقاطع مفصولة بمسافات، والأسطر فواصل حقيقية) — لا
  تقدير ولا تخمين
- 🗂️ **حفظ أي نص كملف باسم تختاره**: فعل **«حفظ كملف»** يفتح حوار
  اسم الملف معبأً مسبقًا باسم افتراضي مؤرخ (`drs-clip-20260925-1430.txt`)،
  ثم يكتب النص فعليًا:
  - **أندرويد 10 وما بعده**: مساهمة MediaStore في مجلد
    **«التنزيلات/DRS Keyboard»** — مرئية لك ولأي تطبيق، دون أي
    إذن تخزين
  - **أندرويد 8-9**: مجلد التطبيق الخاص `files/clips` مع لاحقة
    تصادم تلقائية ` (1)`
  - **تنقية الاسم احترافية**: محارف `/ \ : * ? " < > |` ومحارف
    التحكم تصبح `_`، وحواف النص ونقاطه الختامية تُقتطع (خطر
    ويندوز)، والامتداد `txt` يُفرض دائمًا (إن وُجد امتداد أجنبي
    كـ`json` بقي داخل الاسم وأُلحق `txt` بعده)، والاسم الكامل
    مقيّد بـ80 حرفًا مع الحفاظ على الامتداد، والفراغ يعود
    للاسم الافتراضي
- 🔢 **سقف 50,000 حرف للنص الواحد**: عقد تخزين صريح — **كل نص في
  السجل يحتفظ بحد أقصى 50,000 حرف**، مطبقًا في نقطة اختناق واحدة
  نقية (`ClipboardTextPolicy`) على كل طريق الإدخال: النسخ من
  النظام، واللصق الداخلي، والتعديل اليدوي. القصّ **آمن مع
  الإيموجي**: لو وقع حدّ الـ50,000 داخل زوج بديل (surrogate pair)
  تراجع خطوة واحدة فلا يُنتج نصًا تالفًا أبدًا، والمقصوصة
  الأساسية تحتفظ بالنص الكامل — السقف يحمي السجل وقاعدة
  البيانات والعرض فقط
- 📌 **شارة تثبيت حقيقية على البلاطات**: كل عنصر مثبّت يحمل أيقونة
  الدبوس في زاويته — حالة حقيقية مقروءة من العنصر نفسه، وسُلّم
  الإجراءات في القائمة المنبثقة **أُعيد ترتيبه احترافيًا**:
  لصق ← نسخ مجددًا ← تثبيت/فك ← تعديل ← حفظ كملف ← حذف
  (التعديل والحفظ للنصوص فقط)
- 📤 **تصدير سجل الحافظة من الإعدادات**: زر جديد في شاشة الحافظة
  يكتب **كل نصوص السجل في ملف JSON واحد محمول** عبر منتقي ملفات
  النظام (النصوص فقط — بايتات الوسائط لا تصلح للرحلة ذهابًا
  وإيابًا)، بحقل `text` و`createdAt` و`pinned` لكل عنصر، مع
  توست نجاح/فشل صادق
- 🌐 **16 مفتاح سلاسل AR/EN جديدًا**، مفحوص التوازي آليًا
  (PARITY OK — 2140 مفتاحًا لكل لغة)
- ✅ **233 اختبار وحدة ناجحًا** (كانت 214): 19 اختبارًا جديدًا
  تغطي سقف الـ50,000 بحدّيه تمامًا وفوقه، وسلامة أزواج الإيموجي
  عند القص، وعقد تنقية اسم الملف كاملًا (المحارف غير الشرعية،
  النقاط الختامية، فرض الامتداد، سقف الـ80، الأسماء الافتراضية
  المؤرخة بمنطقة زمنية مثبتة)، والإحصاءات الثلاث على نصوص عربية
  ولاتينية وفارغة ومسافات فقط، وخطة التعديل (نص جديد وطابع مرفوع
  وتثبيت وهوية محفوظان وقصّ فوق السقف)، وتصدير JSON برحلة ذهاب
  وإياب كاملة والعربي والمثبتات محفوظان

## بعد التثبيت

افتح لوحة الحافظة (من الشريط أو الأدوات) واضغط مطولًا على أي نص:
ستجد السلّم الجديد كاملًا — عدّل النص بمؤشرات حية، أو احفظه ملفًا
باسمك في «التنزيلات/DRS Keyboard». انسخ نصًا أطول من 50 ألف حرف
وسترى السجل يحتفظ بأول 50,000 حرف بأمان. ومن شاشة الإعدادات ←
الحافظة صدّر السجل كله ملفًا واحدًا وقتما شئت — وكل ذلك من بياناتك
المحلية وحدها.

## الخصوصية

كل شيء يعمل **دون اتصال بالإنترنت إطلاقًا** في مسار الكتابة،
والحفظ والتصدير يكتبان في المكان الذي تختاره أنت فقط (منتقي ملفات
النظام أو مجلد تنزيلاتك)، ولا إذن تخزين يُطلب في أي حال، والإحصاءات
عدّادات مجهولة على جهازك — ولا نصوص ولا طوابع زمنية للضغطات ولا
هوية حقول، كما في كل جولة قبل هذه.

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

## What's new in V 1.9.0 — an integrated, smart, professional clipboard

- ✏️ **Edit any history text from inside the keyboard**: the item's
  long-press menu now offers **«Edit text»** — a real editor inside the
  clipboard panel that opens the current text and saves through one
  engine path: the history row is updated with a bumped timestamp (the
  item floats to the top), pin state and id preserved, and the primary
  clip is re-synced through the established sync path when it is the
  same item.
- 📊 **Live stats while editing**: the **character / word / line**
  counts update on every keystroke, computed by tested pure functions
  (words are whitespace-separated runs, lines are real newline counts)
  — no estimation anywhere.
- 🗂️ **Save any text as a file with your own name**: **«Save as
  file»** opens a file-name dialog prefilled with a timestamped default
  (`drs-clip-20260925-1430.txt`) and then really writes the text:
  - **Android 10+**: a MediaStore contribution under
    **Downloads/DRS Keyboard** — visible to you and other apps, with
    no storage permission requested
  - **Android 8-9**: the app-private `files/clips` directory with an
    automatic ` (1)` collision suffix
  - **Professional name sanitization**: `/ \ : * ? " < > |` and
    control characters become `_`, edges and trailing dots are trimmed
    (a Windows hazard), the `.txt` extension is always enforced (a
    foreign `.json` stays inside the base with `.txt` appended), the
    full name is capped at 80 characters preserving the extension, and
    blank input falls back to the default
- 🔢 **The 50,000-character cap per text**: an explicit storage
  contract — **one history text retains at most 50,000 characters**,
  enforced at a single pure choke point (`ClipboardTextPolicy`) across
  every intake path: system sync, internal paste and manual edit.
  Truncation is **emoji-safe**: if the boundary lands inside a
  surrogate pair it steps back one character instead of producing
  corrupted text, and the primary clip keeps the FULL text — the cap
  protects only the history database and rendering.
- 📌 **A real pin badge on tiles**: every pinned item wears a pushpin
  icon in its corner — true state read from the item itself — and the
  popup action ladder is **re-organized professionally**: paste ← copy
  again ← pin/unpin ← edit ← save as file ← delete (edit/save for
  text items only).
- 📤 **History export from the settings screen**: a new button on the
  clipboard screen writes **all history texts into one portable JSON
  document** through the system file picker (texts only — media bytes
  cannot round-trip), with `text` / `createdAt` / `pinned` per entry
  and an honest success/failure toast.
- 🌐 **16 new AR/EN string keys**, parity-checked automatically
  (PARITY OK — 2140 keys per language).
- ✅ **233 unit tests passing** (was 214): 19 new tests covering both
  sides of the 50k boundary and one char over it, surrogate-pair
  truncation safety, the full file-name contract (illegal characters,
  trailing dots, extension enforcement, the 80-char cap, timestamped
  defaults at a pinned time zone), the three stats on Arabic/Latin/
  empty/whitespace texts, the edit plan (new text, bumped timestamp,
  preserved pin/id, over-cap input), and a full JSON export
  round-trip with Arabic and pin state intact.

## Download

- **APK**: `DRS-Smart-Keyboard-v1.9.0.apk` — install directly (safe
  in-place upgrade over any previous release, same signing key)
- **AAB**: for advanced needs
- **SHA256SUMS.txt**: full cryptographic verification of every asset
- **Theme packs** (8): optional add-on themes, installable later

## Privacy

Everything runs **fully offline**: no internet on the typing path at
all. Saving and exporting write only where you choose (the system file
picker or your Downloads folder), no storage permission is ever
requested, and statistics remain anonymous counters on your device
(never text, never per-keystroke timestamps, never field identity) —
same as every round before this one.

</div>
